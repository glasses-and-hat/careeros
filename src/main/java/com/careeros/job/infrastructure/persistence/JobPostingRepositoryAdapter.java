package com.careeros.job.infrastructure.persistence;

import com.careeros.job.domain.JobPosting;
import com.careeros.job.domain.JobPostingFilter;
import com.careeros.job.domain.JobPostingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class JobPostingRepositoryAdapter implements JobPostingRepository {

    private final SpringDataJobPostingRepository jpaRepository;

    JobPostingRepositoryAdapter(SpringDataJobPostingRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public JobPosting save(JobPosting jobPosting) {
        return jpaRepository.save(jobPosting);
    }

    @Override
    public Optional<JobPosting> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<JobPosting> findByHash(String hash) {
        return jpaRepository.findByHash(hash);
    }

    @Override
    public boolean existsByHash(String hash) {
        return jpaRepository.existsByHash(hash);
    }

    @Override
    public Page<JobPosting> findAll(JobPostingFilter filter, Pageable pageable) {
        return jpaRepository.findAll(JobPostingSpecifications.fromFilter(filter), pageable);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
