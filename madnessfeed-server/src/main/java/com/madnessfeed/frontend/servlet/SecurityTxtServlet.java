package com.madnessfeed.frontend.servlet;

import com.madnessfeed.MadnessFeedConfiguration;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

import org.apache.hc.core5.http.HttpStatus;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Serves security.txt (RFC 9116) at /.well-known/security.txt. In order of precedence: the content
 * of madnessfeed.security-txt-file or madnessfeed.security-txt. Otherwise, 404.
 */
@Path("/.well-known/security.txt")
@PermitAll
@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
@RequiredArgsConstructor
@Singleton
public class SecurityTxtServlet {

    private final MadnessFeedConfiguration config;

    @GET
    @Operation(hidden = true)
    public Response get() {
        return ConfigurableTextFile.resolve(config.securityTxtFile(), config.securityTxt())
                .map(c -> Response.ok(c).build())
                .orElseGet(() -> Response.status(HttpStatus.SC_NOT_FOUND).build());
    }
}
