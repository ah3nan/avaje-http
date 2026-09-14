package org.example.tmpl;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.http.marble.nima.TemplateRender;
import io.avaje.jsonb.Jsonb;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResponseRuntimeTest {

  static WebServer server;
  static HttpClient client;

  @BeforeAll
  static void start() {
    final TemplateRender storefront = TestRenderers.marbleRender("templates/storefront");
    final Jsonb jsonb = Jsonb.builder().build();

    final var routing = HttpRouting.builder();
    final var controller = new ResponseController();
    controller.renderer = storefront;
    routing.addFeature(new ResponseController$Route(controller, jsonb));

    server = WebServer.builder().routing(routing).port(0).build().start();
    client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
  }

  @AfterAll
  static void stop() {
    if (server != null) {
      server.stop();
    }
  }

  @Test
  void okSerializesPojoAsJson() throws Exception {
    final var res = get("/resp/ok");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(contentType(res)).contains("application/json");
    assertThat(res.body()).contains("\"id\":1").contains("\"name\":\"Apple\"");
  }

  @Test
  void statusAndHeadersAreApplied() throws Exception {
    final var res = get("/resp/created");
    assertThat(res.statusCode()).isEqualTo(201);
    assertThat(res.headers().firstValue("X-Trace")).contains("abc");
    assertThat(res.headers().allValues("X-Multi")).containsExactly("one", "two");
    assertThat(res.body()).contains("\"id\":2").contains("\"name\":\"Banana\"");
  }

  @Test
  void notFoundWithEntity() throws Exception {
    final var res = get("/resp/notfound");
    assertThat(res.statusCode()).isEqualTo(404);
    assertThat(res.body()).contains("\"id\":9").contains("\"name\":\"Gone\"");
  }

  @Test
  void redirectSendsLocationAndNoBody() throws Exception {
    final var res = get("/resp/redirect");
    assertThat(res.statusCode()).isEqualTo(303);
    assertThat(res.headers().firstValue("Location")).contains("/resp/target");
    assertThat(res.body()).isEmpty();
  }

  @Test
  void noContentHasNoBody() throws Exception {
    final var res = get("/resp/nocontent");
    assertThat(res.statusCode()).isEqualTo(204);
    assertThat(res.body()).isEmpty();
  }

  @Test
  void cookieIsSet() throws Exception {
    final var res = get("/resp/cookie");
    assertThat(res.statusCode()).isEqualTo(204);
    final var setCookie = res.headers().firstValue("Set-Cookie").orElse("");
    assertThat(setCookie).contains("session=tok").contains("Path=/").containsIgnoringCase("HttpOnly");
  }

  @Test
  void stringEntityIsTextPlain() throws Exception {
    final var res = get("/resp/text");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(contentType(res)).contains("text/plain");
    assertThat(res.body()).isEqualTo("hello text");
  }

  @Test
  void byteArrayEntity() throws Exception {
    final var res = get("/resp/bytes");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(contentType(res)).contains("application/octet-stream");
    assertThat(res.body()).isEqualTo("raw-bytes");
  }

  @Test
  void inputStreamEntity() throws Exception {
    final var res = get("/resp/stream");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("stream-body");
  }

  @Test
  void streamingOutputEntity() throws Exception {
    final var res = get("/resp/streaming");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("streaming-body");
  }

  @Test
  void explicitMediaTypeOverridesDefault() throws Exception {
    final var res = get("/resp/typed");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(contentType(res)).contains("text/csv");
    assertThat(res.body()).isEqualTo("a,b,c");
  }

  @Test
  void templateEntityRendersThroughInjectedRenderer() throws Exception {
    final var res = get("/resp/view");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(contentType(res)).contains("text/html");
    assertThat(res.headers().firstValue("X-View")).contains("yes");
    assertThat(res.body().strip()).isEqualTo("storefront:resp");
  }

  @Test
  void pathParamEntityFound() throws Exception {
    final var res = get("/resp/item/1");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("\"name\":\"Apple\"");
  }

  @Test
  void pathParamEntityNotFound() throws Exception {
    final var res = get("/resp/item/2");
    assertThat(res.statusCode()).isEqualTo(404);
    assertThat(res.headers().firstValue("X-Reason")).contains("missing");
    assertThat(res.body()).isEmpty();
  }

  @Test
  void genericListEntitySerializesAsJsonArray() throws Exception {
    final var res = get("/resp/list");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(contentType(res)).contains("application/json");
    assertThat(res.body()).contains("\"name\":\"Apple\"").contains("\"name\":\"Banana\"");
  }

  @Test
  void producesAnnotationOverridesEntityDefault() throws Exception {
    final var res = get("/resp/xml");
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(contentType(res)).contains("application/xml");
    assertThat(res.body()).isEqualTo("<product><name>Apple</name></product>");
  }

  private static String contentType(HttpResponse<String> res) {
    return res.headers().firstValue("Content-Type").orElse("");
  }

  @Test
  void goBackRedirectsToReferer() throws Exception {
    final var res = getWithReferer("/resp/back", "https://example.com/previous");
    assertThat(res.statusCode()).isEqualTo(303);
    assertThat(res.headers().firstValue("Location")).contains("https://example.com/previous");
  }

  @Test
  void goBackFallsBackToRootWhenNoReferer() throws Exception {
    final var res = get("/resp/back");
    assertThat(res.statusCode()).isEqualTo(303);
    assertThat(res.headers().firstValue("Location")).contains("/");
  }

  private static HttpResponse<String> get(String path) throws Exception {
    final var req = HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path))
        .GET()
        .build();
    return client.send(req, HttpResponse.BodyHandlers.ofString());
  }

  private static HttpResponse<String> getWithReferer(String path, String referer) throws Exception {
    final var req =
        HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path))
            .header("Referer", referer)
            .GET()
            .build();
    return client.send(req, HttpResponse.BodyHandlers.ofString());
  }
}
