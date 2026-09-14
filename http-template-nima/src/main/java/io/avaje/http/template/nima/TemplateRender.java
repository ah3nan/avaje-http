package io.avaje.http.template.nima;

import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Renders the view model returned by an avaje-http controller method whose return
 * type implements {@link io.avaje.http.api.template.TemplateView}.
 *
 * <p>A controller selects its {@code TemplateRender} bean with ordinary dependency
 * injection (e.g. {@code @Inject @Named("storefront") TemplateRender renderer;}); the
 * generated route invokes {@link #render(ServerRequest, ServerResponse, Object)} for
 * the returned view.
 */
public interface TemplateRender {

  /**
   * Render the given view model to a String. This is always supported and is the safe
   * default.
   */
  String render(Object viewModel);

  /**
   * Render the given view model directly to the server response, optionally streaming.
   *
   * <p>Implementations that can stream should override this to write to
   * {@code res.outputStream()} (for example Marble's {@code renderTo}). The default
   * implementation renders to a String and sends it.
   */
  default void render(ServerRequest req, ServerResponse res, Object viewModel) {
    res.send(render(viewModel));
  }
}
