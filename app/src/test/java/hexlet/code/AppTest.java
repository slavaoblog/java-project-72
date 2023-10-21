package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    private static Javalin app;

    @BeforeEach
    public void setUp() throws Exception {
        app = App.getApp();
    }

    @Test
    void testMainPage() {

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            assert response.body() != null;
            assertThat(response.body().string()).contains("Page Analyzer");
        });
    }

    @Test
    void testUrlsPage() {

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls");
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    void testUrlPage() throws Exception {
        long now = System.currentTimeMillis();
        Timestamp sqlTimestamp = new Timestamp(now);
        var url = new Url("http://www.google.com", sqlTimestamp);
        UrlRepository.save(url);
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
            assert response.body() != null;
            assertThat(response.body().string()).contains("http://www.google.com");
        });
    }

    @Test
    public void testCreateUrl() throws Exception {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=http://www.google.com";
            var response = client.post("/urls", requestBody);
            assertThat(response.code()).isEqualTo(200);
            assert response.body() != null;
            var responseBody = response.body().string();
            assertThat(responseBody.contains("http://www.google.com")).isTrue();
            assertThat(responseBody.contains("Page added successfully")).isTrue();
        });

        assertThat(UrlRepository.getEntities()).hasSize(1);
        assertThat(UrlRepository.existsByName("http://www.google.com")).isTrue();
    }

    @Test
    public void testCreateUrlNull() throws Exception {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=www.google.com";
            var response = client.post("/urls", requestBody);
            assert response.body() != null;
            var responseBody = response.body().string();
            assertThat(responseBody.contains("www.google.com")).isFalse();
            assertThat(responseBody.contains("Incorrect URL")).isTrue();
        });

        assertThat(UrlRepository.getEntities()).hasSize(0);
    }

    @Test
    public void testCreateUrlDoubled() throws Exception {
        JavalinTest.test(app, (server, client) -> {
            long now = System.currentTimeMillis();
            var ts = new Timestamp(now);
            var url = new Url("http://www.google.com", ts);
            UrlRepository.save(url);

            var requestBody = "url=http://www.google.com";
            var response = client.post("/urls", requestBody);
            assert response.body() != null;
            var responseBody = response.body().string();
            assertThat(responseBody.contains("Page already exists")).isTrue();
        });

        assertThat(UrlRepository.getEntities()).hasSize(1);
        assertThat(UrlRepository.existsByName("http://www.google.com")).isTrue();
    }
}
