package org.example.tmpl;

import io.avaje.http.template.nima.TemplateRender;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/** Uses the default request-aware method (which renders to a String and sends it). */
@Singleton
@Named("storefront")
public class StorefrontRender implements TemplateRender {

    @Override
    public String render(Object viewModel) {
        return "storefront:" + ((TestView) viewModel).name();
    }
}
