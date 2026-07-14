package com.careeros.job.web;

import com.careeros.company.domain.AtsType;

import java.util.List;
import java.util.UUID;

public record CompanyIngestionResultResponse(
        UUID companyId,
        String companyName,
        AtsType atsType,
        int created,
        int skipped,
        int failed,
        List<String> errors
) {
}
