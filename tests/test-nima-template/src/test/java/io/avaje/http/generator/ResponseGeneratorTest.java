package io.avaje.http.generator;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.http.generator.core.APContext;
import io.avaje.http.generator.helidon.nima.HelidonProcessor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ResponseGeneratorTest {

  @AfterEach
  void cleanup() throws IOException {
    APContext.clear();
    delete(Paths.get("org"));
    delete(Paths.get("target/testAPI"));
  }

  private static void delete(Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }
    Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  @Test
  void responseReturnsGenerateControlCode() throws Exception {
    compile();

    final var route = Files.readString(Paths.get("org/example/tmpl/ResponseController$Route.java"));

    // status / headers / cookies preamble
    assertThat(route).contains("res.status(result.status());");
    assertThat(route)
        .contains("result.headers().forEach((k, vs) -> vs.forEach(v -> res.headers().add(HeaderNames.create(k),"
            + " v)));");
    assertThat(route).contains("result.cookies().forEach");
    assertThat(route).contains("res.headers().addCookie(cb.build());");

    // POJO entity -> generated JsonType
    assertThat(route).contains("productJsonType.toJson(result.entity(), JsonOutput.of(res));");
    assertThat(route).contains("this.productJsonType = jsonb.type(Product.class);");

    // entity kinds / null entity guard
    assertThat(route).contains("if (result.hasEntity()) {");
    assertThat(route).contains("res.send(result.entity());");
    assertThat(route).contains("in.transferTo(res.outputStream());");
    assertThat(route).contains("result.entity().write(os);");
    assertThat(route).contains("controller.renderer.render(req, res, result.entity());");

    // default media types
    assertThat(route).contains("MediaTypes.APPLICATION_JSON");
    assertThat(route).contains("MediaTypes.TEXT_PLAIN");
    assertThat(route).contains("MediaTypes.APPLICATION_OCTET_STREAM");
    assertThat(route).contains("HTML_UTF8");

    // runtime media type override
    assertThat(route).contains("res.headers().contentType(MediaTypes.create(result.mediaType()));");

    // goBack() resolves the Referer from the live request
    assertThat(route).contains("if (result.isRefererRedirect()) {");
    assertThat(route).contains("req.headers().referer().orElse(java.net.URI.create(\"/\"))");

    // the envelope type itself must never become a JsonType
    assertThat(route).doesNotContain("ResponseJsonType");
  }

  @Test
  void responseReturnUsesStringTestClient() throws Exception {
    compile();

    final var api = Files.readString(Paths.get("org.example.tmpl.ResponseControllerTestAPI.txt"));
    assertThat(api).contains("HttpResponse<String> ok();");
    assertThat(api).contains("HttpResponse<String> redirect();");
    assertThat(api).doesNotContain("HttpResponse<Response<");
  }

  private void compile() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();
    final var files = getSourceFiles(source);
    final var task = ToolProvider.getSystemJavaCompiler()
        .getTask(new PrintWriter(System.out), null, null, List.of("--release=21"), null, files);
    task.setProcessors(List.of(new HelidonProcessor()));
    assertThat(task.call()).isTrue();
  }

  private Iterable<JavaFileObject> getSourceFiles(String source) throws Exception {
    final var compiler = ToolProvider.getSystemJavaCompiler();
    final var files = compiler.getStandardFileManager(null, null, null);
    files.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));
    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
    return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
  }
}
