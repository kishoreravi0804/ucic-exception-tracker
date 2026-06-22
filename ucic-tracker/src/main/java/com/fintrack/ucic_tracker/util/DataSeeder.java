package com.fintrack.ucic_tracker.util;

import com.fintrack.ucic_tracker.entity.Customer;
import com.fintrack.ucic_tracker.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final Random random = new Random(42);
    private int customerCounter = 1;

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) {
            log.info("Database already seeded. Skipping. Total: {}", customerRepository.count());
            return;
        }

        log.info("Seeding 50,000 customer records...");
        long start = System.currentTimeMillis();

        List<Customer> batch = new ArrayList<>();
        int batchSize = 500;

        // Pattern 1 — Same PAN, different Customer IDs (~3,000 records)
        log.info("Pattern 1: Same PAN, different Customer IDs...");
        for (int g = 0; g < 1000; g++) {
            String sharedPan  = generatePan();
            String sharedName = generateName();
            int size = 2 + random.nextInt(3);
            for (int i = 0; i < size; i++) {
                Customer c = new Customer();
                c.setCustomerId(generateCustomerId());
                c.setFullName(sharedName);
                c.setPan(sharedPan);
                c.setVoterId(generateVoterId());
                c.setAadhaar(generateAadhaar());
                c.setDl(generateDl());
                c.setPassport(random.nextBoolean() ? generatePassport() : null);
                c.setMobile(generateMobile());
                c.setEmail(generateEmail(sharedName, i));
                batch.add(c);
                flushIfNeeded(batch, batchSize);
            }
        }

        // Pattern 2 — Same Customer ID, different PANs (~2,000 records)
        log.info("Pattern 2: Same Customer ID, different PANs...");
        for (int g = 0; g < 800; g++) {
            String sharedCustId = generateCustomerId();
            int size = 2 + random.nextInt(2);
            for (int i = 0; i < size; i++) {
                Customer c = new Customer();
                c.setCustomerId(sharedCustId);
                c.setFullName(generateName());
                c.setPan(generatePan());
                c.setVoterId(generateVoterId());
                c.setAadhaar(generateAadhaar());
                c.setDl(null);
                c.setPassport(null);
                c.setMobile(generateMobile());
                c.setEmail(generateEmail(c.getFullName(), i));
                batch.add(c);
                flushIfNeeded(batch, batchSize);
            }
        }

        // Pattern 3 — Same Voter ID (~2,000 records)
        log.info("Pattern 3: Same Voter ID...");
        for (int g = 0; g < 700; g++) {
            String sharedVoter = generateVoterId();
            int size = 2 + random.nextInt(2);
            for (int i = 0; i < size; i++) {
                Customer c = new Customer();
                c.setCustomerId(generateCustomerId());
                c.setFullName(generateName());
                c.setPan(generatePan());
                c.setVoterId(sharedVoter);
                c.setAadhaar(generateAadhaar());
                c.setDl(generateDl());
                c.setPassport(null);
                c.setMobile(generateMobile());
                c.setEmail(generateEmail(c.getFullName(), i));
                batch.add(c);
                flushIfNeeded(batch, batchSize);
            }
        }

        // Pattern 4 — Same Aadhaar (~1,500 records)
        log.info("Pattern 4: Same Aadhaar...");
        for (int g = 0; g < 500; g++) {
            String sharedAadhaar = generateAadhaar();
            int size = 2 + random.nextInt(2);
            for (int i = 0; i < size; i++) {
                Customer c = new Customer();
                c.setCustomerId(generateCustomerId());
                c.setFullName(generateName());
                c.setPan(generatePan());
                c.setVoterId(generateVoterId());
                c.setAadhaar(sharedAadhaar);
                c.setDl(null);
                c.setPassport(null);
                c.setMobile(generateMobile());
                c.setEmail(generateEmail(c.getFullName(), i));
                batch.add(c);
                flushIfNeeded(batch, batchSize);
            }
        }

        // Pattern 5 — Multiple KYC issues (~1,500 records)
        log.info("Pattern 5: Multiple KYC issues...");
        for (int g = 0; g < 400; g++) {
            String sharedPan   = generatePan();
            String sharedVoter = generateVoterId();
            int size = 2 + random.nextInt(3);
            for (int i = 0; i < size; i++) {
                Customer c = new Customer();
                c.setCustomerId(generateCustomerId());
                c.setFullName(generateName());
                c.setPan(sharedPan);
                c.setVoterId(sharedVoter);
                c.setAadhaar(generateAadhaar());
                c.setDl(generateDl());
                c.setPassport(random.nextBoolean() ? generatePassport() : null);
                c.setMobile(generateMobile());
                c.setEmail(generateEmail(c.getFullName(), i));
                batch.add(c);
                flushIfNeeded(batch, batchSize);
            }
        }

        // Clean records — remaining up to 50K
        log.info("Seeding clean records...");
        long seededSoFar = customerRepository.count() + batch.size();
        int cleanCount = (int)(50000 - seededSoFar);
        for (int i = 0; i < cleanCount; i++) {
            Customer c = new Customer();
            c.setCustomerId(generateCustomerId());
            c.setFullName(generateName());
            c.setPan(generatePan());
            c.setVoterId(generateVoterId());
            c.setAadhaar(generateAadhaar());
            c.setDl(random.nextBoolean() ? generateDl() : null);
            c.setPassport(random.nextInt(5) == 0 ? generatePassport() : null);
            c.setMobile(generateMobile());
            c.setEmail(generateEmail(c.getFullName(), i));
            batch.add(c);
            flushIfNeeded(batch, batchSize);
        }

        if (!batch.isEmpty()) customerRepository.saveAll(batch);

        long elapsed = System.currentTimeMillis() - start;
        log.info("Seeding complete! Total: {} | Time: {}ms",
                customerRepository.count(), elapsed);
    }

    private void flushIfNeeded(List<Customer> batch, int size) {
        if (batch.size() >= size) {
            customerRepository.saveAll(batch);
            batch.clear();
        }
    }

    private String generateCustomerId() {
        return String.format("CUST%06d", customerCounter++);
    }

    private String generatePan() {
        String l = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return "" + l.charAt(random.nextInt(26))
                   + l.charAt(random.nextInt(26))
                   + l.charAt(random.nextInt(26))
                   + l.charAt(random.nextInt(26))
                   + l.charAt(random.nextInt(26))
                   + (char)('1' + random.nextInt(9))
                   + (char)('0' + random.nextInt(10))
                   + (char)('0' + random.nextInt(10))
                   + (char)('0' + random.nextInt(10))
                   + l.charAt(random.nextInt(26));
    }

    private String generateVoterId() {
        String l = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return "" + l.charAt(random.nextInt(26))
                   + l.charAt(random.nextInt(26))
                   + l.charAt(random.nextInt(26))
                   + String.format("%07d", random.nextInt(9999999));
    }

    private String generateAadhaar() {
        return String.format("%04d%04d%04d",
                1000 + random.nextInt(8999),
                random.nextInt(10000),
                random.nextInt(10000));
    }

    private String generateDl() {
        String[] states = {"MH", "DL", "KA", "TN", "UP", "GJ", "RJ", "WB"};
        return states[random.nextInt(states.length)]
                + String.format("%02d", random.nextInt(99))
                + String.format("%011d", (long)(random.nextDouble() * 99999999999L));
    }

    private String generatePassport() {
        String l = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return "" + l.charAt(random.nextInt(26))
                   + String.format("%07d", random.nextInt(9999999));
    }

    private String generateMobile() {
        int[] prefixes = {98, 97, 96, 95, 94, 93, 92, 91, 90,
                          89, 88, 87, 86, 85, 84, 83, 82, 81, 80};
        return prefixes[random.nextInt(prefixes.length)]
                + String.format("%08d", random.nextInt(99999999));
    }

    private String generateName() {
        String[] first = {"Amit", "Priya", "Rahul", "Sneha", "Vikram",
                          "Pooja", "Arjun", "Neha", "Suresh", "Kavya",
                          "Ravi", "Anita", "Kiran", "Deepa", "Manoj",
                          "Sunita", "Ajay", "Meera", "Rohit", "Divya"};
        String[] last  = {"Sharma", "Patel", "Singh", "Kumar", "Reddy",
                          "Nair", "Iyer", "Joshi", "Gupta", "Verma",
                          "Mehta", "Shah", "Das", "Rao", "Pillai",
                          "Menon", "Naidu", "Chauhan", "Tiwari", "Mishra"};
        return first[random.nextInt(first.length)] + " "
             + last[random.nextInt(last.length)];
    }

    private String generateEmail(String name, int suffix) {
        String base = name.toLowerCase().replace(" ", ".") + suffix;
        String[] domains = {"gmail.com", "yahoo.com", "hotmail.com", "rediffmail.com"};
        return base + "@" + domains[random.nextInt(domains.length)];
    }
}
