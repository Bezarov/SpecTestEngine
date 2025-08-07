package com.example.spectestengine.service;

import com.example.spectestengine.dto.TestRunDTO;

import java.util.List;

public interface TestRunService {
    List<TestRunDTO> runAllTestsSpec();

    TestRunDTO runTestBySpecId(Long specId);

    TestRunDTO runTestWithSpecName(String specName);

    List<TestRunDTO> runTestsInSpecRangeId(Long fromId, Long toId);
}
