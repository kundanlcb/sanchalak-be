package com.cm.sanchalak.platform.master;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterDataServiceTest {

    @Mock
    private MasterDomainRepository domainRepository;

    @Mock
    private MasterValueRepository valueRepository;

    @InjectMocks
    private MasterDataService masterDataService;

    private MasterDomain genderDomain;
    private MasterValue maleValue;

    @BeforeEach
    void setUp() {
        genderDomain = MasterDomain.builder()
                .id(UUID.randomUUID())
                .code("GENDER")
                .description("Gender Identity")
                .isSystem(true)
                .build();

        maleValue = MasterValue.builder()
                .id(1L)
                .domain(genderDomain)
                .code("MALE")
                .label("Male")
                .isActive(true)
                .sortOrder(1)
                .build();
    }

    @Test
    void getValues_ShouldReturnDtos_WhenDomainExists() {
        when(domainRepository.existsByCode("GENDER")).thenReturn(true);
        when(valueRepository.findByDomainCodeAndIsActiveTrueOrderBySortOrderAsc("GENDER"))
                .thenReturn(List.of(maleValue));

        List<MasterValueDto> result = masterDataService.getValues("GENDER", true);

        assertEquals(1, result.size());
        assertEquals("MALE", result.get(0).getCode());
        assertEquals("Male", result.get(0).getLabel());
    }

    @Test
    void getValues_ShouldThrowException_WhenDomainDoesNotExist() {
        when(domainRepository.existsByCode("INVALID")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> masterDataService.getValues("INVALID", true));
    }

    @Test
    void validateValue_ShouldPass_WhenValueIsValid() {
        when(valueRepository.findByDomainCodeAndCode("GENDER", "MALE"))
                .thenReturn(Optional.of(maleValue));

        assertDoesNotThrow(() -> masterDataService.validateValue("GENDER", "MALE"));
    }

    @Test
    void validateValue_ShouldThrowException_WhenValueIsInvalid() {
        when(valueRepository.findByDomainCodeAndCode("GENDER", "INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> masterDataService.validateValue("GENDER", "INVALID"));
    }

    @Test
    void validateValue_ShouldPass_WhenValueIsNull() {
        assertDoesNotThrow(() -> masterDataService.validateValue("GENDER", null));
    }
}
