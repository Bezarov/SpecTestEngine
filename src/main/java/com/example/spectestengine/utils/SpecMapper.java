package com.example.spectestengine.utils;

import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.model.TestSpecEntity;

import java.util.Collections;

public class SpecMapper {

    private SpecMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static TestSpecDTO mapToDTO(TestSpecEntity specEntity) {
        return new TestSpecDTO(specEntity.getId(), specEntity.getName(), specEntity.getSpec(), specEntity.getCreatedAt());
    }

    public static TestSpecEntity mapToDTO(TestSpecDTO specDTO) {
        return new TestSpecEntity(specDTO.id(), specDTO.name(), specDTO.spec(), Collections.emptyList(), specDTO.createdAt());
    }
}
