package com.careeros.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static String read(String classpathLocation) {
        try (InputStream in = TestFixtures.class.getClassLoader().getResourceAsStream(classpathLocation)) {
            if (in == null) {
                throw new IllegalArgumentException("Fixture not found on classpath: " + classpathLocation);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
