package com.careeros.job.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Port defining the persistence operations the application layer needs for
 * {@link JobPosting}. Implemented by an infrastructure adapter.
 */
public interface JobPostingRepository {

    JobPosting save(JobPosting jobPosting);

    Optional<JobPosting> findById(UUID id);

    Optional<JobPosting> findByHash(String hash);

    boolean existsByHash(String hash);

    Page<JobPosting> findAll(JobPostingFilter filter, Pageable pageable);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
