package com.example.demo.attendance;

import javax.persistence.*;

@Entity
@Table(name = "sites")
public class Site {

    @Id
    @SequenceGenerator(name = "site_sequence", sequenceName = "site_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site_sequence")
    private Long id;

    @Column(nullable = false)
    private String siteName;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Boolean active = true;

    public Site() {}

    public Long getId() { return id; }
    public String getSiteName() { return siteName; }
    public String getLocation() { return location; }
    public Boolean getActive() { return active; }

    public void setId(Long id) { this.id = id; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public void setLocation(String location) { this.location = location; }
    public void setActive(Boolean active) { this.active = active; }
}