package org.example.tmpl;

import io.avaje.jsonb.Json;

/** A simple JSON entity used by {@link ResponseController}. */
@Json
public record Product(long id, String name) {}
