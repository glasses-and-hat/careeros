package com.careeros.matching.domain;

import com.careeros.job.domain.JobPosting;
import java.util.List;

public record JobMatch(JobPosting job, int overallScore, int roleScore, int technologyScore,
                       int locationScore, int remoteScore, int companyPriorityScore, int recencyScore,
                       List<String> matchExplanation) {}
