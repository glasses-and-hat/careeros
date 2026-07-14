package com.careeros.job.infrastructure.ingestion.support;

import java.util.Locale;

/**
 * Fallback "looks remote" detection for ATSes that don't expose an explicit
 * remote flag (Greenhouse, Workday) — a case-insensitive "remote" substring
 * check across whichever text fields are available.
 */
public final class RemoteHeuristic {

    private RemoteHeuristic() {
    }

    public static boolean looksRemote(String... texts) {
        for (String text : texts) {
            if (text != null && text.toLowerCase(Locale.ROOT).contains("remote")) {
                return true;
            }
        }
        return false;
    }
}
