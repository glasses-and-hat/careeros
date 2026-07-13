package com.careeros.company.web;

import com.careeros.common.exception.DuplicateResourceException;
import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.application.CompanyService;
import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyFilter;
import com.careeros.company.domain.Priority;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    private static Company aCompany() {
        return Company.create("Acme Inc", "https://acme.example/careers", AtsType.GREENHOUSE, Priority.HIGH, true);
    }

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        Company company = aCompany();
        when(companyService.create(any())).thenReturn(company);

        CompanyRequest request = new CompanyRequest(
                "Acme Inc", "https://acme.example/careers", AtsType.GREENHOUSE, Priority.HIGH, true);

        mockMvc.perform(post("/api/v1/companies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme Inc"))
                .andExpect(jsonPath("$.atsType").value("GREENHOUSE"));
    }

    @Test
    void createReturns400WhenNameBlank() throws Exception {
        CompanyRequest request = new CompanyRequest("", "https://acme.example", AtsType.GREENHOUSE, Priority.HIGH, true);

        mockMvc.perform(post("/api/v1/companies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createReturns409OnDuplicateName() throws Exception {
        when(companyService.create(any())).thenThrow(new DuplicateResourceException("A company named 'Acme Inc' already exists"));

        CompanyRequest request = new CompanyRequest(
                "Acme Inc", "https://acme.example/careers", AtsType.GREENHOUSE, Priority.HIGH, true);

        mockMvc.perform(post("/api/v1/companies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(companyService.get(id)).thenThrow(ResourceNotFoundException.forId("Company", id));

        mockMvc.perform(get("/api/v1/companies/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsPagedResults() throws Exception {
        Page<Company> page = new PageImpl<>(java.util.List.of(aCompany()));
        when(companyService.list(any(CompanyFilter.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/companies").param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Acme Inc"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deleteReturns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/companies/{id}", id))
                .andExpect(status().isNoContent());
    }
}
