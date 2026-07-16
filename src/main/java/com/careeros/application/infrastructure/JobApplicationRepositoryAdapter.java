package com.careeros.application.infrastructure;
import com.careeros.application.domain.*;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.stereotype.Repository;
import java.util.*;
interface SpringDataJobApplicationRepository extends JpaRepository<JobApplication,UUID> { @EntityGraph(attributePaths={"job","job.company"}) Optional<JobApplication> findById(UUID id); @EntityGraph(attributePaths={"job","job.company"}) Page<JobApplication> findAll(Pageable p); @EntityGraph(attributePaths={"job","job.company"}) List<JobApplication> findAll(); }
@Repository class JobApplicationRepositoryAdapter implements JobApplicationRepository {
 private final SpringDataJobApplicationRepository r; JobApplicationRepositoryAdapter(SpringDataJobApplicationRepository r){this.r=r;}
 public JobApplication save(JobApplication a){return r.save(a);} public Optional<JobApplication> findById(UUID id){return r.findById(id);} public Page<JobApplication> findAll(Pageable p){return r.findAll(p);} public List<JobApplication> findAll(){return r.findAll();} public void deleteById(UUID id){r.deleteById(id);}
}
