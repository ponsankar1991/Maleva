package my.maleva.api.security.controller;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * The filter copies every response through its own buffer. That copy must be
 * exact: the invoice PDF reached the browser corrupted once, because the
 * bytes were turned into a String and written back through a text writer.
 */
class JwtAuthenticationFilterBodyTest {

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(mock(JwtService.class), mock(TokenStore.class));

    @Test
    void binaryResponsesPassThroughUnchanged() throws Exception {
        byte[] pdf = new byte[512];
        for (int i = 0; i < pdf.length; i++) {
            pdf[i] = (byte) (i * 7); // every byte value, including the ones text encoding mangles
        }
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/sale-invoices/1/print");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            res.setContentType("application/pdf");
            res.getOutputStream().write(pdf);
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getContentAsByteArray()).isEqualTo(pdf);
        assertThat(response.getContentLength()).isEqualTo(pdf.length);
    }

    @Test
    void textResponsesKeepTheirUtf8() throws Exception {
        String json = "{\"Message\":\"Pengesahan – RM 1,700.00 – ✔\"}";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/x");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(json);
        };

        filter.doFilter(request, response, chain);

        assertThat(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8)).isEqualTo(json);
    }

    @Test
    void logLineShowsTextButOnlyTheSizeOfBinary() {
        byte[] pdf = "%PDF-1.5 ...".getBytes(StandardCharsets.US_ASCII);
        assertThat(JwtAuthenticationFilter.describeBody("application/pdf", pdf))
                .isEqualTo("[binary application/pdf, " + pdf.length + " bytes]");
        assertThat(JwtAuthenticationFilter.describeBody("image/png", new byte[10])).startsWith("[binary image/png");

        assertThat(JwtAuthenticationFilter.describeBody("application/json", "{\"ok\":true}".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo("{\"ok\":true}");
        char[] big = new char[2500];
        Arrays.fill(big, 'x');
        assertThat(JwtAuthenticationFilter.describeBody("text/plain", new String(big).getBytes(StandardCharsets.UTF_8)))
                .endsWith("[TRUNCATED - 500 more characters]");
        assertThat(JwtAuthenticationFilter.describeBody(null, new byte[0])).isEmpty();
    }
}
