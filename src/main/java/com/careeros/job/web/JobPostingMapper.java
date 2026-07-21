package com.careeros.job.web;

import com.careeros.job.application.JobPostingCommand;
import com.careeros.job.domain.JobPosting;

public final class JobPostingMapper {

    private JobPostingMapper() {
    }

    public static JobPostingResponse toResponse(JobPosting jobPosting) {
        return new JobPostingResponse(
                jobPosting.getId(),
                jobPosting.getExternalId(),
                jobPosting.getCompany().getId(),
                jobPosting.getCompany().getName(),
                jobPosting.getTitle(),
                jobPosting.getLocation(),
                jobPosting.getEmploymentType(),
                jobPosting.isRemote(),
                jobPosting.getSalary().min(),
                jobPosting.getSalary().max(),
                jobPosting.getSalary().currency(),
                jobPosting.getDescription(),
                jobPosting.getPostedDate(),
                jobPosting.getApplyUrl(),
                jobPosting.getSourceType(),
                jobPosting.getSourceUrl(),
                jobPosting.getHash(),
                jobPosting.getCreatedAt(),
                jobPosting.getUpdatedAt());
    }

    static JobPostingCommand toCommand(JobPostingRequest request) {
        return new JobPostingCommand(
                request.externalId(),
                request.companyId(),
                request.title(),
                request.location(),
                request.employmentType(),
                request.remote(),
                request.salaryMin(),
                request.salaryMax(),
                request.salaryCurrency(),
                request.description(),
                request.postedDate(),
                request.applyUrl());
    }
}
