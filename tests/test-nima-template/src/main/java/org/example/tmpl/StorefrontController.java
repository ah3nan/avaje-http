package org.example.tmpl;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.marble.nima.TemplateInstance;
import io.avaje.http.marble.nima.TemplateRender;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Controller("/sf")
public class StorefrontController {

    @Inject
    @Named("storefront")
    TemplateRender renderer;

    @Get
    TemplateInstance home() {
        return TemplateInstance.of("home").data("name", "home");
    }

    @Get("/frag")
    TemplateInstance frag() {
        return TemplateInstance.of("frag").block("summary").data("name", "frag");
    }
}
