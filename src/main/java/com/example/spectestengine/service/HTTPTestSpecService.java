package com.example.spectestengine.service;

import com.example.spectestengine.dto.TestRunResultDTO;
import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;

import java.util.List;

public interface HTTPTestSpecService {
    TestSpecDTO createSpec(String specName, String specJson);

    List<TestSpecDTO> getAllTestSpec();

    List<TestRunResultDTO> runAllTestsSpec();

    TestRunResultDTO runTestBySpecId(Long specId);

    TestRunResultDTO runTestWithSpecName(String specName);

    List<TestRunResultDTO> runTestsInSpecRangeId(Long fromId, Long toId);

    TestSpecWithRunsDTO getSpecWithRuns(Long specId);

    TestSpecDTO updateSpecById(Long specId, String specJson);

    TestSpecDTO updateSpecByName(String specName, String specJson);

    TestSpecDTO deleteSpecById(Long specId, String specJson);

    TestSpecDTO deleteSpecByName(String specName, String specJson);
}