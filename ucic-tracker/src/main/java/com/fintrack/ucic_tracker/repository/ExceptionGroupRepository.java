package com.fintrack.ucic_tracker.repository;

import com.fintrack.ucic_tracker.entity.ExceptionGroup;
import com.fintrack.ucic_tracker.enums.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExceptionGroupRepository extends JpaRepository<ExceptionGroup, Long> {

    List<ExceptionGroup> findByStatus(GroupStatus status);

    Optional<ExceptionGroup> findByVendorToken(String vendorToken);

    Optional<ExceptionGroup> findByGroupRef(String groupRef);

    @Query("""
        SELECT COUNT(e) FROM ExceptionGroup e WHERE e.status = :status
    """)
    Long countByStatus(GroupStatus status);

    @Query("""
        SELECT e FROM ExceptionGroup e
        ORDER BY e.detectedAt DESC
    """)
    List<ExceptionGroup> findAllOrderByDetectedAtDesc();
}
