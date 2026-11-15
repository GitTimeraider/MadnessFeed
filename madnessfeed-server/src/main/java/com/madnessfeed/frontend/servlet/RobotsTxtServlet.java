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
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Serves robots.txt. In order of precedence: the content of madnessfeed.robots-txt-file,
 * madnessfeed.robots-txt, or a file disallowing everything when madnessfeed.hide-from-web-crawlers
 * is enabled. Otherwise, 404.
 */
@Slf4j
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
        return content()
                .map(c -> Response.ok(c).build())
                .orElseGet(() -> Response.status(HttpStatus.SC_NOT_FOUND).build());
    }

    private Optional<String> content() {
        Optional<String> fromFile =
                config.robotsTxtFile().filter(StringUtils::isNotBlank).flatMap(this::readFile);
        if (fromFile.isPresent()) {
            return fromFile;
        }

        Optional<String> custom =
                config.robotsTxt().filter(StringUtils::isNotBlank).map(c -> c.replace("\\n", "\n"));
        if (custom.isPresent()) {
            return custom;
        }

        return config.hideFromWebCrawlers() ? Optional.of(DISALLOW_ALL) : Optional.empty();
    }

    private Optional<String> readFile(String file) {
        try {
            return Optional.of(Files.readString(Paths.get(file), StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            log.warn("could not read robots.txt file {}: {}", file, e.getMessage());
            return Optional.empty();
        }
    }
}
