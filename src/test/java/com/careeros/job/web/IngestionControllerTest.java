package com.careeros.job.web;

import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.domain.AtsType;
import com.careeros.job.application.ingestion.CompanyIngestionResult;
import com.careeros.job.application.ingestion.IngestionSummary;
import com.careeros.job.application.ingestion.JobIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobIngestionService jobIngestionService;

    private static IngestionSummary aSummary() {
        CompanyIngestionResult result = new CompanyIngestionResult(
                UUID.randomUUID(), "Acme Inc", AtsType.GREENHOUSE, 3, 1, 0, List.of());
        return new IngestionSummary(1, 3, 1, 0, List.of(result));
    }

    @Test
    void runAllReturns200WithAggregatedSummary() throws Exception {
        when(jobIngestionService.ingestAllEnabledCompanies()).thenReturn(aSummary());

        mockMvc.perform(post("/api/v1/ingestion/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companiesProcessed").value(1))
                .andExpect(jsonPath("$.totalCreated").value(3))
                .andExpect(jsonPath("$.results[0].companyName").value("Acme Inc"));
    }

    @Test
    void runForCompanyReturns200WithSummary() throws Exception {
        UUID companyId = UUID.randomUUID();
        when(jobIngestionService.ingestCompany(companyId)).thenReturn(aSummary());

        mockMvc.perform(post("/api/v1/ingestion/companies/{companyId}/runs", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCreated").value(3));
    }

    @Test
    void runForCompanyReturns404WhenCompanyMissing() throws Exception {
        UUID companyId = UUID.randomUUID();
        when(jobIngestionService.ingestCompany(companyId))
                .thenThrow(ResourceNotFoundException.forId("Company", companyId));

        mockMvc.perform(post("/api/v1/ingestion/companies/{companyId}/runs", companyId))
                .andExpect(status().isNotFound());
    }
}
