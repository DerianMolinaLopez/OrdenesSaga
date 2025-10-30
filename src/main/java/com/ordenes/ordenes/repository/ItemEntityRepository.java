package com.ordenes.ordenes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ordenes.ordenes.models.ItemEntity;

@Repository
public interface ItemEntityRepository extends JpaRepository<ItemEntity, String> {
    @Query(value = "SELECT * FROM items WHERE correlation_id = :correlationId", nativeQuery = true)
    List<ItemEntity> findByCorrelationIdNative(@Param("correlationId") String correlationId);
}
