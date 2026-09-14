package org.example.tmpl;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.http.marble.nima.TemplateRender;
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
        final TemplateRender storefront = TestRenderers.marbleRender("templates/storefront");
        final TemplateRender manage = TestRenderers.marbleRender("templates/manage");
        final TemplateRender theme = TestRenderers.marbleRender("templates/theme");

        final var routing = HttpRouting.builder();

        final var sf = new StorefrontController();
        sf.renderer = storefront;
        routing.addFeature(new StorefrontController$Route(sf));

        final var manageController = new ManageController();
        manageController.renderer = manage;
        routing.addFeature(new ManageController$Route(manageController));

        final var requestFactory = new RequestScopedController$RequestFactory();
        requestFactory.renderer = storefront;
        routing.addFeature(new RequestScopedController$Route(requestFactory));

        final var themeController = new ThemeController();
        themeController.renderer = theme;
        routing.addFeature(new ThemeController$Route(themeController));

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
    void storefrontRendersWholeTemplateWithContentType() throws Exception {
        final var res = get("/sf");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body().strip()).isEqualTo("storefront:home");
        assertThat(res.headers().firstValue("Content-Type").orElse("")).contains("text/html");
    }

    @Test
    void storefrontRendersBlockFragment() throws Exception {
        final var res = get("/sf/frag");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body().strip()).isEqualTo("SUMMARY:frag");
    }

    @Test
    void manageUsesItsOwnEngine() throws Exception {
        final var res = get("/admin");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body().strip()).isEqualTo("manage:dash");
    }

    @Test
    void requestScopedControllerCapturedOnce() throws Exception {
        final var res = get("/rs");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body().strip()).isEqualTo("storefront:rs");
    }

    @Test
    void themeLayoutPageWithHeaderFooterAndBody() throws Exception {
        final var res = get("/theme/page");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body()).isEqualTo(TestRenderers.EXPECTED_THEME_PAGE);
    }

    @Test
    void themeFragment() throws Exception {
        final var res = get("/theme/fragment");
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body()).isEqualTo("BODY:Products");
    }

    private static HttpResponse<String> get(String path) throws Exception {
        final var req = HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path))
                .GET()
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
