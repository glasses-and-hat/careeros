package com.careeros.provider.infrastructure.generic;

import com.fasterxml.jackson.databind.*;
import java.util.*;

final class ProviderConfiguration {
    private final JsonNode root;
    ProviderConfiguration(ObjectMapper mapper, String json) {
        try { root = mapper.readTree(Objects.requireNonNull(json, "providerConfiguration is required")); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid provider configuration JSON", e); }
    }
    String required(String name) { String v = text(name); if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " is required"); return v; }
    String text(String name) { JsonNode n=root.get(name); return n==null||n.isNull()?null:n.asText(); }
    int integer(String name,int fallback) { JsonNode n=root.get(name); return n==null?fallback:n.asInt(fallback); }
}
