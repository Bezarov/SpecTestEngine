package com.example.spectestengine.repository;

import com.example.spectestengine.model.TestRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRunRepository extends JpaRepository<TestRunEntity, Long> {
    List<TestRunEntity> findAllByStatusIgnoreCase(String status);
}