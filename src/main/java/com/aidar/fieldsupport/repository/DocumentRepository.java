package com.aidar.fieldsupport.repository;

import com.aidar.fieldsupport.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByCategoryAndIsCurrentTrue(String category);
    List<Document> findByEquipmentContainingIgnoreCaseAndIsCurrentTrue(String equipment);
    List<Document> findByCategoryAndEquipmentContainingIgnoreCaseAndIsCurrentTrue(String category, String equipment);
}