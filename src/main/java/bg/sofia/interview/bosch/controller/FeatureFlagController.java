package bg.sofia.interview.bosch.controller;

import bg.sofia.interview.bosch.dto.FeatureFlagPatchDTO;
import bg.sofia.interview.bosch.dto.FlagEvaluationResponse;
import bg.sofia.interview.bosch.model.FeatureFlag;
import bg.sofia.interview.bosch.service.FeatureFlagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/flags")
public class FeatureFlagController {

    private final FeatureFlagService service;

    public FeatureFlagController(FeatureFlagService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createFeatureFlag(@RequestBody FeatureFlag flag) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFeatureFlag(flag));
    }

    @GetMapping
    public ResponseEntity<List<FeatureFlag>> getAllFeatureFlags() {
        return ResponseEntity.ok(service.getFeatureFlags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureFlag> getFeatureFlag(@PathVariable Long id) {
        return ResponseEntity.ok(service.getFeatureFlag(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FeatureFlag> updateFeatureFlag(@PathVariable Long id,
                                                         @RequestBody FeatureFlagPatchDTO update) {
        return ResponseEntity.ok(service.updateFeatureFlag(id, update));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlag(@PathVariable Long id) {
        service.deleteFeatureFlag(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{name}/evaluate")
    public ResponseEntity<?> evaluateFlag(@PathVariable String name) {
        boolean enabled = service.evaluateFeatureFlag(name);
        return ResponseEntity.ok(new FlagEvaluationResponse(name, enabled));
    }

}
