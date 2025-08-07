package com.example.spectestengine.utils;

import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.model.TestSpecEntity;

import java.time.temporal.ChronoUnit;

public final class TestSpecMapper {

    private TestSpecMapper() {
        throw new IllegalStateException("Utility class");
    }


    public static TestSpecDTO mapToDTO(TestSpecEntity specEntity) {
        return new TestSpecDTO(
                specEntity.getId(),
                specEntity.getName(),
                specEntity.getFormat().getMediaType(),
                getFormattedSpec(specEntity),
                specEntity.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    public static Object getFormattedSpec(TestSpecEntity specEntity) {
        return switch (specEntity.getFormat()) {
            case JSON -> SpecFormatMapper.fromJson(specEntity.getSpec());
            case YAML -> SpecFormatMapper.fromYaml(specEntity.getSpec());
        };
    }
}
