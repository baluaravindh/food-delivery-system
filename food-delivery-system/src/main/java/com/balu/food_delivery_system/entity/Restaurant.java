package com.balu.food_delivery_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "restaurants")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String cuisineType;

    @Column(nullable = false)
    private LocalTime openingTime;

    @Column(nullable = false)
    private LocalTime closingTime;

    @Column(nullable = true)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean isApproved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User user;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
