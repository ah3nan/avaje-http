package io.avaje.http.marble.nima;

import io.avaje.http.api.template.TemplateView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A named template, its model data and an optional fragment (template block).
 *
 * <p>This is the standard view type for a controller method: returning a
 * {@code TemplateInstance} (which is a {@link TemplateView}) makes the generated route
 * render it through the controller's injected {@link TemplateRender}.
 *
 * <pre>{@code
 * @Get("/products/{slug}")
 * TemplateInstance product(String slug) {
 *   return TemplateInstance.of("pages.product.single").data("product", catalog.bySlug(slug));
 * }
 * }</pre>
 */
public final class TemplateInstance implements TemplateView {

  private final String template;
  private final Map<String, Object> model;
  private final String block;

  private TemplateInstance(String template, Map<String, Object> model, String block) {
    this.template = template;
    this.model = model;
    this.block = block;
  }

  /**
   * Create an instance for the given template name with an empty model.
   */
  public static TemplateInstance of(String template) {
    return new TemplateInstance(template, new LinkedHashMap<>(), null);
  }

  /**
   * Create an instance for the given template name with the given model.
   */
  public static TemplateInstance of(String template, Map<String, Object> model) {
    return new TemplateInstance(template, new LinkedHashMap<>(model), null);
  }

  /**
   * Add a model value.
   */
  public TemplateInstance data(String key, Object value) {
    model.put(key, value);
    return this;
  }

  /**
   * Add a model value.
   */
  public TemplateInstance data(Map<String, Object> data) {
    model.putAll(data);
    return this;
  }

  /**
   * Return a copy that renders only the given template block (fragment).
   */
  public TemplateInstance block(String block) {
    return new TemplateInstance(template, new LinkedHashMap<>(model), block);
  }

  /**
   * The logical template name.
   */
  public String template() {
    return template;
  }

  /**
   * The model data.
   */
  public Map<String, Object> model() {
    return model;
  }

  /**
   * The optional template block (fragment) to render instead of the whole template.
   */
  public String block() {
    return block;
  }
}
