package bg.sofia.interview.bosch.service;

import bg.sofia.interview.bosch.dto.FeatureFlagPatchDTO;
import bg.sofia.interview.bosch.model.FeatureFlag;

import java.util.List;

public interface FeatureFlagService {

    List<FeatureFlag> getFeatureFlags();

    FeatureFlag getFeatureFlag(Long id);

    FeatureFlag createFeatureFlag(FeatureFlag featureFlag);

    FeatureFlag updateFeatureFlag(Long id, FeatureFlagPatchDTO update);

    void deleteFeatureFlag(Long id);

    boolean evaluateFeatureFlag(String name);

}
