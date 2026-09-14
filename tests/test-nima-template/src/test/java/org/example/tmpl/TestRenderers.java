package org.example.tmpl;

import com.digitalmerce.marble.MarbleEngine;
import com.digitalmerce.marble.loader.ClasspathLoader;
import io.avaje.http.marble.nima.TemplateRender;

/** Test helper: a real Marble engine over a classpath template prefix. */
final class TestRenderers {

    private TestRenderers() {}

    static final String EXPECTED_THEME_PAGE = "<!DOCTYPE html><html><head><title>Products | Acme</title></head>"
            + "<body><header><nav>Acme</nav></header>"
            + "<main><h1>Products</h1><ul><li>Apple</li><li>Banana</li></ul></main>"
            + "<footer>Acme</footer></body></html>";

    static TemplateRender marbleRender(String prefix) {
        final var loader = new ClasspathLoader();
        loader.setPrefix(prefix);
        loader.setSuffix(".mar");
        final MarbleEngine engine = MarbleEngine.builder().loader(loader).build();
        return new TestMarbleTemplateRender(() -> engine);
    }
}
