package com.careeros.company.web;

import com.careeros.company.application.CompanyCommand;
import com.careeros.company.domain.Company;

final class CompanyMapper {

    private CompanyMapper() {
    }

    static CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getCareerUrl(),
                company.getAtsType(),
                company.getPriority(),
                company.isEnabled(),
                company.getCreatedAt(),
                company.getUpdatedAt());
    }

    static CompanyCommand toCommand(CompanyRequest request) {
        return new CompanyCommand(
                request.name(),
                request.careerUrl(),
                request.atsType(),
                request.priority(),
                request.enabled());
    }
}
