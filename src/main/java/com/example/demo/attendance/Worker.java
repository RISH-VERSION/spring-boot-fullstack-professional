package com.example.demo.attendance;

import javax.persistence.*;

@Entity
@Table(name = "workers", indexes = {
        @Index(name = "idx_worker_phone", columnList = "phone")
})
public class Worker {

    @Id
    @SequenceGenerator(name = "worker_sequence", sequenceName = "worker_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "worker_sequence")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Designation designation;

    @Column(nullable = false)
    private Double dailyWageRate;

    @Column(nullable = false)
    private Boolean active = true;

    public enum Designation {
        MASON, ELECTRICIAN, PLUMBER, SUPERVISOR, HELPER
    }

    public Worker() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Designation getDesignation() { return designation; }
    public Double getDailyWageRate() { return dailyWageRate; }
    public Boolean getActive() { return active; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDesignation(Designation designation) { this.designation = designation; }
    public void setDailyWageRate(Double dailyWageRate) { this.dailyWageRate = dailyWageRate; }
    public void setActive(Boolean active) { this.active = active; }
}
