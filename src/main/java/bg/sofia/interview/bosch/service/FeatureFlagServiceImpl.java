package bg.sofia.interview.bosch.service;

import bg.sofia.interview.bosch.dto.FeatureFlagPatchDTO;
import bg.sofia.interview.bosch.model.FeatureFlag;
import bg.sofia.interview.bosch.repository.FeatureFlagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FeatureFlagServiceImpl implements FeatureFlagService {

    private final FeatureFlagRepository repo;

    public FeatureFlagServiceImpl(FeatureFlagRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<FeatureFlag> getFeatureFlags() {
        return repo.findAll();
    }

    @Override
    public FeatureFlag getFeatureFlag(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flag not found"));
    }

    @Override
    public FeatureFlag createFeatureFlag(FeatureFlag featureFlag) {
        if (repo.findByName(featureFlag.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Flag '" + featureFlag.getName() + "' already exists");
        }

        return repo.save(featureFlag);
    }

    @Override
    public FeatureFlag updateFeatureFlag(Long id, FeatureFlagPatchDTO update) {
        FeatureFlag flag = getFeatureFlag(id);
        if (update.name() != null) flag.setName(update.name());
        if (update.description() != null) flag.setDescription(update.description());
        if (update.enabled() != null) flag.setEnabled(update.enabled());

        return repo.save(flag);
    }

    @Override
    public void deleteFeatureFlag(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flag not found");
        }

        repo.deleteById(id);
    }

    @Override
    public boolean evaluateFeatureFlag(String name) {
        FeatureFlag flag = repo.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Flag '" + name + "' not found"));

        return flag.isEnabled();
    }
}
