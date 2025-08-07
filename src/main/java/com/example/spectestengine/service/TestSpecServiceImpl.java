package com.example.spectestengine.service;

import com.example.spectestengine.dto.TestRunDTO;
import com.example.spectestengine.dto.TestSpecDTO;
import com.example.spectestengine.dto.TestSpecWithRunsDTO;
import com.example.spectestengine.model.TestSpecEntity;
import com.example.spectestengine.repository.TestSpecRepository;
import com.example.spectestengine.utils.SpecFormatMapper;
import com.example.spectestengine.utils.SpecFormatResolver;
import com.example.spectestengine.utils.TestSpecMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class TestSpecServiceImpl implements TestSpecService {
    private static final String SPEC_NOT_FOUND_LOG_MSG = "Specification not found with '%s': '%s'";

    private final TestSpecRepository testSpecRepository;

    public TestSpecServiceImpl(TestSpecRepository testSpecRepository) {
        this.testSpecRepository = testSpecRepository;
    }

    @Override
    public TestSpecDTO createSpec(String specName, String rawSpec) {
        log.info("Creating new test specification with name: '{}'", specName);
        testSpecRepository.findByName(specName)
                .ifPresent(existing -> {
                    log.warn("Specification with name: '{}', already exists", specName);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Specification with name '%s' already exists".formatted(specName));
                });

        TestSpecEntity savedEntity = testSpecRepository.save(TestSpecEntity.builder()
                .name(specName)
                .format(SpecFormatResolver.resolve(rawSpec))
                .spec(rawSpec)
                .createdAt(LocalDateTime.now())
                .build());
        log.info("Successfully created specification with name: '{}' and ID: '{}'", specName, savedEntity.getId());

        return TestSpecMapper.mapToDTO(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public TestSpecDTO getSpecById(Long specId) {
        log.debug("Searching specification by ID: '{}'", specId);
        return testSpecRepository.findById(specId)
                .map(TestSpecMapper::mapToDTO)
                .orElseThrow(() -> {
                    log.warn("Specification not found with ID: '{}'", specId);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public TestSpecDTO getSpecByName(String specName) {
        log.debug("Searching specification by name: '{}'", specName);
        return testSpecRepository.findByName(specName)
                .map(TestSpecMapper::mapToDTO)
                .orElseThrow(() -> {
                    log.warn("Specification not found with name: '{}'", specName);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("name:", specName));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestSpecDTO> getAllTestSpec() {
        log.debug("Searching all specifications");
        return testSpecRepository.findAll().stream()
                .map(TestSpecMapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TestSpecWithRunsDTO getSpecWithRuns(Long specId) {
        log.debug("Searching specification with runs for specification ID: '{}'", specId);
        return testSpecRepository.findByIdWithRuns(specId)
                .map(spec -> new TestSpecWithRunsDTO(
                        spec.getId(),
                        spec.getName(),
                        TestSpecMapper.getFormattedSpec(spec),
                        spec.getFormat().getMediaType(),
                        spec.getCreatedAt(),
                        spec.getRuns().stream()
                                .map(run -> new TestRunDTO(
                                        run.getId(),
                                        spec.getId(),
                                        spec.getFormat().getMediaType(),
                                        run.getStatus(),
                                        SpecFormatMapper.fromJson(run.getTestResultLog()),
                                        run.getStartedAt(),
                                        run.getFinishedAt()
                                ))
                                .toList()
                ))
                .orElseThrow(() -> {
                    log.warn("Specification with runs not found for ID: '{}'", specId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    public TestSpecDTO updateSpecById(Long specId, String rawSpec) {
        log.info("Updating specification with ID: '{}'", specId);
        return testSpecRepository.findById(specId)
                .map(specEntity -> {
                    specEntity.setFormat(SpecFormatResolver.resolve(rawSpec));
                    specEntity.setSpec(rawSpec);
                    TestSpecEntity savedSpec = testSpecRepository.save(specEntity);
                    log.debug("Specification updated from: '{}' to: '{}'", specEntity, savedSpec);
                    return TestSpecMapper.mapToDTO(savedSpec);
                })
                .orElseThrow(() -> {
                    log.warn("Update specification failed - specification with id: '{}' not found", specId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    public TestSpecDTO updateSpecByName(String specName, String rawSpec) {
        log.info("Updating specification with name: '{}'", specName);
        return testSpecRepository.findByName(specName)
                .map(specEntity -> {
                    specEntity.setFormat(SpecFormatResolver.resolve(rawSpec));
                    specEntity.setSpec(rawSpec);
                    TestSpecEntity savedSpec = testSpecRepository.save(specEntity);
                    log.debug("Specification updated from: '{}' to: '{}'", specEntity, savedSpec);
                    return TestSpecMapper.mapToDTO(savedSpec);
                })
                .orElseThrow(() -> {
                    log.warn("Update specification failed - specification with name: '{}' not found", specName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("name:", specName));
                });
    }

    @Override
    public TestSpecDTO deleteSpecById(Long specId) {
        log.info("Deleting specification with id: '{}'", specId);
        return testSpecRepository.findById(specId)
                .map(specEntity -> {
                    testSpecRepository.delete(specEntity);
                    log.debug("Successfully deleted specification with id: '{}'", specId);
                    return TestSpecMapper.mapToDTO(specEntity);
                })
                .orElseThrow(() -> {
                    log.warn("Delete specification failed - specification with id: '{}' not found", specId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("id:", specId));
                });
    }

    @Override
    public TestSpecDTO deleteSpecByName(String specName) {
        log.info("Deleting specification with name: '{}'", specName);
        return testSpecRepository.findByName(specName)
                .map(specEntity -> {
                    testSpecRepository.delete(specEntity);
                    log.debug("Successfully deleted specification with name: '{}'", specName);
                    return TestSpecMapper.mapToDTO(specEntity);
                })
                .orElseThrow(() -> {
                    log.warn("Delete specification failed - specification with name: '{}' not found", specName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            SPEC_NOT_FOUND_LOG_MSG.formatted("name:", specName));
                });
    }
}