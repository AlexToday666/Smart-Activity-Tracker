package com.example.ActivityTracker.service;

import com.example.ActivityTracker.dto.ProjectRequestDto;
import com.example.ActivityTracker.exception.BadRequestException;
import com.example.ActivityTracker.exception.NotFoundException;
import com.example.ActivityTracker.model.Project;
import com.example.ActivityTracker.repository.ProjectRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project create(ProjectRequestDto request) {
        if (projectRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Project slug already exists");
        }
        Project project = new Project();
        apply(project, request);
        Project saved = projectRepository.save(project);
        log.info("project_created projectId={} slug={}", saved.getId(), saved.getSlug());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Project get(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }

    public Project update(Long id, ProjectRequestDto request) {
        Project project = get(id);
        if (!project.getSlug().equals(request.getSlug()) && projectRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Project slug already exists");
        }
        apply(project, request);
        Project saved = projectRepository.save(project);
        log.info("project_updated projectId={} slug={}", saved.getId(), saved.getSlug());
        return saved;
    }

    public void delete(Long id) {
        Project project = get(id);
        projectRepository.delete(project);
        log.info("project_deleted projectId={}", id);
    }

    private void apply(Project project, ProjectRequestDto request) {
        project.setName(request.getName());
        project.setSlug(request.getSlug());
        project.setDescription(request.getDescription());
    }
}
