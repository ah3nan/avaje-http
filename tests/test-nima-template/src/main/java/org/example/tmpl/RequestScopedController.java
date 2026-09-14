package org.example.tmpl;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.template.nima.TemplateRender;
import io.helidon.webserver.http.ServerRequest;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** The {@code ServerRequest} field makes this controller request-scoped (factory-created). */
@Controller("/rs")
public class RequestScopedController {

    @Inject
    @Named("storefront")
    TemplateRender renderer;

    @Inject
    ServerRequest request;

    @Get
    TestView view() {
        return new TestView("rs");
    }
}
