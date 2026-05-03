package com.example.backend.Controller;

import com.example.backend.Model.Staff;
import com.example.backend.Repository.StaffRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:5173")
public class StaffController {

    private final StaffRepository repo;

    public StaffController(StaffRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<Staff>> getAll(
            @RequestParam(required = false) String search
    ) {
        if (search != null && !search.trim().isBlank()) {
            String keyword = search.trim();
            List<Staff> byName = repo.findByFullNameContainingIgnoreCase(keyword);
            List<Staff> byPosition = repo.findByPositionContainingIgnoreCase(keyword);

            byPosition.forEach(item -> {
                if (byName.stream().noneMatch(s -> s.getStaffId().equals(item.getStaffId()))) {
                    byName.add(item);
                }
            });

            return ResponseEntity.ok(byName);
        }

        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Staff> create(@RequestBody Staff body) {
        return ResponseEntity.ok(repo.save(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Staff> update(@PathVariable Long id, @RequestBody Staff body) {
        return repo.findById(id)
                .map(existing -> {
                    body.setStaffId(id);
                    if (body.getCreatedAt() == null) {
                        body.setCreatedAt(existing.getCreatedAt());
                    }
                    return ResponseEntity.ok(repo.save(body));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}