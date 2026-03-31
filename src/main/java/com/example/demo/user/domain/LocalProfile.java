package com.example.demo.user.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "local_profiles")
public class LocalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "business_name", nullable = false, length = 120)
    private String businessName;

    @NotBlank
    @Column(name = "food_description", nullable = false, length = 1000)
    private String foodDescription;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "street", nullable = false, length = 120)),
            @AttributeOverride(name = "number", column = @Column(name = "number", nullable = false, length = 20)),
            @AttributeOverride(name = "apartment", column = @Column(name = "apartment", length = 20)),
            @AttributeOverride(name = "city", column = @Column(name = "city", nullable = false, length = 80)),
            @AttributeOverride(name = "department", column = @Column(name = "department", length = 80)),
            @AttributeOverride(name = "referenceNote", column = @Column(name = "reference_note", length = 255))
    })
    private Address address;

    @Column(name = "approval_requested_at")
    private Instant approvalRequestedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @ElementCollection
    @CollectionTable(name = "local_profile_images", joinColumns = @JoinColumn(name = "local_profile_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image_url", nullable = false, length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    protected LocalProfile() {
    }

    public LocalProfile(String businessName, String foodDescription, Address address, Instant approvalRequestedAt) {
        this.businessName = businessName;
        this.foodDescription = foodDescription;
        this.address = address;
        this.approvalRequestedAt = approvalRequestedAt;
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Instant getApprovalRequestedAt() {
        return approvalRequestedAt;
    }

    public void setApprovalRequestedAt(Instant approvalRequestedAt) {
        this.approvalRequestedAt = approvalRequestedAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getLocalProfile() != this) {
            user.setLocalProfile(this);
        }
    }
}
