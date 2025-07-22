package com.example.spectestengine.repository;

import com.example.spectestengine.model.TestSpecEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestSpecRepository extends JpaRepository<TestSpecEntity, Long> {
    Optional<TestSpecEntity> findByName(String name);

    List<TestSpecEntity> findAllByIdBetween(Long fromId, Long toId);

    @Query("SELECT s FROM TestSpecEntity s LEFT JOIN FETCH s.runs WHERE s.id = :id")
    Optional<TestSpecEntity> findByIdWithRuns(@Param("id") Long id);
}
