package com.fintrack.ucic_tracker.service;

import com.fintrack.ucic_tracker.entity.*;
import com.fintrack.ucic_tracker.enums.GroupStatus;
import com.fintrack.ucic_tracker.enums.RootCause;
import com.fintrack.ucic_tracker.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final CustomerRepository customerRepository;
    private final ExceptionGroupRepository exceptionGroupRepository;
    private final ExceptionGroupMemberRepository memberRepository;
    private final AuditLogRepository auditLogRepository;
    private final AnalysisRunRepository analysisRunRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public AnalysisRun runAnalysis() {
        long start = System.currentTimeMillis();
        log.info("Starting Union-Find analysis...");

        
        List<Customer> allCustomers = customerRepository.findAllForAnalysis();
        log.info("Loaded {} customers", allCustomers.size());

       
        UnionFind uf = new UnionFind();
        for (Customer c : allCustomers) {
            uf.add(c.getId());
        }

       
        Map<String, Long> panMap = new HashMap<>();
        for (Customer c : allCustomers) {
            if (c.getPan() != null && !c.getPan().isBlank()) {
                if (panMap.containsKey(c.getPan())) {
                    uf.union(panMap.get(c.getPan()), c.getId());
                } else {
                    panMap.put(c.getPan(), c.getId());
                }
            }
        }

        
        Map<String, Long> custIdMap = new HashMap<>();
        for (Customer c : allCustomers) {
            if (c.getCustomerId() != null && !c.getCustomerId().isBlank()) {
                if (custIdMap.containsKey(c.getCustomerId())) {
                    uf.union(custIdMap.get(c.getCustomerId()), c.getId());
                } else {
                    custIdMap.put(c.getCustomerId(), c.getId());
                }
            }
        }

        
        Map<String, Long> voterMap = new HashMap<>();
        for (Customer c : allCustomers) {
            if (c.getVoterId() != null && !c.getVoterId().isBlank()) {
                if (voterMap.containsKey(c.getVoterId())) {
                    uf.union(voterMap.get(c.getVoterId()), c.getId());
                } else {
                    voterMap.put(c.getVoterId(), c.getId());
                }
            }
        }

        
        Map<String, Long> aadhaarMap = new HashMap<>();
        for (Customer c : allCustomers) {
            if (c.getAadhaar() != null && !c.getAadhaar().isBlank()) {
                if (aadhaarMap.containsKey(c.getAadhaar())) {
                    uf.union(aadhaarMap.get(c.getAadhaar()), c.getId());
                } else {
                    aadhaarMap.put(c.getAadhaar(), c.getId());
                }
            }
        }

       
        Map<Long, List<Customer>> groups = new HashMap<>();
        for (Customer c : allCustomers) {
            Long root = uf.find(c.getId());
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(c);
        }

        
        int groupCount = 0;
        String datePrefix = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Step 8 — Save groups in batch
        List<ExceptionGroup> groupsToSave = new ArrayList<>();
        Map<ExceptionGroup, List<Customer>> groupMemberMap = new LinkedHashMap<>();

        for (Map.Entry<Long, List<Customer>> entry : groups.entrySet()) {
            List<Customer> members = entry.getValue();
            if (members.size() < 2) continue;

            groupCount++;
            RootCause rootCause = detectRootCause(members, panMap, custIdMap, voterMap, aadhaarMap);

            ExceptionGroup group = new ExceptionGroup();
            group.setGroupRef(String.format("GRP-%s-%04d", datePrefix, groupCount));
            group.setStatus(GroupStatus.PENDING);
            group.setRootCause(rootCause);
            group.setRecordCount(members.size());

            groupsToSave.add(group);
            groupMemberMap.put(group, members);
        }

        // Save all groups at once
        List<ExceptionGroup> savedGroups = exceptionGroupRepository.saveAll(groupsToSave);

        // Save all members and audit logs in batch
        List<ExceptionGroupMember> membersToSave = new ArrayList<>();
        List<AuditLog> auditsToSave = new ArrayList<>();

        for (int i = 0; i < savedGroups.size(); i++) {
            ExceptionGroup savedGroup = savedGroups.get(i);
            List<Customer> members = groupMemberMap.get(groupsToSave.get(i));

            for (int j = 0; j < members.size(); j++) {
                ExceptionGroupMember member = new ExceptionGroupMember();
                member.setExceptionGroup(savedGroup);
                member.setCustomer(members.get(j));
                member.setIsMaster(j == 0);
                membersToSave.add(member);
            }

            AuditLog audit = new AuditLog();
            audit.setExceptionGroup(savedGroup);
            audit.setAction("CREATED");
            audit.setActor("SYSTEM");
            audit.setNote("Detected by Union-Find analysis. Members: " + members.size());
            auditsToSave.add(audit);
        }

        // Save members in chunks using EntityManager for speed
        int i = 0;
        for (ExceptionGroupMember m : membersToSave) {
            entityManager.persist(m);
            if (++i % 100 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();

        // Save audit logs in chunks
        int j = 0;
        for (AuditLog a : auditsToSave) {
            entityManager.persist(a);
            if (++j % 100 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();

        long elapsed = System.currentTimeMillis() - start;
        log.info("Analysis complete. Groups found: {} | Time: {}ms", groupCount, elapsed);

    
        AnalysisRun run = new AnalysisRun();
        run.setTotalCustomers((long) allCustomers.size());
        run.setGroupsFound(groupCount);
        run.setDurationMs(elapsed);
        run.setTriggeredBy("SYSTEM");
        return analysisRunRepository.save(run);
    }

    private RootCause detectRootCause(
            List<Customer> members,
            Map<String, Long> panMap,
            Map<String, Long> custIdMap,
            Map<String, Long> voterMap,
            Map<String, Long> aadhaarMap) {

        boolean samePan      = hasDuplicate(members, Customer::getPan);
        boolean sameCustId   = hasDuplicate(members, Customer::getCustomerId);
        boolean sameVoter    = hasDuplicate(members, Customer::getVoterId);
        boolean sameAadhaar  = hasDuplicate(members, Customer::getAadhaar);

        int issueCount = (samePan ? 1 : 0) + (sameCustId ? 1 : 0)
                       + (sameVoter ? 1 : 0) + (sameAadhaar ? 1 : 0);

        if (issueCount > 1)  return RootCause.MULTIPLE_KYC_ISSUES;
        if (samePan)         return RootCause.DUPLICATE_PAN;
        if (sameCustId)      return RootCause.DUPLICATE_CUSTOMER_ID;
        if (sameVoter)       return RootCause.KYC_MISMATCH_VOTER;
        if (sameAadhaar)     return RootCause.KYC_MISMATCH_AADHAAR;

        return RootCause.UNIDENTIFIED;
    }

    private boolean hasDuplicate(List<Customer> members,
                                  java.util.function.Function<Customer, String> getter) {
        Set<String> seen = new HashSet<>();
        for (Customer c : members) {
            String val = getter.apply(c);
            if (val != null && !val.isBlank()) {
                if (!seen.add(val)) return true;
            }
        }
        return false;
    }
}
