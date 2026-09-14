package io.avaje.http.api;

import java.time.Duration;

/**
 * A cookie to set on a {@link Response}.
 *
 * <pre>{@code
 * Response.ok(dto).cookie(NewCookie.of("session", token)).build();
 * }</pre>
 */
public final class NewCookie {

  private final String name;
  private final String value;
  private final String path;
  private final String domain;
  private final Duration maxAge;
  private final boolean secure;
  private final boolean httpOnly;

  private NewCookie(
      String name,
      String value,
      String path,
      String domain,
      Duration maxAge,
      boolean secure,
      boolean httpOnly) {
    this.name = name;
    this.value = value;
    this.path = path;
    this.domain = domain;
    this.maxAge = maxAge;
    this.secure = secure;
    this.httpOnly = httpOnly;
  }

  /** Create a simple name/value cookie. */
  public static NewCookie of(String name, String value) {
    return new NewCookie(name, value, null, null, null, false, false);
  }

  /** Create a builder for the given name/value cookie. */
  public static Builder builder(String name, String value) {
    return new Builder(name, value);
  }

  public String name() {
    return name;
  }

  public String value() {
    return value;
  }

  public String path() {
    return path;
  }

  public String domain() {
    return domain;
  }

  public Duration maxAge() {
    return maxAge;
  }

  public boolean secure() {
    return secure;
  }

  public boolean httpOnly() {
    return httpOnly;
  }

  /** Builder for a {@link NewCookie}. */
  public static final class Builder {

    private final String name;
    private final String value;
    private String path;
    private String domain;
    private Duration maxAge;
    private boolean secure;
    private boolean httpOnly;

    private Builder(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public Builder path(String path) {
      this.path = path;
      return this;
    }

    public Builder domain(String domain) {
      this.domain = domain;
      return this;
    }

    public Builder maxAge(Duration maxAge) {
      this.maxAge = maxAge;
      return this;
    }

    public Builder secure(boolean secure) {
      this.secure = secure;
      return this;
    }

    public Builder httpOnly(boolean httpOnly) {
      this.httpOnly = httpOnly;
      return this;
    }

    public NewCookie build() {
      return new NewCookie(name, value, path, domain, maxAge, secure, httpOnly);
    }
  }
}
