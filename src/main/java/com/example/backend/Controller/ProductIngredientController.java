package com.example.backend.Controller;
import com.example.backend.Model.ProductIngredient;
import com.example.backend.Repository.ProductIngredientRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/product-ingredients")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductIngredientController {

    private final ProductIngredientRepository repo;

    public ProductIngredientController(ProductIngredientRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/product/{productId}")
    public List<ProductIngredient> getByProduct(@PathVariable Long productId) {
        return repo.findByProduct_ProductId(productId);
    }

    @PostMapping
    public ProductIngredient create(@RequestBody ProductIngredient body) {
        return repo.save(body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}