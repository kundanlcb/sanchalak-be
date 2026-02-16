package com.cm.sanchalak.platform.master;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final MasterDomainRepository domainRepository;
    private final MasterValueRepository valueRepository;

    public List<MasterValueDto> getValues(String domainCode, boolean activeOnly) {
        if (!domainRepository.existsByCode(domainCode)) {
            throw new RuntimeException("Master Domain not found: " + domainCode);
        }

        List<MasterValue> values;
        if (activeOnly) {
            values = valueRepository.findByDomainCodeAndIsActiveTrueOrderBySortOrderAsc(domainCode);
        } else {
            values = valueRepository.findByDomainCode(domainCode);
        }

        return values.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public boolean isValid(String domainCode, String valueCode) {
        if (valueCode == null)
            return true; // Null is valid (optional fields)
        if (valueCode.isEmpty())
            return true; // Empty string logic depends on use case, treating as valid or handled by
                         // @NotBlank

        Optional<MasterValue> value = valueRepository.findByDomainCodeAndCode(domainCode, valueCode);
        return value.isPresent() && value.get().isActive();
    }

    public void validateValue(String domainCode, String valueCode) {
        if (valueCode != null && !valueCode.isEmpty()) {
            if (!isValid(domainCode, valueCode)) {
                throw new RuntimeException("Invalid value '" + valueCode + "' for domain '" + domainCode + "'");
            }
        }
    }

    @Transactional
    public void createValue(String domainCode, MasterValueDto dto) {
        MasterDomain domain = domainRepository.findByCode(domainCode)
                .orElseThrow(() -> new RuntimeException("Domain not found: " + domainCode));

        if (domain.isSystem()) {
            throw new RuntimeException("Cannot add values to System domain: " + domainCode);
        }

        if (valueRepository.existsByDomainCodeAndCode(domainCode, dto.getCode())) {
            throw new RuntimeException("Value code already exists: " + dto.getCode());
        }

        MasterValue value = MasterValue.builder()
                .domain(domain)
                .code(dto.getCode())
                .label(dto.getLabel())
                .isActive(true)
                .sortOrder(dto.getSortOrder())
                .build();

        valueRepository.save(value);
    }

    private MasterValueDto mapToDto(MasterValue entity) {
        return MasterValueDto.builder()
                .code(entity.getCode())
                .label(entity.getLabel())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.isActive())
                .build();
    }
}
