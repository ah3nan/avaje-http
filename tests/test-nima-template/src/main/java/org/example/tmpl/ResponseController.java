package org.example.tmpl;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.NewCookie;
import io.avaje.http.api.Produces;
import io.avaje.http.api.Response;
import io.avaje.http.api.StreamingOutput;
import io.avaje.http.marble.nima.TemplateInstance;
import io.avaje.http.marble.nima.TemplateRender;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Exercises every {@link Response} entity kind and status/header/cookie control. */
@Controller("/resp")
public class ResponseController {

  @Inject
  @Named("storefront")
  TemplateRender renderer;

  @Get("/ok")
  Response<Product> ok() {
    return Response.ok(new Product(1, "Apple"));
  }

  @Get("/created")
  Response<Product> created() {
    return Response.status(201)
        .entity(new Product(2, "Banana"))
        .header("X-Trace", "abc")
        .header("X-Multi", "one")
        .header("X-Multi", "two")
        .build();
  }

  @Get("/notfound")
  Response<Product> notFound() {
    return Response.status(404).entity(new Product(9, "Gone")).build();
  }

  @Get("/redirect")
  Response<Void> redirect() {
    return Response.seeOther(URI.create("/resp/target")).build();
  }

  @Get("/back")
  Response<Void> back() {
    return Response.goBack();
  }

  @Get("/nocontent")
  Response<Void> noContent() {
    return Response.noContent();
  }

  @Get("/cookie")
  Response<Void> cookie() {
    return Response.<Void>status(204)
        .cookie(NewCookie.builder("session", "tok")
            .path("/")
            .httpOnly(true)
            .build())
        .build();
  }

  @Get("/text")
  Response<String> text() {
    return Response.ok("hello text");
  }

  @Get("/bytes")
  Response<byte[]> bytes() {
    return Response.ok("raw-bytes".getBytes(StandardCharsets.UTF_8));
  }

  @Get("/stream")
  Response<InputStream> stream() {
    return Response.ok(new ByteArrayInputStream("stream-body".getBytes(StandardCharsets.UTF_8)));
  }

  @Get("/streaming")
  Response<StreamingOutput> streaming() {
    return Response.ok(os -> os.write("streaming-body".getBytes(StandardCharsets.UTF_8)));
  }

  @Get("/typed")
  Response<String> typed() {
    return Response.status(200).type("text/csv").entity("a,b,c").build();
  }

  @Get("/view")
  Response<TemplateInstance> view() {
    return Response.status(200)
        .entity(TemplateInstance.of("home").data("name", "resp"))
        .header("X-View", "yes")
        .build();
  }

  @Get("/item/{id}")
  Response<Product> item(int id) {
    if (id == 1) {
      return Response.ok(new Product(1, "Apple"));
    }
    return Response.<Product>status(404).header("X-Reason", "missing").build();
  }

  @Get("/list")
  Response<List<Product>> list() {
    return Response.ok(List.of(new Product(1, "Apple"), new Product(2, "Banana")));
  }

  @Produces("application/xml")
  @Get("/xml")
  Response<String> xml() {
    return Response.ok("<product><name>Apple</name></product>");
  }
}
