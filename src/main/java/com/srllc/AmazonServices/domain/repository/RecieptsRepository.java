package com.srllc.AmazonServices.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.srllc.AmazonServices.domain.entity.Reciepts;

@Repository
public interface RecieptsRepository extends JpaRepository<Reciepts, Long> {
}