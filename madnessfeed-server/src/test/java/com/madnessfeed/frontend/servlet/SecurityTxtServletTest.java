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

class SecurityTxtServletTest {

    private MadnessFeedConfiguration config;
    private SecurityTxtServlet servlet;

    @BeforeEach
    void init() {
        config = Mockito.mock(MadnessFeedConfiguration.class);
        Mockito.when(config.securityTxt()).thenReturn(Optional.empty());
        Mockito.when(config.securityTxtFile()).thenReturn(Optional.empty());
        servlet = new SecurityTxtServlet(config);
    }

    @Test
    void notFoundByDefault() {
        Assertions.assertEquals(404, servlet.get().getStatus());
    }

    @Test
    void customContentWithEscapedNewlines() {
        Mockito.when(config.securityTxt())
                .thenReturn(
                        Optional.of(
                                "Contact: mailto:security@example.com\\nExpires: 2027-12-31T23:00:00.000Z"));
        assertContent("Contact: mailto:security@example.com\nExpires: 2027-12-31T23:00:00.000Z");
    }

    @Test
    void fileTakesPrecedence(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("security.txt");
        Files.writeString(file, "Contact: https://example.com/security\n", StandardCharsets.UTF_8);
        Mockito.when(config.securityTxt())
                .thenReturn(Optional.of("Contact: mailto:security@example.com"));
        Mockito.when(config.securityTxtFile()).thenReturn(Optional.of(file.toString()));
        assertContent("Contact: https://example.com/security\n");
    }

    @Test
    void missingFileWithoutContentIsNotFound(@TempDir Path dir) {
        Mockito.when(config.securityTxtFile())
                .thenReturn(Optional.of(dir.resolve("missing.txt").toString()));
        Assertions.assertEquals(404, servlet.get().getStatus());
    }

    private void assertContent(String expected) {
        Response response = servlet.get();
        Assertions.assertEquals(200, response.getStatus());
        Assertions.assertEquals(expected, response.getEntity());
    }
}
