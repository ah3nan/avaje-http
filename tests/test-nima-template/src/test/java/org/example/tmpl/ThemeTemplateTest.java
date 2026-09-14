package org.example.tmpl;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.http.marble.nima.TemplateInstance;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Full layout example: {@code page} extends {@code layout}, includes header/nav/footer/products. */
class ThemeTemplateTest {

    @Test
    void rendersLayoutWithHeaderFooterAndBody() {
        final var render = TestRenderers.marbleRender("templates/theme");
        final var view = TemplateInstance.of("page")
                .data("title", "Products")
                .data("store", Map.of("name", "Acme"))
                .data("products", List.of(Map.of("name", "Apple"), Map.of("name", "Banana")));

        assertThat(render.render(view)).isEqualTo(TestRenderers.EXPECTED_THEME_PAGE);
    }

    @Test
    void rendersOnlyFragmentBlock() {
        final var render = TestRenderers.marbleRender("templates/theme");
        final var view = TemplateInstance.of("fragment").block("body").data("title", "Products");

        assertThat(render.render(view)).isEqualTo("BODY:Products");
    }
}
