package com.ordenes.ordenes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ordenes.ordenes.models.ItemEntity;

@Repository
public interface ItemEntityRepository extends JpaRepository<ItemEntity, String> {
    
}
