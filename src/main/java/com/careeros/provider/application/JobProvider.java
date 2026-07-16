package com.careeros.provider.application;

import com.careeros.company.domain.Company;
import com.careeros.job.application.JobPostingCommand;
import com.careeros.provider.domain.ProviderType;
import java.util.List;

public interface JobProvider {
    ProviderType providerType();
    List<JobPostingCommand> fetchJobs(Company company);
}
