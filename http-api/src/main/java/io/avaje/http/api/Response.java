package io.avaje.http.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A Jakarta-EE style response envelope.
 *
 * <p>A controller method returning {@code Response<T>} lets the generated route control the
 * HTTP status, headers, cookies and media type, with {@code T} as the entity body. The entity
 * may be any supported return kind: a POJO (serialized as JSON), {@code String}, {@code byte[]},
 * {@code InputStream}, {@code StreamingOutput} or a
 * {@link io.avaje.http.api.template.TemplateView} (rendered through the controller's injected
 * template renderer).
 *
 * <pre>{@code
 * @Get("/{id}")
 * Response<Product> get(int id) {
 *   return catalog.find(id)
 *       .map(Response::ok)
 *       .orElseGet(() -> Response.status(404).build());
 * }
 *
 * @Get("/back")
 * Response<Void> back() {
 *   return Response.goBack();
 * }
 * }</pre>
 *
 * @param <T> the entity type
 */
public final class Response<T> {

  private final int status;
  private final T entity;
  private final String mediaType;
  private final Map<String, List<String>> headers;
  private final List<NewCookie> cookies;
  private final boolean refererRedirect;

  private Response(
      int status,
      T entity,
      String mediaType,
      Map<String, List<String>> headers,
      List<NewCookie> cookies,
      boolean refererRedirect) {
    this.status = status;
    this.entity = entity;
    this.mediaType = mediaType;
    this.headers = headers;
    this.cookies = cookies;
    this.refererRedirect = refererRedirect;
  }

  /** The HTTP status code. */
  public int status() {
    return status;
  }

  /** The entity body, or {@code null}. */
  public T entity() {
    return entity;
  }

  /** True when an entity body is present. */
  public boolean hasEntity() {
    return entity != null;
  }

  /** The explicit media type, or {@code null} to use the entity default. */
  public String mediaType() {
    return mediaType;
  }

  /** The response headers (values may repeat). */
  public Map<String, List<String>> headers() {
    return headers;
  }

  /** The cookies to set. */
  public List<NewCookie> cookies() {
    return cookies;
  }

  /** True when this response redirects to the request {@code Referer} (see {@link #goBack()}). */
  public boolean isRefererRedirect() {
    return refererRedirect;
  }

  /** A {@code 200 OK} response with the given entity. */
  public static <T> Response<T> ok(T entity) {
    return new Builder<T>(200).entity(entity).build();
  }

  /** A response with the given status and entity. */
  public static <T> Response<T> status(int status, T entity) {
    return new Builder<T>(status).entity(entity).build();
  }

  /** A builder for a response with the given status. */
  public static <T> Builder<T> status(int status) {
    return new Builder<>(status);
  }

  /** A {@code 204 No Content} response. */
  public static Response<Void> noContent() {
    return new Builder<Void>(204).build();
  }

  /** A {@code 304 Not Modified} response. */
  public static Response<Void> notModified() {
    return new Builder<Void>(304).build();
  }

  /** A {@code 201 Created} response with a {@code Location} header. */
  public static Builder<Void> created(URI location) {
    return new Builder<Void>(201).location(location);
  }

  /** A {@code 301 Moved Permanently} redirect. */
  public static Builder<Void> movedPermanently(URI location) {
    return new Builder<Void>(301).location(location);
  }

  /** A {@code 302 Found} redirect. */
  public static Builder<Void> found(URI location) {
    return new Builder<Void>(302).location(location);
  }

  /** A {@code 303 See Other} redirect. */
  public static Builder<Void> seeOther(URI location) {
    return new Builder<Void>(303).location(location);
  }

  /** A {@code 307 Temporary Redirect} redirect. */
  public static Builder<Void> temporaryRedirect(URI location) {
    return new Builder<Void>(307).location(location);
  }

  /** A {@code 308 Permanent Redirect} redirect. */
  public static Builder<Void> permanentRedirect(URI location) {
    return new Builder<Void>(308).location(location);
  }

  /**
   * Redirect ({@code 303 See Other}) to the request {@code Referer}, falling back to {@code /}
   * when the request has no {@code Referer} header.
   *
   * <pre>{@code
   * @Get("/back")
   * Response<Void> back() {
   *   return Response.goBack();
   * }
   * }</pre>
   */
  public static Response<Void> goBack() {
    return new Builder<Void>(303).markRefererRedirect().build();
  }

  /** Redirect ({@code 303 See Other}) to the given referer, or to {@code /} when absent. */
  public static Response<Void> goBack(Optional<URI> referer) {
    return goBack(referer, URI.create("/"));
  }

  /** Redirect ({@code 303 See Other}) to the given referer, or to the fallback when absent. */
  public static Response<Void> goBack(Optional<URI> referer, URI fallback) {
    return seeOther(referer.isPresent() ? referer.get() : fallback).build();
  }

  /** Redirect ({@code 303 See Other}) to the given location. */
  public static Response<Void> goBack(URI referer) {
    return seeOther(referer).build();
  }

  /**
   * Mutable builder for a {@link Response}.
   *
   * @param <T> the entity type
   */
  public static final class Builder<T> {

    private int status;
    private T entity;
    private String mediaType;
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private final List<NewCookie> cookies = new ArrayList<>();
    private boolean refererRedirect;

    private Builder(int status) {
      this.status = status;
    }

    private Builder<T> markRefererRedirect() {
      this.refererRedirect = true;
      return this;
    }

    /** Set the entity, re-typing the builder. */
    @SuppressWarnings("unchecked")
    public <R> Builder<R> entity(R entity) {
      final var self = (Builder<R>) this;
      self.entity = entity;
      return self;
    }

    /** Override the status code. */
    public Builder<T> status(int status) {
      this.status = status;
      return this;
    }

    /** Set an explicit media type (overrides the entity default). */
    public Builder<T> type(String mediaType) {
      this.mediaType = mediaType;
      return this;
    }

    /** Add a response header. May be called repeatedly for the same name. */
    public Builder<T> header(String name, String value) {
      headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
      return this;
    }

    /** Set the {@code Location} header. */
    public Builder<T> location(URI location) {
      return header("Location", location.toString());
    }

    /** Add a cookie. */
    public Builder<T> cookie(NewCookie cookie) {
      cookies.add(cookie);
      return this;
    }

    /** Add a cookie. */
    public Builder<T> cookie(String name, String value) {
      return cookie(NewCookie.of(name, value));
    }

    /** Build the immutable response. */
    public Response<T> build() {
      final Map<String, List<String>> copy = new LinkedHashMap<>();
      headers.forEach((k, v) -> copy.put(k, List.copyOf(v)));
      return new Response<>(
          status, entity, mediaType, Map.copyOf(copy), List.copyOf(cookies), refererRedirect);
    }
  }
}
