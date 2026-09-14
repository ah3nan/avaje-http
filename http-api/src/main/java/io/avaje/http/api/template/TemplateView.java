package io.avaje.http.api.template;

/**
 * Marker for a controller method's return type whose value is a view model to be
 * rendered by a {@code TemplateRender}.
 *
 * <p>Detected by the generator via the interface (the same way
 * {@link io.avaje.http.api.StreamingOutput} is detected), so any controller method
 * returning a {@code TemplateView} is rendered as a template instead of being
 * serialized or sent directly.
 *
 * <pre>{@code
 * @Get("/")
 * TemplateInstance home() {
 *   return TemplateInstance.of("pages.index");
 * }
 * }</pre>
 */
public interface TemplateView {}
