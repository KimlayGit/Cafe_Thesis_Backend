package com.example.backend.Service;

import com.example.backend.Model.Product;
import com.example.backend.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class ProductService {
    private final ProductRepository repo;
    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }
    public List<Product> getAll() {
        return repo.findAll();
    }

    public Product save(Product product) {
        return repo.save(product);
    }
}
