package com.example.backend.Controller;

import com.example.backend.Model.Ingredient;
import com.example.backend.Repository.IngredientRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@CrossOrigin(origins = "http://localhost:5173")
public class IngredientController {

    private final IngredientRepository repo;

    public IngredientController(IngredientRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Ingredient> getAll() {
        return repo.findAll();
    }

    @GetMapping("/low-stock")
    public List<Ingredient> getLowStock() {
        return repo.findLowStockIngredients();
    }

    @PostMapping
    public Ingredient create(@RequestBody Ingredient ingredient) {
        return repo.save(ingredient);
    }

    @PutMapping("/{id}")
    public Ingredient update(@PathVariable Long id, @RequestBody Ingredient body) {
        body.setIngredientId(id);
        return repo.save(body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}