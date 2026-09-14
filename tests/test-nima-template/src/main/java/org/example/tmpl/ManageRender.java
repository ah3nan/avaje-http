package org.example.tmpl;

import io.avaje.http.template.nima.TemplateRender;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/** Overrides the request-aware method to prove it is used for the manage engine. */
@Singleton
@Named("manage")
public class ManageRender implements TemplateRender {

    @Override
    public String render(Object viewModel) {
        return "manage-unused";
    }

    @Override
    public void render(ServerRequest req, ServerResponse res, Object viewModel) {
        res.send("manage:" + ((TestView) viewModel).name());
    }
}
