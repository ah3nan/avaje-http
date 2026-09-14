package org.example.tmpl;

import static org.assertj.core.api.Assertions.assertThat;

import io.avaje.http.marble.nima.TemplateInstance;
import org.junit.jupiter.api.Test;

class MarbleTemplateRenderTest {

    @Test
    void rendersTemplateInstanceToString() {
        final var render = TestRenderers.marbleRender("templates/storefront");
        final var view = TemplateInstance.of("home").data("name", "x");

        assertThat(render.render(view).strip()).isEqualTo("storefront:x");
    }

    @Test
    void rendersBlockFragment() {
        final var render = TestRenderers.marbleRender("templates/storefront");
        final var view = TemplateInstance.of("frag").block("summary").data("name", "y");

        assertThat(render.render(view).strip()).isEqualTo("SUMMARY:y");
    }
}
