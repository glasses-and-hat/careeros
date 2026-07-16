package com.careeros.watchlist.domain;
import com.careeros.common.domain.AuditableEntity; import com.careeros.company.domain.Company; import jakarta.persistence.*; import org.hibernate.annotations.UuidGenerator;
import java.util.*;
@Entity @Table(name="watchlists") public class Watchlist extends AuditableEntity {
 @Id @UuidGenerator private UUID id; @Column(nullable=false) private String name;
 @OneToMany(mappedBy="watchlist",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.EAGER) private Set<WatchlistCompany> companies=new LinkedHashSet<>();
 protected Watchlist(){} public static Watchlist create(String n){Watchlist w=new Watchlist();w.rename(n);return w;} public void rename(String n){if(n==null||n.isBlank())throw new IllegalArgumentException("name is required");name=n;} public void add(Company c,int p,boolean enabled){companies.removeIf(x->x.getCompany().getId().equals(c.getId()));companies.add(new WatchlistCompany(this,c,p,enabled));} public void remove(UUID companyId){companies.removeIf(x->x.getCompany().getId().equals(companyId));}
 public UUID getId(){return id;} public String getName(){return name;} public Set<WatchlistCompany> getCompanies(){return Set.copyOf(companies);}
}
