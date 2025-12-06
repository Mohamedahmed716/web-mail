package com.mailSystem.demo.dal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mailSystem.demo.model.EmailFilter;
import com.mailSystem.demo.utils.Constants;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FilterFileAccess {

    private File getFiltersFile(String userEmail) {
        String userPath = Constants.DATA_DIR + "/" + userEmail;
        File dir = new File(userPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "filters.json");
    }

    public List<EmailFilter> loadUserFilters(String userEmail) {
        File file = getFiltersFile(userEmail);
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return JsonMapper.getInstance().readValue(file, new TypeReference<List<EmailFilter>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load filters for user: " + userEmail, e);
        }
    }

    public EmailFilter saveFilter(String userEmail, EmailFilter filter) {
        List<EmailFilter> filters = loadUserFilters(userEmail);

        if (filter.getId() == null) {
            filter.setId(UUID.randomUUID().toString());
        }
        filter.setUserId(userEmail);

        // Remove old version if updating
        filters.removeIf(f -> f.getId().equals(filter.getId()));
        filters.add(filter);

        saveAllFilters(userEmail, filters);
        return filter;
    }

    public void deleteFilter(String userEmail, String filterId) {
        List<EmailFilter> filters = loadUserFilters(userEmail);
        filters.removeIf(f -> f.getId().equals(filterId));
        saveAllFilters(userEmail, filters);
    }

    public Optional<EmailFilter> findFilterById(String userEmail, String filterId) {
        return loadUserFilters(userEmail).stream()
                .filter(f -> f.getId().equals(filterId))
                .findFirst();
    }

    public List<EmailFilter> getActiveFilters(String userEmail) {
        return loadUserFilters(userEmail).stream()
                .filter(EmailFilter::isActive)
                .collect(Collectors.toList());
    }

    private void saveAllFilters(String userEmail, List<EmailFilter> filters) {
        File file = getFiltersFile(userEmail);
        try {
            JsonMapper.getInstance().writeValue(file, filters);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save filters for user: " + userEmail, e);
        }
    }
}