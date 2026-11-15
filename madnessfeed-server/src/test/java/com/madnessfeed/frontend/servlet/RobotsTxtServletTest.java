package com.madnessfeed.frontend.servlet;

import com.madnessfeed.MadnessFeedConfiguration;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

class RobotsTxtServletTest {

    private MadnessFeedConfiguration config;
    private RobotsTxtServlet servlet;

    @BeforeEach
    void init() {
        config = Mockito.mock(MadnessFeedConfiguration.class);
        Mockito.when(config.robotsTxt()).thenReturn(Optional.empty());
        Mockito.when(config.robotsTxtFile()).thenReturn(Optional.empty());
        Mockito.when(config.hideFromWebCrawlers()).thenReturn(true);
        servlet = new RobotsTxtServlet(config);
    }

    @Test
    void disallowAllByDefault() {
        assertContent(RobotsTxtServlet.DISALLOW_ALL);
    }

    @Test
    void notFoundWhenDisabled() {
        Mockito.when(config.hideFromWebCrawlers()).thenReturn(false);
        Assertions.assertEquals(404, servlet.get().getStatus());
    }

    @Test
    void customContentWithEscapedNewlines() {
        Mockito.when(config.hideFromWebCrawlers()).thenReturn(false);
        Mockito.when(config.robotsTxt())
                .thenReturn(Optional.of("User-agent: *\\nDisallow: /\\nAllow: /public/"));
        assertContent("User-agent: *\nDisallow: /\nAllow: /public/");
    }

    @Test
    void blankCustomContentIsIgnored() {
        Mockito.when(config.robotsTxt()).thenReturn(Optional.of("  "));
        assertContent(RobotsTxtServlet.DISALLOW_ALL);
    }

    @Test
    void fileTakesPrecedence(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("robots.txt");
        Files.writeString(file, "User-agent: *\nAllow: /\n", StandardCharsets.UTF_8);
        Mockito.when(config.robotsTxt())
                .thenReturn(Optional.of("User-agent: *\\nDisallow: /admin"));
        Mockito.when(config.robotsTxtFile()).thenReturn(Optional.of(file.toString()));
        assertContent("User-agent: *\nAllow: /\n");

        // changes are picked up without a restart
        Files.writeString(file, "User-agent: *\nDisallow: /private\n", StandardCharsets.UTF_8);
        assertContent("User-agent: *\nDisallow: /private\n");
    }

    @Test
    void missingFileFallsBack(@TempDir Path dir) {
        Mockito.when(config.robotsTxt())
                .thenReturn(Optional.of("User-agent: *\\nDisallow: /admin"));
        Mockito.when(config.robotsTxtFile())
                .thenReturn(Optional.of(dir.resolve("missing.txt").toString()));
        assertContent("User-agent: *\nDisallow: /admin");
    }

    private void assertContent(String expected) {
        Response response = servlet.get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(expected, response.getEntity());
    }
}
