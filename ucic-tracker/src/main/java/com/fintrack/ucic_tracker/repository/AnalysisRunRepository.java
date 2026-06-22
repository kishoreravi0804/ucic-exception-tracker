package com.fintrack.ucic_tracker.repository;

import com.fintrack.ucic_tracker.entity.AnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, Long> {

    List<AnalysisRun> findTop5ByOrderByRunAtDesc();
}
