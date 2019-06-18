package org.analyzer.pullrequestanalyzer.registry;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Map;

@Data
@Service
public class DeveloperRegistry {

    private Map<String, Developer> developers;

    public Developer getDeveloperById(Long id) {
        return developers.values().stream()
                .filter(x -> x.getDeveloperId().equals(id))
                .findAny()
                .orElse(null);
    }

    public Developer getDeveloperByUsername(String username) {
        return developers.values().stream()
                .filter(x -> x.getUsername().equals(username))
                .findAny()
                .orElse(null);
    }
}
