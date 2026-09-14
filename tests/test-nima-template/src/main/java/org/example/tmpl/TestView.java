package org.example.tmpl;

import io.avaje.http.api.template.TemplateView;

public record TestView(String name) implements TemplateView {}
