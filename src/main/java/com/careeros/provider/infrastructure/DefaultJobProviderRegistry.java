package com.careeros.provider.infrastructure;

import com.careeros.provider.application.*;
import com.careeros.provider.domain.ProviderType;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DefaultJobProviderRegistry implements JobProviderRegistry {
    private final Map<ProviderType, JobProvider> providers;
    public DefaultJobProviderRegistry(List<JobProvider> providers) {
        try { this.providers = providers.stream().collect(Collectors.toUnmodifiableMap(JobProvider::providerType, Function.identity())); }
        catch (IllegalStateException e) { throw new IllegalStateException("Duplicate job provider registration", e); }
    }
    public Optional<JobProvider> resolve(ProviderType type) { return Optional.ofNullable(providers.get(type)); }
}
