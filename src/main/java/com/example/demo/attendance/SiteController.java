package com.example.demo.attendance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteRepository siteRepository;

    public SiteController(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @PostMapping
    public ResponseEntity<?> createSite(@RequestBody Site site) {
        return ResponseEntity.ok(siteRepository.save(site));
    }

    @GetMapping
    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }
}