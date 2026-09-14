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

class TemplateGeneratorTest {

    @AfterEach
    void cleanup() throws IOException {
        APContext.clear();
        delete(Paths.get("org"));
    }

    private static void delete(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Test
    void templateReturnsGenerateRendererCall() throws Exception {
        compile();

        final var sf = Files.readString(Paths.get("org/example/tmpl/StorefrontController$Route.java"));
        assertThat(sf).contains("private static final io.helidon.common.media.type.MediaType HTML_UTF8");
        assertThat(sf).contains("controller.renderer.render(req, res, result);");
        assertThat(sf).doesNotContain("private final TemplateRender");

        final var admin = Files.readString(Paths.get("org/example/tmpl/ManageController$Route.java"));
        assertThat(admin).contains("controller.renderer.render(req, res, result);");

        final var rs = Files.readString(Paths.get("org/example/tmpl/RequestScopedController$Route.java"));
        assertThat(rs).contains("var target = factory.create(req, res);");
        assertThat(rs).contains("target.renderer.render(req, res, result);");

        // the request-scoped factory must keep the controller field's DI qualifier
        final var factory = Files.readString(Paths.get("org/example/tmpl/RequestScopedController$RequestFactory.java"));
        assertThat(factory).contains("@jakarta.inject.Named(\"storefront\")");
    }

    @Test
    void templateReturnUsesStringTestClient() throws Exception {
        compile();

        final var api = Files.readString(Paths.get("target/testAPI/org.example.tmpl.StorefrontControllerTestAPI.txt"));
        assertThat(api).contains("HttpResponse<String> home();");
        assertThat(api).contains("HttpResponse<String> frag();");
        assertThat(api).doesNotContain("HttpResponse<TemplateInstance>");
    }

    @Test
    void templateViewDoesNotProduceJsonType() throws Exception {
        // avaje-jsonb is not on this module's classpath, so JsonB is off; the meaningful
        // assertion here is that the view type never becomes a JsonType field.
        compile();

        final var sf = Files.readString(Paths.get("org/example/tmpl/StorefrontController$Route.java"));
        assertThat(sf).doesNotContain("TemplateInstanceJsonType");
        assertThat(sf).doesNotContain("jsonb");
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
