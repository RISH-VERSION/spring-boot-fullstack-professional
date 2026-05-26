package com.example.demo.attendance;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerRepository workerRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVE_WORKERS_KEY = "active_workers";

    public WorkerController(WorkerRepository workerRepository,
                            RedisTemplate<String, Object> redisTemplate) {
        this.workerRepository = workerRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createWorker(@RequestBody Worker worker) {
        return ResponseEntity.ok(workerRepository.save(worker));
    }

    @GetMapping
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    @PutMapping("/{workerId}")
    public ResponseEntity<?> updateWorker(@PathVariable Long workerId,
                                          @RequestBody Worker updated) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("WORKER_NOT_FOUND:Worker not found"));

        worker.setName(updated.getName());
        worker.setDesignation(updated.getDesignation());
        worker.setDailyWageRate(updated.getDailyWageRate());
        worker.setActive(updated.getActive());

        workerRepository.save(worker);

        // Invalidate Redis cache if worker is currently clocked in
        redisTemplate.delete(ACTIVE_WORKERS_KEY + ":" + workerId);

        return ResponseEntity.ok(worker);
    }
}