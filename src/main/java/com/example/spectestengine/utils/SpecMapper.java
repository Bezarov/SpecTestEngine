package com.example.spectestengine.utils;

import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.model.TestSpecEntity;

import java.time.temporal.ChronoUnit;
import java.util.Collections;

public final class SpecMapper {

    private SpecMapper() {
        throw new IllegalStateException("Utility class");
    }


    public static TestSpecDTO mapToDTO(TestSpecEntity specEntity) {
        return new TestSpecDTO(
                specEntity.getId(),
                specEntity.getName(),
                JsonMapper.fromJson(specEntity.getSpec()),
                specEntity.getCreatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    public static TestSpecEntity mapToEntity(TestSpecDTO specDTO) {
        return TestSpecEntity.builder()
                .id(specDTO.id())
                .name(specDTO.name())
                .spec(JsonMapper.toJson(specDTO.spec()))
                .runs(Collections.emptyList())
                .createdAt(specDTO.createdAt().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
