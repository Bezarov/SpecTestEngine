package com.example.spectestengine.utils;

import com.example.spectestengine.dto.TestRunResultDTO;
import com.example.spectestengine.model.TestRunEntity;

import java.time.temporal.ChronoUnit;

public class TestRunMapper {
    private TestRunMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static TestRunResultDTO mapToDTO(TestRunEntity runEntity) {
        return new TestRunResultDTO(
                runEntity.getId(),
                runEntity.getSpec().getId(),
                runEntity.getStatus(),
                JsonMapper.fromJson(runEntity.getLog()),
                runEntity.getStartedAt().truncatedTo(ChronoUnit.SECONDS),
                runEntity.getFinishedAt().truncatedTo(ChronoUnit.SECONDS)
        );
    }
}
