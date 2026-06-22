package com.fintrack.ucic_tracker.repository;

import com.fintrack.ucic_tracker.entity.Customer;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("""
        SELECT c FROM Customer c
        WHERE c.pan IS NOT NULL
        AND c.pan IN (
            SELECT c2.pan FROM Customer c2
            WHERE c2.pan IS NOT NULL
            GROUP BY c2.pan HAVING COUNT(c2.pan) > 1
        )
        ORDER BY c.pan
    """)
    List<Customer> findDuplicatesByPan();

    @Query("""
        SELECT c FROM Customer c
        WHERE c.customerId IN (
            SELECT c2.customerId FROM Customer c2
            GROUP BY c2.customerId HAVING COUNT(c2.customerId) > 1
        )
        ORDER BY c.customerId
    """)
    List<Customer> findDuplicatesByCustomerId();

    @Query("""
        SELECT c FROM Customer c
        WHERE c.voterId IS NOT NULL
        AND c.voterId IN (
            SELECT c2.voterId FROM Customer c2
            WHERE c2.voterId IS NOT NULL
            GROUP BY c2.voterId HAVING COUNT(c2.voterId) > 1
        )
        ORDER BY c.voterId
    """)
    List<Customer> findDuplicatesByVoterId();

    @Query("""
        SELECT c FROM Customer c
        WHERE c.aadhaar IS NOT NULL
        AND c.aadhaar IN (
            SELECT c2.aadhaar FROM Customer c2
            WHERE c2.aadhaar IS NOT NULL
            GROUP BY c2.aadhaar HAVING COUNT(c2.aadhaar) > 1
        )
        ORDER BY c.aadhaar
    """)
    List<Customer> findDuplicatesByAadhaar();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @Query("SELECT c FROM Customer c")
    List<Customer> findAllForAnalysis();
}
