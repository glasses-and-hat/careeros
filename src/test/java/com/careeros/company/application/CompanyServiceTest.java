package com.careeros.company.application;

import com.careeros.common.exception.DuplicateResourceException;
import com.careeros.common.exception.ResourceNotFoundException;
import com.careeros.company.domain.AtsType;
import com.careeros.company.domain.Company;
import com.careeros.company.domain.CompanyFilter;
import com.careeros.company.domain.CompanyRepository;
import com.careeros.company.domain.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        companyService = new CompanyService(companyRepository);
    }

    private static CompanyCommand aCommand() {
        return new CompanyCommand("Acme Inc", "https://acme.example/careers", AtsType.GREENHOUSE, Priority.HIGH, true);
    }

    @Test
    void createsCompanyWhenNameIsUnique() {
        CompanyCommand command = aCommand();
        when(companyRepository.existsByNameIgnoreCase(command.name())).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company result = companyService.create(command);

        assertThat(result.getName()).isEqualTo("Acme Inc");
        assertThat(result.getAtsType()).isEqualTo(AtsType.GREENHOUSE);
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void rejectsDuplicateCompanyName() {
        CompanyCommand command = aCommand();
        when(companyRepository.existsByNameIgnoreCase(command.name())).thenReturn(true);

        assertThatThrownBy(() -> companyService.create(command))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Acme Inc");

        verify(companyRepository, never()).save(any());
    }

    @Test
    void throwsWhenCompanyNotFoundOnGet() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.get(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesExistingCompany() {
        UUID id = UUID.randomUUID();
        Company existing = Company.create("Old Name", "https://old.example", AtsType.LEVER, Priority.LOW, false);
        when(companyRepository.findById(id)).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyCommand update = new CompanyCommand("New Name", "https://new.example", AtsType.ASHBY, Priority.HIGH, true);
        Company result = companyService.update(id, update);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getAtsType()).isEqualTo(AtsType.ASHBY);
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void deletesExistingCompany() {
        UUID id = UUID.randomUUID();
        when(companyRepository.existsById(id)).thenReturn(true);

        companyService.delete(id);

        verify(companyRepository).deleteById(id);
    }

    @Test
    void throwsWhenDeletingMissingCompany() {
        UUID id = UUID.randomUUID();
        when(companyRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> companyService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(companyRepository, never()).deleteById(any());
    }

    @Test
    void listsCompaniesUsingFilterAndPageable() {
        Pageable pageable = PageRequest.of(0, 20);
        CompanyFilter filter = CompanyFilter.empty();
        Page<Company> page = new PageImpl<>(List.of());
        when(companyRepository.findAll(filter, pageable)).thenReturn(page);

        Page<Company> result = companyService.list(filter, pageable);

        assertThat(result).isSameAs(page);
    }
}
