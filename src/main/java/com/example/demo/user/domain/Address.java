package com.example.demo.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    @Column(name = "street", nullable = false, length = 120)
    private String street;

    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Column(name = "apartment", length = 20)
    private String apartment;

    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Column(name = "department", length = 80)
    private String department;

    @Column(name = "reference_note", length = 255)
    private String referenceNote;

    protected Address() {
    }

    public Address(String street, String number, String apartment, String city, String department, String referenceNote) {
        this.street = street;
        this.number = number;
        this.apartment = apartment;
        this.city = city;
        this.department = department;
        this.referenceNote = referenceNote;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getReferenceNote() {
        return referenceNote;
    }

    public void setReferenceNote(String referenceNote) {
        this.referenceNote = referenceNote;
    }
}
