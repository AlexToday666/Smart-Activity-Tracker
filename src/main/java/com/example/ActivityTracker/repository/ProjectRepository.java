package com.example.ActivityTracker.repository;

import com.example.ActivityTracker.model.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsBySlug(String slug);

    Optional<Project> findBySlug(String slug);
}
