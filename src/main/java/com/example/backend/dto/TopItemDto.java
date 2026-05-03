package com.example.backend.dto;

public class TopItemDto {
    private String name;
    private Long quantity;
    private String image;

    public TopItemDto(String name, Long quantity, String image) {
        this.name = name;
        this.quantity = quantity;
        this.image = image;
    }

    public String getName() { return name; }
    public Long getQuantity() { return quantity; }
    public String getImage() { return image; }
}