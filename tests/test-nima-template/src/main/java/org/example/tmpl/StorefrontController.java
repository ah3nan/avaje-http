package org.example.tmpl;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.template.nima.TemplateRender;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Controller("/sf")
public class StorefrontController {

    @Inject
    @Named("storefront")
    TemplateRender renderer;

    @Get
    TestView home() {
        return new TestView("home");
    }
}
