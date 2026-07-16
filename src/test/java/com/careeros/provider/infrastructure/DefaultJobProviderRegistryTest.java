package com.careeros.provider.infrastructure;

import com.careeros.company.domain.Company;import com.careeros.job.application.JobPostingCommand;import com.careeros.provider.application.JobProvider;import com.careeros.provider.domain.ProviderType;import org.junit.jupiter.api.Test;import java.util.*;import static org.assertj.core.api.Assertions.*;
class DefaultJobProviderRegistryTest {
 @Test void resolvesProviderWithoutBranching(){JobProvider p=provider(ProviderType.GENERIC_JSON);var registry=new DefaultJobProviderRegistry(List.of(p));assertThat(registry.resolve(ProviderType.GENERIC_JSON)).contains(p);assertThat(registry.resolve(ProviderType.GENERIC_HTML)).isEmpty();}
 @Test void rejectsDuplicateRegistrations(){assertThatThrownBy(()->new DefaultJobProviderRegistry(List.of(provider(ProviderType.GENERIC_JSON),provider(ProviderType.GENERIC_JSON)))).isInstanceOf(IllegalStateException.class);}
 private JobProvider provider(ProviderType t){return new JobProvider(){public ProviderType providerType(){return t;}public List<JobPostingCommand>fetchJobs(Company c){return List.of();}};}
}
