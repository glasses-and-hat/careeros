package com.careeros.application.application;
import com.careeros.application.domain.*; import com.careeros.common.exception.ResourceNotFoundException; import com.careeros.job.domain.*;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate; import java.util.*;
@Service @Transactional public class JobApplicationService {
 private final JobApplicationRepository repo; private final JobPostingRepository jobs;
 public JobApplicationService(JobApplicationRepository r, JobPostingRepository j){repo=r;jobs=j;}
 public JobApplication create(Command c){JobPosting j=jobs.findById(c.jobPostingId()).orElseThrow(()->ResourceNotFoundException.forId("JobPosting",c.jobPostingId())); return repo.save(JobApplication.create(j,c.status(),c.appliedDate(),c.followUpDate(),c.interviewDate(),c.notes(),c.resumeVersion(),c.referralName(),c.recruiterName(),c.jobLink()));}
 public JobApplication update(UUID id,Command c){JobApplication a=get(id);a.change(c.status(),c.appliedDate(),c.followUpDate(),c.interviewDate(),c.notes(),c.resumeVersion(),c.referralName(),c.recruiterName(),c.jobLink());return repo.save(a);} @Transactional(readOnly=true) public JobApplication get(UUID id){return repo.findById(id).orElseThrow(()->ResourceNotFoundException.forId("Application",id));} @Transactional(readOnly=true) public Page<JobApplication> list(Pageable p){return repo.findAll(p);} public void delete(UUID id){get(id);repo.deleteById(id);}
 public record Command(UUID jobPostingId,ApplicationStatus status,LocalDate appliedDate,LocalDate followUpDate,LocalDate interviewDate,String notes,String resumeVersion,String referralName,String recruiterName,String jobLink){}
}
