package com.srllc.AmazonServices.domain.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Reciepts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String companyName;
    private String branch;
    private String managerName;
    private String cashierNumber;
    private Double subTotal;
    private Double cash;

    @Column(name = "change_amount")
    private Double change;

    @OneToMany(mappedBy = "reciepts", cascade = CascadeType.ALL)
    private List<RecieptItem> items;
}
