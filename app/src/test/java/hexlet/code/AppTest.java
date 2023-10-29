package hexlet.code;

import hexlet.code.controllers.UrlController;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    private static Javalin app;
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();

        String expectedBody = Files.readString(Path.of("src/test/resources/test.html"));
        mockWebServer.enqueue(new MockResponse().setBody(expectedBody));
        mockWebServer.start();
    }

    @BeforeEach
    public void setUp() throws Exception {
        app = App.getApp();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockWebServer.shutdown();
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
    public void testAddUrl() throws Exception {
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
    public void testAddBadUrl() throws Exception {
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
    public void testAddExistingUrl() throws Exception {
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

    @Test
    void testParseUrl() {
        String expected1 = "https://www.example.com";
        String expected2 = "https://www.example.com:8080";
        String actual1 = UrlController.parseUrl("https://www.example.com/one/two");
        String actual2 = UrlController.parseUrl("https://www.example.com:8080/one/two");
        String actual3 = UrlController.parseUrl("www.example.com");
        assertThat(actual1).isEqualTo(expected1);
        assertThat(actual2).isEqualTo(expected2);
        assertThat(actual3).isEqualTo(null);
    }

    @Test
    void testCheckUrl() throws SQLException {
        String serverUrl = mockWebServer.url("/").toString();
        String correctServerUrl = serverUrl.substring(0, serverUrl.length() - 1);

        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=" + correctServerUrl;
            var response1 = client.post("/urls", requestBody);
            assertThat(response1.code()).isEqualTo(200);
            assert response1.body() != null;
            var responseBody1 = response1.body().string();
            assertThat(responseBody1).contains(correctServerUrl);
            assertThat(responseBody1).contains("Page added successfully");

            var urlList = UrlRepository.getEntities();
            var id = urlList.get(0).getId();

            var response2 = client.post("/urls/" + id + "/checks");
            var responseBody2 = response2.body().string();
            assertThat(responseBody2).contains(correctServerUrl);
            assertThat(responseBody2).contains("Page was checked successfully");
            assertThat(responseBody2).contains("Хекслет");
            assertThat(responseBody2).contains("Живое онлайн сообщество");
            assertThat(responseBody2).contains("Это заголовок h1");
        });
    }
}
