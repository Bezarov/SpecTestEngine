package com.example.spectestengine.controller;

import com.example.spectestengine.dto.TestRunResultDTO;
import com.example.spectestengine.service.HTTPTestSpecService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test/run")
@Slf4j
public class TestRunController {
    private static final String RESPONSE_LOG = "Request was successfully processed and response was sent: {}";

    private final HTTPTestSpecService httpTestSpecService;

    public TestRunController(HTTPTestSpecService httpTestSpecService) {
        this.httpTestSpecService = httpTestSpecService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<TestRunResultDTO>> runAll() {
        log.debug("Received GET request to RUN all tests specification ");
        List<TestRunResultDTO> resultDTOS = httpTestSpecService.runAllTestsSpec();
        log.debug(RESPONSE_LOG, resultDTOS);
        return ResponseEntity.ok(resultDTOS);
    }

    @GetMapping("/by-id/{specId}")
    public ResponseEntity<TestRunResultDTO> runById(@PathVariable Long specId) {
        log.debug("Received GET request to RUN test specification with id: {}", specId);
        TestRunResultDTO resultDTO = httpTestSpecService.runTestBySpecId(specId);
        log.debug(RESPONSE_LOG, resultDTO);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping("/by-name/{specName}")
    public ResponseEntity<TestRunResultDTO> runByName(@PathVariable String specName) {
        log.debug("Received GET request to RUN test specification with name: {}", specName);
        TestRunResultDTO resultDTO = httpTestSpecService.runTestWithSpecName(specName);
        log.debug(RESPONSE_LOG, resultDTO);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping("/in-range")
    public ResponseEntity<List<TestRunResultDTO>> runInRange(@RequestParam Long fromId, @RequestParam Long toId) {
        log.debug("Received GET request to RUN in range tests specification from id: {}, to id: {}", fromId, toId);
        List<TestRunResultDTO> resultDTOS = httpTestSpecService.runTestsInSpecRangeId(fromId, toId);
        log.debug(RESPONSE_LOG, resultDTOS);
        return ResponseEntity.ok(resultDTOS);
    }
}
