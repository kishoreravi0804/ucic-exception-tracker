package com.fintrack.ucic_tracker.repository;

import com.fintrack.ucic_tracker.entity.ExceptionGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExceptionGroupMemberRepository extends JpaRepository<ExceptionGroupMember, Long> {

    List<ExceptionGroupMember> findByExceptionGroupId(Long groupId);
}
