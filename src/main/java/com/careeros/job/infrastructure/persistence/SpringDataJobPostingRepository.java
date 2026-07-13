package com.careeros.job.infrastructure.persistence;

import com.careeros.job.domain.JobPosting;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

interface SpringDataJobPostingRepository extends JpaRepository<JobPosting, UUID>, JpaSpecificationExecutor<JobPosting> {

    /**
     * {@code company} is lazy and open-in-view is disabled, so callers that
     * map a {@code JobPosting} to a response DTO outside the transaction
     * (i.e. every controller) need it eagerly loaded here.
     */
    @Override
    @EntityGraph(attributePaths = "company")
    Optional<JobPosting> findById(UUID id);

    Optional<JobPosting> findByHash(String hash);

    boolean existsByHash(String hash);
}
