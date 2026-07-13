package com.careeros.job.web;

import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.Priority;
import com.careeros.job.application.JobPostingService;
import com.careeros.job.domain.EmploymentType;
import com.careeros.job.domain.JobPosting;
import com.careeros.job.domain.SalaryRange;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobPostingController.class)
class JobPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobPostingService jobPostingService;

    private static JobPosting aJobPosting() {
        Company company = Company.create("Acme Inc", "https://acme.example/careers", AtsType.GREENHOUSE, Priority.HIGH, true);
        return JobPosting.create("job-123", company, "Senior Backend Engineer", "Remote",
                EmploymentType.FULL_TIME, true, SalaryRange.undisclosed(), "Great job", LocalDate.now(),
                "https://acme.example/apply/job-123");
    }

    @Test
    void createReturns201() throws Exception {
        JobPosting jobPosting = aJobPosting();
        when(jobPostingService.create(any())).thenReturn(jobPosting);

        JobPostingRequest request = new JobPostingRequest(
                "job-123", UUID.randomUUID(), "Senior Backend Engineer", "Remote", EmploymentType.FULL_TIME, true,
                null, null, null, "Great job", LocalDate.now(), "https://acme.example/apply/job-123");

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.remote").value(true));
    }

    @Test
    void createReturns400WhenTitleBlank() throws Exception {
        JobPostingRequest request = new JobPostingRequest(
                "job-123", UUID.randomUUID(), "", "Remote", EmploymentType.FULL_TIME, true,
                null, null, null, null, LocalDate.now(), "https://acme.example/apply/job-123");

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(jobPostingService.get(id)).thenThrow(ResourceNotFoundException.forId("JobPosting", id));

        mockMvc.perform(get("/api/v1/jobs/{id}", id))
                .andExpect(status().isNotFound());
    }
}
