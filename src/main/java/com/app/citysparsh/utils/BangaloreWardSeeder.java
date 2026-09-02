package com.app.citysparsh.utils;

import com.app.citysparsh.model.Ward;
import com.app.citysparsh.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BangaloreWardSeeder implements CommandLineRunner {

    @Autowired
    private WardRepository wardRepo;


    @Override
    public void run(String... args) throws Exception {
        if (wardRepo.count() > 0) {
            System.out.println(">>> Wards already seeded, skipping.");
            return;
        }

        List<Ward> wards = new ArrayList<>();

        ClassPathResource resource = new ClassPathResource("data/bbmp_wards.csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // skip header row
            String[] columns = header.split(",");
            // find column indices dynamically so CSV column order doesn't matter
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < columns.length; i++) {
                colIndex.put(columns[i].trim(), i);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = parseCsvLine(line); // see helper below — handles quoted commas

                Ward ward = new Ward();
                ward.setWardNumber(Integer.parseInt(f[colIndex.get("wardNumber")].trim()));
                ward.setWardName(f[colIndex.get("name")].trim());
                ward.setZone(getOrEmpty(f, colIndex, "zone"));
                ward.setMinLat(parseDoubleOrNull(f, colIndex, "minLat"));
                ward.setMaxLat(parseDoubleOrNull(f, colIndex, "maxLat"));
                ward.setMinLng(parseDoubleOrNull(f, colIndex, "minLng"));
                ward.setMaxLng(parseDoubleOrNull(f, colIndex, "maxLng"));

                wards.add(ward);
            }
        }

        wardRepo.saveAll(wards);
        System.out.println(">>> Seeded " + wards.size() + " Bangalore wards");
    }

    private String getOrEmpty(String[] f, Map<String, Integer> idx, String col) {
        Integer i = idx.get(col);
        if (i == null || i >= f.length) return "";
        return f[i].trim();
    }

    private Double parseDoubleOrNull(String[] f, Map<String, Integer> idx, String col) {
        String v = getOrEmpty(f, idx, col);
        return v.isEmpty() ? null : Double.parseDouble(v);
    }

    // Simple CSV line parser that handles quoted fields containing commas
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
