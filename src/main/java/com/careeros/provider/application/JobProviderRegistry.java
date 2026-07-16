package com.careeros.provider.application;

import com.careeros.provider.domain.ProviderType;
import java.util.Optional;

public interface JobProviderRegistry {
    Optional<JobProvider> resolve(ProviderType type);
}
