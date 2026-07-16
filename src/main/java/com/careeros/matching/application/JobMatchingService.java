package com.careeros.matching.application;

import com.careeros.company.domain.Priority;
import com.careeros.job.domain.JobPosting;
import com.careeros.matching.domain.JobMatch;
import com.careeros.preference.domain.UserPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class JobMatchingService {
    private final MatchingProperties weights;
    private final Clock clock;

    @Autowired
    public JobMatchingService(MatchingProperties weights) { this(weights, Clock.systemUTC()); }
    JobMatchingService(MatchingProperties weights, Clock clock) { this.weights = weights; this.clock = clock; }

    public JobMatch score(JobPosting job, UserPreference p) {
        String title = normalize(job.getTitle());
        String text = title + " " + normalize(job.getDescription());
        int role = bestContains(title, p.getRoles());
        int tech = ratio(text, p.getTechnologies());
        int location = bestContains(normalize(job.getLocation()), p.getLocations());
        if (location == 0 && targetsChicagoland(p.getLocations()) && isChicagoland(job.getLocation())) location = 100;
        if (location == 0 && job.isRemote() && targetsRemote(p.getLocations()) && isUnitedStatesLocation(job.getLocation())) location = 100;
        int remote = job.isRemote() ? 100 : (p.isRemoteOnly() ? 0 : 50);
        int priority = switch (job.getCompany().getPriority()) { case HIGH -> 100; case MEDIUM -> 60; case LOW -> 25; };
        int recency = recency(job.getPostedDate());
        boolean outsideUnitedStates = p.isUnitedStatesOnly() && !isUnitedStatesLocation(job.getLocation());
        boolean outsidePreferredArea = p.isUnitedStatesOnly() && targetsChicagoland(p.getLocations())
                && !isChicagoland(job.getLocation()) && !(job.isRemote() && isUnitedStatesLocation(job.getLocation()));
        boolean ignored = outsideUnitedStates || outsidePreferredArea
                || p.getIgnoredCompanies().stream().anyMatch(x -> normalize(job.getCompany().getName()).contains(normalize(x)))
                || p.getIgnoredKeywords().stream().anyMatch(x -> text.contains(normalize(x)));
        int overall = ignored ? 0 : weighted(role, tech, location, remote, priority, recency);
        List<String> why = new ArrayList<>();
        if (outsideUnitedStates) why.add("Excluded because the location is not explicitly within the United States");
        else if (outsidePreferredArea) why.add("Excluded because the role is neither in Chicagoland nor explicitly US-remote");
        else if (ignored) why.add("Excluded by ignored company or keyword preference");
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
    private boolean isUnitedStatesLocation(String location) {
        String value = " " + normalize(location) + " ";
        if (value.contains(" united states ") || value.contains(" usa ") || value.contains(" u s ")) return true;
        Set<String> states = Set.of("al","ak","az","ar","ca","co","ct","de","fl","ga","hi","id","il","in","ia","ks","ky","la","me","md","ma","mi","mn","ms","mo","mt","ne","nv","nh","nj","nm","ny","nc","nd","oh","ok","or","pa","ri","sc","sd","tn","tx","ut","vt","va","wa","wv","wi","wy","dc");
        if (location != null && Arrays.stream(location.replaceAll("[^A-Za-z]", " ").trim().split("\\s+"))
                .filter(token -> token.equals(token.toUpperCase(Locale.ROOT)))
                .map(token -> token.toLowerCase(Locale.ROOT)).anyMatch(states::contains)) return true;
        return List.of("alabama","alaska","arizona","arkansas","california","colorado","connecticut","delaware","florida","georgia","hawaii","idaho","illinois","indiana","iowa","kansas","kentucky","louisiana","maine","maryland","massachusetts","michigan","minnesota","mississippi","missouri","montana","nebraska","nevada","new hampshire","new jersey","new mexico","new york","north carolina","north dakota","ohio","oklahoma","oregon","pennsylvania","rhode island","south carolina","south dakota","tennessee","texas","utah","vermont","virginia","washington","west virginia","wisconsin","wyoming","district of columbia","chicago").stream().anyMatch(x -> value.contains(" "+x+" "));
    }
    private boolean targetsChicagoland(List<String> preferences) {
        return preferences.stream().map(this::normalize).anyMatch(value -> value.contains("chicago") || value.contains("chicagoland"));
    }
    private boolean targetsRemote(List<String> preferences) { return preferences.stream().map(this::normalize).anyMatch(value -> value.contains("remote")); }
    private boolean isChicagoland(String location) {
        String value = " " + normalize(location) + " ";
        return List.of("chicago","chicagoland","greater chicago","arlington heights","aurora","barrington","bartlett","batavia","bensenville","berwyn","bolingbrook","buffalo grove","carol stream","cicero","deerfield","des plaines","downers grove","elgin","elk grove village","elmhurst","evanston","glen ellyn","glenview","gurnee","highland park","hoffman estates","itasca","lake forest","libertyville","lincolnshire","lisle","lombard","melrose park","morton grove","mount prospect","mundelein","naperville","niles","northbrook","oak brook","oak park","orland park","palatine","park ridge","rolling meadows","rosemont","schaumburg","skokie","tinley park","vernon hills","warrenville","west chicago","westmont","wheaton","wilmette","wood dale","woodridge").stream().anyMatch(place -> value.contains(" " + place + " "));
    }
}
