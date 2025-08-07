package com.example.spectestengine.utils;

import com.example.spectestengine.dto.TestRunDTO;
import com.example.spectestengine.model.TestRunEntity;

import java.time.temporal.ChronoUnit;

public class TestRunMapper {
    private TestRunMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static TestRunDTO mapToDTO(TestRunEntity runEntity) {
        return new TestRunDTO(
                runEntity.getId(),
                runEntity.getSpec().getId(),
                runEntity.getSpec().getFormat().getMediaType(),
                runEntity.getStatus(),
                SpecFormatMapper.fromJson(runEntity.getTestResultLog()),
                runEntity.getStartedAt().truncatedTo(ChronoUnit.SECONDS),
                runEntity.getFinishedAt().truncatedTo(ChronoUnit.SECONDS)
        );
    }
}
