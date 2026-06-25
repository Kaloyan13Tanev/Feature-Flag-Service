package bg.sofia.interview.bosch.service;

import bg.sofia.interview.bosch.dto.FeatureFlagPatchDTO;
import bg.sofia.interview.bosch.model.FeatureFlag;
import bg.sofia.interview.bosch.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeatureFlagServiceImplTest {

    private static final Long PUBLIC_ID = 1L;

    @Mock
    private FeatureFlagRepository repo;

    private FeatureFlag flag1 = new FeatureFlag();
    private FeatureFlag flag2 = new FeatureFlag();
    private FeatureFlagServiceImpl featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagService = new FeatureFlagServiceImpl(repo);

        flag1 = new FeatureFlag();
        flag1.setId(1L);
        flag1.setName("flag1");
        flag1.setDescription("First flag");
        flag1.setEnabled(true);

        flag2 = new FeatureFlag();
        flag2.setId(2L);
        flag2.setName("flag2");
        flag2.setDescription("Second flag");
        flag2.setEnabled(false);
    }

    @Test
    void testGetFeatureFlags() {
        when(repo.findAll()).thenReturn(List.of(flag1, flag2));

        List<FeatureFlag> result = featureFlagService.getFeatureFlags();

        assertEquals(2, result.size());
        assertEquals(flag1, result.get(0));
        assertEquals(flag2, result.get(1));
        verify(repo).findAll();
    }

    @Test
    void testGetFeatureFlag() {
        when(repo.findById(PUBLIC_ID)).thenReturn(Optional.of(flag1));

        FeatureFlag test = featureFlagService.getFeatureFlag(PUBLIC_ID);

        assertEquals(flag1, test);
    }

    @Test
    void testGetFeatureFlagNonExistent() {
        when(repo.findById(PUBLIC_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> featureFlagService.getFeatureFlag(PUBLIC_ID)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testCreateFeatureFlag() {
        when(repo.findByName(flag1.getName())).thenReturn(Optional.empty());
        when(repo.save(flag1)).thenReturn(flag1);

        FeatureFlag test = featureFlagService.createFeatureFlag(flag1);

        assertEquals(flag1, test);
    }

    @Test
    void testCreateFeatureFlagNameAlreadyExists() {
        when(repo.findByName(flag1.getName())).thenReturn(Optional.of(flag1));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> featureFlagService.createFeatureFlag(flag1));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void testUpdateFeatureFlag() {
        FeatureFlagPatchDTO update = new FeatureFlagPatchDTO(null, null, false);
        when(repo.findById(PUBLIC_ID)).thenReturn(Optional.of(flag1));
        when(repo.save(flag1)).thenReturn(flag1);

        FeatureFlag test = featureFlagService.updateFeatureFlag(PUBLIC_ID, update);

        assertEquals(flag1, test);
        verify(repo).save(flag1);
    }

    @Test
    void testUpdateFeatureFlagNonExistent() {
        FeatureFlagPatchDTO update = new FeatureFlagPatchDTO(null, null, false);
        when(repo.findById(PUBLIC_ID)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> featureFlagService.updateFeatureFlag(PUBLIC_ID, update));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testUpdateFeatureFlagOnlyName() {
        FeatureFlagPatchDTO update = new FeatureFlagPatchDTO("new name", null, null);
        when(repo.findById(PUBLIC_ID)).thenReturn(Optional.of(flag1));
        when(repo.save(flag1)).thenReturn(flag1);

        FeatureFlag result = featureFlagService.updateFeatureFlag(PUBLIC_ID, update);

        assertEquals("new name", result.getName());
        assertEquals(flag1.getDescription(), result.getDescription());
        assertEquals(flag1.isEnabled(), result.isEnabled());
    }

    @Test
    void testUpdateFeatureFlagOnlyDescription() {
        FeatureFlagPatchDTO update = new FeatureFlagPatchDTO(null, "new description", null);
        when(repo.findById(PUBLIC_ID)).thenReturn(Optional.of(flag1));
        when(repo.save(flag1)).thenReturn(flag1);

        FeatureFlag result = featureFlagService.updateFeatureFlag(PUBLIC_ID, update);

        assertEquals(flag1.getName(), result.getName());
        assertEquals("new description", result.getDescription());
        assertEquals(flag1.isEnabled(), result.isEnabled());
    }

    @Test
    void testUpdateFeatureFlagOnlyEnabled() {
        FeatureFlagPatchDTO update = new FeatureFlagPatchDTO(null, null, false);
        when(repo.findById(PUBLIC_ID)).thenReturn(Optional.of(flag1));
        when(repo.save(flag1)).thenReturn(flag1);

        FeatureFlag result = featureFlagService.updateFeatureFlag(PUBLIC_ID, update);

        assertEquals(flag1.getName(), result.getName());
        assertEquals(flag1.getDescription(), result.getDescription());
        assertFalse(result.isEnabled());
    }

    @Test
    void testDeleteFeatureFlag() {
        when(repo.existsById(PUBLIC_ID)).thenReturn(true);

        featureFlagService.deleteFeatureFlag(PUBLIC_ID);

        verify(repo).deleteById(PUBLIC_ID);
    }

    @Test
    void testDeleteFeatureFlagNonExistent() {
        when(repo.existsById(PUBLIC_ID)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> featureFlagService.deleteFeatureFlag(PUBLIC_ID));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testEvaluateFeatureFlag() {
        when(repo.findByName(flag1.getName())).thenReturn(Optional.of(flag1));

        boolean result = featureFlagService.evaluateFeatureFlag(flag1.getName());

        assertTrue(result);
    }

    @Test
    void testEvaluateFeatureFlagNonExistent() {
        when(repo.findByName(flag1.getName())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> featureFlagService.evaluateFeatureFlag(flag1.getName()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

}
