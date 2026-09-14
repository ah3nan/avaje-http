package org.example.tmpl;

import com.digitalmerce.marble.MarbleEngine;
import com.digitalmerce.marble.template.MarbleTemplate;
import io.avaje.http.marble.nima.TemplateInstance;
import io.avaje.http.marble.nima.TemplateRender;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Test-only Marble-backed {@link TemplateRender}.
 *
 * <p>The production implementation lives in the amber framework; the fork keeps only the
 * SPI. This test renderer lets the fork's tests run against a real Marble engine.
 */
final class TestMarbleTemplateRender implements TemplateRender {

    private final Supplier<MarbleEngine> engines;
    private final Locale locale;

    TestMarbleTemplateRender(Supplier<MarbleEngine> engines) {
        this(engines, Locale.getDefault());
    }

    TestMarbleTemplateRender(Supplier<MarbleEngine> engines, Locale locale) {
        this.engines = engines;
        this.locale = locale;
    }

    @Override
    public String render(Object viewModel) {
        final var view = (TemplateInstance) viewModel;
        final var template = template(view);
        if (view.block() != null) {
            final var writer = new StringWriter();
            try {
                template.evaluateBlock(view.block(), writer, view.model(), locale);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return writer.toString();
        }
        return template.render(view.model());
    }

    @Override
    public void render(ServerRequest req, ServerResponse res, Object viewModel) {
        final var view = (TemplateInstance) viewModel;
        final var template = template(view);
        if (view.block() != null) {
            final var writer = new StringWriter();
            try {
                template.evaluateBlock(view.block(), writer, view.model(), locale);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            res.send(writer.toString());
            return;
        }
        try {
            template.renderTo(res.outputStream(), view.model());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private MarbleTemplate template(TemplateInstance view) {
        return engines.get().getTemplate(view.template());
    }
}
