package com.careeros.application.domain;
import org.springframework.data.domain.*;
import java.util.*;
public interface JobApplicationRepository { JobApplication save(JobApplication a); Optional<JobApplication> findById(UUID id); Page<JobApplication> findAll(Pageable p); List<JobApplication> findAll(); void deleteById(UUID id); }
