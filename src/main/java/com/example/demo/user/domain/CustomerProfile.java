package com.example.demo.user.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "identity_document", nullable = false, unique = true, length = 30)
    private String identityDocument;

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    protected CustomerProfile() {
    }

    public CustomerProfile(String identityDocument, String firstName, String lastName, Address address) {
        this.identityDocument = identityDocument;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }

    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getCustomerProfile() != this) {
            user.setCustomerProfile(this);
        }
    }
}
