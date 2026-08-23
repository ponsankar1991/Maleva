package my.maleva.api.module.common.controller;

import my.maleva.api.common.dto.FireBaseRequestModel;
import my.maleva.api.common.dto.FirebaseData;
import my.maleva.api.common.dto.FirebaseMessage;
import my.maleva.api.common.dto.FirebaseNotification;
import my.maleva.api.common.dto.FirebaseRoot;
import my.maleva.api.common.dto.ResponseViewModel;
import my.maleva.api.module.common.service.ICommonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cross-cutting endpoints ported from the .NET {@code CommonController}.
 *
 * File handling used to live here as five near-identical actions. It now lives
 * in {@code module.filehandling}: new screens call
 * {@code /api/attachments}, and the legacy {@code /Common/UploadFile*} routes
 * are served by {@code LegacyAttachmentController} against the same service.
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

    private final ICommonService commonService;

    public CommonController(ICommonService commonService) {
        this.commonService = commonService;
    }

    @PostMapping("/fetchFiles")
    public ResponseEntity<Map<String, Object>> fetchFiles(@RequestParam String imageDirectory) {
        try {
            ResponseViewModel ro = commonService.fetchFiles(imageDirectory);
            return legacyBody(ro);
        } catch (Exception ex) {
            logger.error("Error in fetchFiles", ex);
            return errorBody(ex);
        }
    }

    @PostMapping("/checkFiles")
    public ResponseEntity<Map<String, Object>> checkFiles(@RequestParam String imageDirectory) {
        try {
            ResponseViewModel ro = commonService.checkFiles(imageDirectory);
            return legacyBody(ro);
        } catch (Exception ex) {
            logger.error("Error in checkFiles", ex);
            return errorBody(ex);
        }
    }

    @PostMapping("/sendNotification")
    public ResponseEntity<Map<String, Object>> sendNotification(@RequestBody List<FireBaseRequestModel> requests) {
        try {
            String firebaseKeyPath = "src/main/resources/maleva-4eefb-firebase-adminsdk-zwr0y-ea250439ed.json";
            String scopes = "https://www.googleapis.com/auth/firebase.messaging";
            String firebaseUrl = "https://fcm.googleapis.com/v1/projects/maleva-4eefb/messages:send";

            GoogleCredentials credentials;
            try (FileInputStream keyStream = new FileInputStream(firebaseKeyPath)) {
                credentials = GoogleCredentials.fromStream(keyStream).createScoped(scopes);
            }
            credentials.refreshIfExpired();
            String bearerToken = credentials.getAccessToken().getTokenValue();

            HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(request -> {
                request.getHeaders().setAuthorization("Bearer " + bearerToken);
                request.getHeaders().setContentType("application/json");
            });
            ObjectMapper objectMapper = new ObjectMapper();

            for (FireBaseRequestModel model : requests) {
                FirebaseRoot payload = FirebaseRoot.builder()
                        .message(FirebaseMessage.builder()
                                .token(model.getTokenid())
                                .data(FirebaseData.builder()
                                        .title(model.getTitle())
                                        .body(model.getBody())
                                        .key_1(model.getKey_1())
                                        .key_2(model.getKey_2())
                                        .build())
                                .notification(FirebaseNotification.builder()
                                        .title(model.getTitle())
                                        .body(model.getMessage())
                                        .image(model.getImageUrl())
                                        .build())
                                .build())
                        .build();

                HttpRequest request = requestFactory.buildPostRequest(
                        new GenericUrl(firebaseUrl),
                        new ByteArrayContent("application/json", objectMapper.writeValueAsBytes(payload)));
                HttpResponse response = request.execute();
                response.disconnect();
            }

            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception ex) {
            logger.error("Error in sendNotification", ex);
            return errorBody(ex);
        }
    }

    private ResponseEntity<Map<String, Object>> legacyBody(ResponseViewModel ro) {
        Map<String, Object> body = new HashMap<>();
        body.put("ok", Boolean.TRUE.equals(ro.isSuccess()));
        body.put("message", ro.getMessage());
        body.put("data", ro.getData1());
        return ResponseEntity.ok(body);
    }

    /**
     * Uses a mutable map because {@code Map.of} rejects null values, and an
     * exception with a null message would then throw a second time inside the
     * handler that is meant to report the first.
     */
    private ResponseEntity<Map<String, Object>> errorBody(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("ok", false);
        body.put("error", String.valueOf(ex.getMessage()));
        return ResponseEntity.ok(body);
    }
}
