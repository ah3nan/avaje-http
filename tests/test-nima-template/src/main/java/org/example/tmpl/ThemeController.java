package org.example.tmpl;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.marble.nima.TemplateInstance;
import io.avaje.http.marble.nima.TemplateRender;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.Map;

/** A layout/header/footer/body example: {@code page} extends {@code layout}. */
@Controller("/theme")
public class ThemeController {

    @Inject
    @Named("storefront")
    TemplateRender renderer;

    @Get("/page")
    TemplateInstance page() {
        final Map<String, Object> store = Map.of("name", "Acme");
        final List<Map<String, Object>> products = List.of(Map.of("name", "Apple"), Map.of("name", "Banana"));
        return TemplateInstance.of("page")
                .data("title", "Products")
                .data("store", store)
                .data("products", products);
    }

    @Get("/fragment")
    TemplateInstance fragment() {
        return TemplateInstance.of("fragment").block("body").data("title", "Products");
    }
}
