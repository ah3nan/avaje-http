package org.example.tmpl;

import static org.assertj.core.api.Assertions.assertThat;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TemplateRuntimeTest {

    static WebServer server;
    static HttpClient client;

    @BeforeAll
    static void start() {
        final var routing = HttpRouting.builder();

        final var sf = new StorefrontController();
        sf.renderer = new StorefrontRender();
        routing.addFeature(new StorefrontController$Route(sf));

        final var manage = new ManageController();
        manage.renderer = new ManageRender();
        routing.addFeature(new ManageController$Route(manage));

        final var requestFactory = new RequestScopedController$RequestFactory();
        requestFactory.renderer = new StorefrontRender();
        routing.addFeature(new RequestScopedController$Route(requestFactory));

        server = WebServer.builder().routing(routing).port(0).build().start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stop() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void storefrontUsesDefaultRenderPath() throws Exception {
        final var res = get("/sf");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body()).isEqualTo("storefront:home");
        assertThat(res.headers().firstValue("Content-Type").orElse("")).contains("text/html");
    }

    @Test
    void manageUsesRequestAwareRenderPath() throws Exception {
        final var res = get("/admin");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body()).isEqualTo("manage:dash");
    }

    @Test
    void requestScopedControllerCapturedOnce() throws Exception {
        final var res = get("/rs");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body()).isEqualTo("storefront:rs");
    }

    private static HttpResponse<String> get(String path) throws Exception {
        final var req = HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path))
                .GET()
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
