package com.careeros.company.web;

import com.careeros.common.web.PageResponse;
import com.careeros.company.application.CompanyService;
import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyFilter;
import com.careeros.company.domain.Priority;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * CRUD API for companies monitored by CareerOS.
 */
@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies", description = "Manage companies monitored for new job postings")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @Operation(summary = "Register a new company to monitor")
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        Company company = companyService.create(CompanyMapper.toCommand(request));
        CompanyResponse response = CompanyMapper.toResponse(company);
        return ResponseEntity.created(URI.create("/api/v1/companies/" + company.getId())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a company by id")
    public CompanyResponse get(@PathVariable UUID id) {
        return CompanyMapper.toResponse(companyService.get(id));
    }

    @GetMapping
    @Operation(summary = "List companies with optional filtering, sorting, and pagination")
    public PageResponse<CompanyResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) AtsType atsType,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        CompanyFilter filter = new CompanyFilter(name, atsType, priority, enabled);
        return PageResponse.from(companyService.list(filter, pageable), CompanyMapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing company")
    public CompanyResponse update(@PathVariable UUID id, @Valid @RequestBody CompanyRequest request) {
        Company company = companyService.update(id, CompanyMapper.toCommand(request));
        return CompanyMapper.toResponse(company);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a company from monitoring")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        companyService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
