package com.madnessfeed.frontend.servlet;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Resolves the content of a text file that can be customized with a configuration property pointing
 * to a file, or with a configuration property containing the content itself.
 */
@Slf4j
@UtilityClass
class ConfigurableTextFile {

    /**
     * @param file path to a file, read on every call. Takes precedence over content. Ignored if it
     *     can't be read.
     * @param content the content itself, with \n (a backslash followed by n) as line separator
     * @return the content of the file, or the content, or empty if none is set
     */
    Optional<String> resolve(Optional<String> file, Optional<String> content) {
        Optional<String> fromFile =
                file.filter(StringUtils::isNotBlank).flatMap(ConfigurableTextFile::readFile);
        if (fromFile.isPresent()) {
            return fromFile;
        }

        return content.filter(StringUtils::isNotBlank).map(c -> c.replace("\\n", "\n"));
    }

    private Optional<String> readFile(String file) {
        try {
            return Optional.of(Files.readString(Paths.get(file), StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            log.warn("could not read file {}: {}", file, e.getMessage());
            return Optional.empty();
        }
    }
}
