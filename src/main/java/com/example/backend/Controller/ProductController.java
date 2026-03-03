package com.example.backend.Controller;

import com.example.backend.Model.Category;
import com.example.backend.Model.Product;
import com.example.backend.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository repo;

    @Value("${app.upload.products-dir:uploads/products}")
    private String uploadDir;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    // ──────────────────────────────
    // Upload product image
    // ──────────────────────────────
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            // Use UUID to avoid filename collisions & path traversal attacks
            String safeFilename = UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(safeFilename);

            // Copy file safely
            Files.copy(file.getInputStream(), targetPath);

            // Return URL that frontend can use in <img src="...">
            String imageUrl = "/api/products/images/" + safeFilename;
            return ResponseEntity.ok(imageUrl);

        } catch (IOException e) {
            // In production: use logger instead of printStackTrace
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
        }
    }

    // ──────────────────────────────
    // Serve uploaded images
    // ──────────────────────────────
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {

        // Basic protection against path traversal attacks
        if (filename == null || filename.trim().isEmpty() ||
                filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Resolve the file path safely
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();

            // Use FileSystemResource (recommended - avoids URI/MalformedURLException issues)
            Resource resource = new FileSystemResource(filePath.toFile());

            // Check if file exists and is readable
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Detect content type (jpg, png, etc.)
            String contentType = Files.probeContentType(filePath);
            MediaType mediaType = (contentType != null)
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;

            // Build response with inline display (browser shows image instead of download)
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // optional: cache 1 hour
                    .body(resource);

        } catch (IOException e) {
            // Log error (in production use proper logger like SLF4J)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // or return custom error message
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Your existing CRUD endpoints (unchanged)
    // ─────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product body) {
        return ResponseEntity.ok(repo.save(body));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean available
    ) {
        String q = (search == null) ? null : search.trim();
        List<Product> result;

        if (categoryId != null && q != null && !q.isBlank()) {
            result = repo.findByCategory_CategoryIdAndProductNameContainingIgnoreCase(categoryId, q);
        } else if (categoryId != null) {
            result = repo.findByCategory_CategoryId(categoryId);
        } else if (q != null && !q.isBlank()) {
            result = repo.findByProductNameContainingIgnoreCase(q);
        } else {
            result = repo.findAll();
        }

        if (available) {
            result.removeIf(p -> Boolean.TRUE != p.getAvailable());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Integer id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Integer id, @RequestBody Product body) {
        return repo.findById(id)
                .map(existing -> {
                    body.setProductId(id);
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