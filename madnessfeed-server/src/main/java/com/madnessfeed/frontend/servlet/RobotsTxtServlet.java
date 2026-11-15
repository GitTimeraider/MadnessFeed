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

import java.util.Optional;

/**
 * Serves robots.txt. In order of precedence: the content of madnessfeed.robots-txt-file,
 * madnessfeed.robots-txt, or a file disallowing everything when madnessfeed.hide-from-web-crawlers
 * is enabled. Otherwise, 404.
 */
@Path("/robots.txt")
@PermitAll
@Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
@RequiredArgsConstructor
@Singleton
public class RobotsTxtServlet {

    static final String DISALLOW_ALL = "User-agent: *\nDisallow: /";

    private final MadnessFeedConfiguration config;

    @GET
    @Operation(hidden = true)
    public Response get() {
        return ConfigurableTextFile.resolve(config.robotsTxtFile(), config.robotsTxt())
                .or(() ->
                        config.hideFromWebCrawlers() ? Optional.of(DISALLOW_ALL) : Optional.empty())
                .map(c -> Response.ok(c).build())
                .orElseGet(() -> Response.status(HttpStatus.SC_NOT_FOUND).build());
    }
}
