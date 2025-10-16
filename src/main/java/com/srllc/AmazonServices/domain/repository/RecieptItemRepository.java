package com.srllc.AmazonServices.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.srllc.AmazonServices.domain.entity.RecieptItem;

@Repository
public interface RecieptItemRepository extends JpaRepository<RecieptItem, Long> {
}