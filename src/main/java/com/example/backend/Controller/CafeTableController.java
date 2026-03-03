package com.example.backend.Controller;

import com.example.backend.Model.CafeTable;
import com.example.backend.Repository.CafeTableRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/tables")
public class CafeTableController {

    private final CafeTableRepository repo;

    public CafeTableController(CafeTableRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<CafeTable> create(@RequestBody CafeTable body) {
        return ResponseEntity.ok(repo.save(body));
    }

    @GetMapping
    public ResponseEntity<List<CafeTable>> getAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CafeTable> getById(@PathVariable Integer id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CafeTable> update(@PathVariable Integer id, @RequestBody CafeTable body) {
        return repo.findById(id)
                .map(existing -> {
                    body.setTableId(id);
                    return ResponseEntity.ok(repo.save(body));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
