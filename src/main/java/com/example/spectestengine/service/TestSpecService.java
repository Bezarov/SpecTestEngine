package com.example.spectestengine.service;

import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;

import java.util.List;

public interface TestSpecService {
    TestSpecDTO createSpec(String specName, String rawSpec);

    TestSpecDTO getSpecById(Long specId);

    TestSpecDTO getSpecByName(String specName);

    List<TestSpecDTO> getAllTestSpec();

    TestSpecWithRunsDTO getSpecWithRuns(Long specId);

    TestSpecDTO updateSpecById(Long specId, String rawSpec);

    TestSpecDTO updateSpecByName(String specName, String rawSpec);

    TestSpecDTO deleteSpecById(Long specId);

    TestSpecDTO deleteSpecByName(String specName);
}