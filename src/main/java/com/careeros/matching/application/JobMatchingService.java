package com.careeros.matching.application;

import com.careeros.company.domain.Priority;
import com.careeros.job.domain.JobPosting;
import com.careeros.matching.domain.JobMatch;
import com.careeros.preference.domain.UserPreference;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class JobMatchingService {
    private final MatchingProperties weights;
    private final Clock clock;
    public JobMatchingService(MatchingProperties weights) { this(weights, Clock.systemUTC()); }
    JobMatchingService(MatchingProperties weights, Clock clock) { this.weights = weights; this.clock = clock; }

    public JobMatch score(JobPosting job, UserPreference p) {
        String title = normalize(job.getTitle());
        String text = title + " " + normalize(job.getDescription());
        int role = bestContains(title, p.getRoles());
        int tech = ratio(text, p.getTechnologies());
        int location = bestContains(normalize(job.getLocation()), p.getLocations());
        int remote = job.isRemote() ? 100 : (p.isRemoteOnly() ? 0 : 50);
        int priority = switch (job.getCompany().getPriority()) { case HIGH -> 100; case MEDIUM -> 60; case LOW -> 25; };
        int recency = recency(job.getPostedDate());
        boolean ignored = p.getIgnoredCompanies().stream().anyMatch(x -> normalize(job.getCompany().getName()).contains(normalize(x)))
                || p.getIgnoredKeywords().stream().anyMatch(x -> text.contains(normalize(x)));
        int overall = ignored ? 0 : weighted(role, tech, location, remote, priority, recency);
        List<String> why = new ArrayList<>();
        if (ignored) why.add("Excluded by ignored company or keyword preference");
        else {
            why.add("Role match: " + role + "%"); why.add("Technology match: " + tech + "%");
            why.add("Location match: " + location + "%"); why.add("Remote match: " + remote + "%");
            why.add("Company priority: " + priority + "%"); why.add("Recency: " + recency + "%");
        }
        return new JobMatch(job, overall, role, tech, location, remote, priority, recency, List.copyOf(why));
    }
    private int weighted(int... s) { return Math.toIntExact(Math.round((s[0]*weights.role()+s[1]*weights.technology()+s[2]*weights.location()+s[3]*weights.remote()+s[4]*weights.companyPriority()+s[5]*weights.recency())/100.0)); }
    private int bestContains(String text, List<String> terms) { return terms.isEmpty() ? 0 : terms.stream().anyMatch(x -> text.contains(normalize(x))) ? 100 : 0; }
    private int ratio(String text, List<String> terms) { if (terms.isEmpty()) return 0; long n=terms.stream().map(this::normalize).filter(text::contains).count(); return (int)Math.round(n*100.0/terms.size()); }
    private int recency(LocalDate date) { if(date==null)return 0; long d=Math.max(0, ChronoUnit.DAYS.between(date, LocalDate.now(clock))); return d<=1?100:d<=7?80:d<=30?40:10; }
    private String normalize(String s) { return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+#]+", " ").trim(); }
}
