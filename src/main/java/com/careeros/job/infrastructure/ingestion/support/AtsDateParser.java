package com.careeros.job.infrastructure.ingestion.support;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Best-effort conversion of the various date shapes ATS APIs return into
 * {@link LocalDate}, in UTC to match {@code hibernate.jdbc.time_zone: UTC}.
 * A bad or missing date shouldn't sink an otherwise-valid posting, so these
 * return {@code null} instead of throwing.
 */
public final class AtsDateParser {

    private AtsDateParser() {
    }

    public static LocalDate fromIsoInstant(String isoInstant) {
        if (isoInstant == null || isoInstant.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(isoInstant).atZone(ZoneOffset.UTC).toLocalDate();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDate fromEpochMillis(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        try {
            return Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate();
        } catch (DateTimeException e) {
            return null;
        }
    }
}
