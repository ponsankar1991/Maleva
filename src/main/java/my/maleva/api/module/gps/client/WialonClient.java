package my.maleva.api.module.gps.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.gps.config.WialonProperties;
import my.maleva.api.module.gps.dto.wialon.WialonReportExecResponse;
import my.maleva.api.module.gps.dto.wialon.WialonResourceItem;
import my.maleva.api.module.gps.dto.wialon.WialonResultRow;
import my.maleva.api.module.gps.dto.wialon.WialonSearchResponse;
import my.maleva.api.module.gps.dto.wialon.WialonSession;
import my.maleva.api.module.gps.dto.wialon.WialonUnitItem;
import my.maleva.api.module.gps.exception.WialonApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Thin client over the Wialon Hosting remote API.
 *
 * Ports the HTTP half of the legacy Common/GPSJob.cs, with three corrections
 * carried over deliberately:
 *
 * <ul>
 *   <li>Wialon signals failure as HTTP 200 with a body of {"error":N}. The
 *       legacy code only checked IsSuccessStatusCode, so a failed login
 *       deserialised into an all-null config and the run silently did nothing.
 *       Every response is inspected here.</li>
 *   <li>The params JSON is URL encoded. The legacy code pasted raw JSON straight
 *       into the query string.</li>
 *   <li>The token is never logged and the session id is truncated in logs.</li>
 * </ul>
 *
 * @see <a href="https://sdk.wialon.com/wiki/en/sidebar/remoteapi/apiref/apiref">Wialon API reference</a>
 */
@Component
public class WialonClient {

    private static final Logger logger = LoggerFactory.getLogger(WialonClient.class);

    /** Wialon DST flag, applied on top of the base offset. */
    private static final long DST_FLAG = 0x08000000L;
    /** Mask applied to the base offset before the DST flag is OR-ed in. */
    private static final long TZ_MASK = 0xF000FFFFL;

    private final WialonProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public WialonClient(WialonProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));
        factory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Exchanges the configured token for a session. The returned eid is the sid
     * that every other call needs.
     */
    public WialonSession login() {
        if (properties.getToken() == null || properties.getToken().isBlank()) {
            throw new WialonApiException("Wialon token is not configured (set WIALON_TOKEN)");
        }
        // fl is a required flag; 1 = basic information. The guide states that all
        // numbers including flags must be decimal, so the legacy "0x1" string is
        // not a valid value even though the server tolerated it.
        String params = "{\"token\":\"" + properties.getToken() + "\",\"fl\":1}";
        WialonSession session = call("token/login", null, params, WialonSession.class);
        if (session == null || session.getEid() == null || session.getEid().isBlank()) {
            throw new WialonApiException("Wialon login returned no session id");
        }
        logger.info("Wialon login ok, sid {}...", abbreviate(session.getEid()));
        return session;
    }

    /**
     * Sets the render locale for the session so report timestamps come back in
     * the expected timezone and format.
     */
    public void setLocale(String sid) {
        WialonProperties.Locale locale = properties.getLocale();
        String params = "{\"tzOffset\":" + tzOffset()
                + ",\"language\":\"" + locale.getLanguage() + "\""
                + ",\"flags\":0"
                + ",\"formatDate\":\"" + locale.getFormatDate() + "\""
                + ",\"density\":1}";
        call("render/set_locale", sid, params, JsonNode.class);
        logger.info("Wialon locale set, tzOffset {}", tzOffset());
    }

    /**
     * Wialon packs the UTC offset and the DST flag into one integer.
     *
     * @see <a href="https://help.wialon.com/en/api/user-guide/data-format/time/tz-example">tz format</a>
     */
    public long tzOffset() {
        WialonProperties.Locale locale = properties.getLocale();
        long masked = ((long) locale.getTzBaseSeconds()) & TZ_MASK;
        return locale.isDst() ? (masked | DST_FLAG) : masked;
    }

    /** Every avl_unit visible to the token - one per tracked vehicle. */
    public List<WialonUnitItem> searchUnits(String sid) {
        String params = "{\"spec\":{\"itemsType\":\"avl_unit\",\"propName\":\"sys_name\","
                + "\"propValueMask\":\"*\",\"sortType\":\"sys_name\"},"
                + "\"force\":1,\"flags\":1,\"from\":0,\"to\":0}";
        WialonSearchResponse<WialonUnitItem> response = call("core/search_items", sid, params,
                new TypeReference<WialonSearchResponse<WialonUnitItem>>() { });
        return response == null || response.getItems() == null
                ? Collections.emptyList()
                : response.getItems();
    }

    /**
     * Every avl_resource with its report templates. Flag 8193 asks for the rep
     * block, which is what template resolution by name needs.
     */
    public List<WialonResourceItem> searchResources(String sid) {
        String params = "{\"spec\":{\"itemsType\":\"avl_resource\",\"propName\":\"sys_name\","
                + "\"propValueMask\":\"*\",\"sortType\":\"sys_name\"},"
                + "\"force\":1,\"flags\":8193,\"from\":0,\"to\":0}";
        WialonSearchResponse<WialonResourceItem> response = call("core/search_items", sid, params,
                new TypeReference<WialonSearchResponse<WialonResourceItem>>() { });
        return response == null || response.getItems() == null
                ? Collections.emptyList()
                : response.getItems();
    }

    /**
     * Runs a report over an interval. fromEpoch and toEpoch are in seconds - see
     * GpsSyncServiceImpl.toEpochSeconds for the legacy quirk in how they are derived.
     */
    public WialonReportExecResponse execReport(String sid,
                                               long resourceId,
                                               int templateId,
                                               long objectId,
                                               long fromEpoch,
                                               long toEpoch) {
        String params = "{\"reportResourceId\":" + resourceId
                + ",\"reportTemplateId\":" + templateId
                + ",\"reportObjectId\":" + objectId
                + ",\"reportObjectSecId\":0"
                + ",\"interval\":{\"from\":" + fromEpoch + ",\"to\":" + toEpoch + ",\"flags\":0}}";
        return call("report/exec_report", sid, params, WialonReportExecResponse.class);
    }

    /**
     * Pulls the rows of one table of the report currently loaded in the session.
     * Must follow execReport on the same sid.
     */
    public List<WialonResultRow> selectResultRows(String sid, int tableIndex, int rows) {
        if (rows <= 0) {
            return Collections.emptyList();
        }
        String params = "{\"tableIndex\":" + tableIndex
                + ",\"config\":{\"type\":\"range\",\"data\":{\"from\":0,\"to\":" + rows
                + ",\"level\":20,\"flat\":0,\"rawValues\":1}}}";
        List<WialonResultRow> result = call("report/select_result_rows", sid, params,
                new TypeReference<List<WialonResultRow>>() { });
        return result == null ? Collections.emptyList() : result;
    }

    /**
     * Clears the report result held by the session.
     *
     * A session can hold only one report result at a time, so this must run
     * before every exec_report after the first. The legacy job never called it
     * and simply fired exec_report again on the same session.
     */
    public void cleanupResult(String sid) {
        call("report/cleanup_result", sid, "{}", JsonNode.class);
    }

    /** Releases the session. Best effort - a failure here is logged, not thrown. */
    public void logout(String sid) {
        try {
            call("core/logout", sid, "{}", JsonNode.class);
        } catch (RuntimeException ex) {
            logger.warn("Wialon logout failed, session will expire on its own: {}", ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- internals

    private <T> T call(String svc, String sid, String params, Class<T> type) {
        String body = exchange(svc, sid, params);
        try {
            return objectMapper.readValue(body, type);
        } catch (Exception ex) {
            throw new WialonApiException("Cannot parse Wialon response for svc=" + svc, ex);
        }
    }

    private <T> T call(String svc, String sid, String params, TypeReference<T> type) {
        String body = exchange(svc, sid, params);
        try {
            return objectMapper.readValue(body, type);
        } catch (Exception ex) {
            throw new WialonApiException("Cannot parse Wialon response for svc=" + svc, ex);
        }
    }

    /**
     * Sends one request.
     *
     * POST with an application/x-www-form-urlencoded body, which is what the
     * guide requires. The legacy job used GET with the JSON pasted unencoded
     * into the query string - that also caps the request at whatever URL length
     * the stack allows, which a long reportObjectIdList would exceed.
     */
    private String exchange(String svc, String sid, String params) {
        StringBuilder form = new StringBuilder()
                .append("svc=").append(URLEncoder.encode(svc, StandardCharsets.UTF_8))
                .append("&params=").append(URLEncoder.encode(params, StandardCharsets.UTF_8));
        if (sid != null && !sid.isBlank()) {
            form.append("&sid=").append(URLEncoder.encode(sid, StandardCharsets.UTF_8));
        }

        logger.debug("Wialon call svc={} sid={}", svc, abbreviate(sid));

        String body;
        try {
            body = restClient.post()
                    .uri(URI.create(properties.getBaseUrl()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form.toString())
                    .retrieve()
                    .body(String.class);
        } catch (WialonApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new WialonApiException("Wialon call failed for svc=" + svc, ex);
        }

        if (body == null || body.isBlank()) {
            throw new WialonApiException("Wialon returned an empty body for svc=" + svc);
        }
        assertNoError(svc, body);
        return body;
    }

    /**
     * Wialon reports failure inside a 200 response, as an object carrying an
     * error code. Zero means success - core/logout answers {"error":0} on a
     * clean exit - so only a non-zero code aborts the call.
     *
     * Some errors add a "reason" string that says which parameter was wrong;
     * it is folded into the message because it is the only diagnostic Wialon
     * gives for error 4.
     */
    private void assertNoError(String svc, String body) {
        JsonNode node;
        try {
            node = objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new WialonApiException("Wialon returned a non-JSON body for svc=" + svc, ex);
        }
        if (node.isObject() && node.has("error")) {
            int code = node.path("error").asInt();
            if (code != 0) {
                String reason = node.path("reason").asText(null);
                String message = "Wialon rejected svc=" + svc
                        + (reason == null || reason.isBlank() ? "" : ", reason=" + reason);
                throw new WialonApiException(message, code);
            }
        }
    }

    private String abbreviate(String sid) {
        if (sid == null || sid.length() <= 6) {
            return "n/a";
        }
        return sid.substring(0, 6);
    }
}
