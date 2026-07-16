package com.careeros.application.domain;

import com.careeros.common.domain.AuditableEntity;
import com.careeros.job.domain.JobPosting;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name="job_applications")
public class JobApplication extends AuditableEntity {
 @Id @UuidGenerator private UUID id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="job_posting_id") private JobPosting job;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ApplicationStatus status;
 private LocalDate appliedDate; private LocalDate followUpDate; private LocalDate interviewDate;
 @Column(columnDefinition="text") private String notes;
 private String resumeVersion; private String referralName; private String recruiterName;
 @Column(length=2048) private String jobLink;
 protected JobApplication() {}
 public static JobApplication create(JobPosting j, ApplicationStatus s, LocalDate applied, LocalDate follow, LocalDate interview, String notes, String resume, String referral, String recruiter, String link) { JobApplication a=new JobApplication(); a.job=j; a.change(s,applied,follow,interview,notes,resume,referral,recruiter,link); return a; }
 public void change(ApplicationStatus s, LocalDate applied, LocalDate follow, LocalDate interview, String notes, String resume, String referral, String recruiter, String link) { if(s==null)throw new IllegalArgumentException("status is required"); status=s; appliedDate=applied; followUpDate=follow; interviewDate=interview; this.notes=notes; resumeVersion=resume; referralName=referral; recruiterName=recruiter; jobLink=link; }
 public UUID getId(){return id;} public JobPosting getJob(){return job;} public ApplicationStatus getStatus(){return status;} public LocalDate getAppliedDate(){return appliedDate;} public LocalDate getFollowUpDate(){return followUpDate;} public LocalDate getInterviewDate(){return interviewDate;} public String getNotes(){return notes;} public String getResumeVersion(){return resumeVersion;} public String getReferralName(){return referralName;} public String getRecruiterName(){return recruiterName;} public String getJobLink(){return jobLink;}
}
