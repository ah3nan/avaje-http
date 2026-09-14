package org.example.tmpl;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.template.nima.TemplateRender;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Controller("/admin")
public class ManageController {

    @Inject
    @Named("manage")
    TemplateRender renderer;

    @Get
    TestView dashboard() {
        return new TestView("dash");
    }
}
