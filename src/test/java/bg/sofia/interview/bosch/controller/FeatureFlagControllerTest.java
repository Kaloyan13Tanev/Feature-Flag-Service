package bg.sofia.interview.bosch.controller;

import bg.sofia.interview.bosch.dto.FeatureFlagPatchDTO;
import bg.sofia.interview.bosch.model.FeatureFlag;
import bg.sofia.interview.bosch.service.FeatureFlagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(FeatureFlagController.class)
class FeatureFlagControllerTest {

    private static final String BASE_PATH = "/flags";
    private static final String CREATE_FLAG_JSON =
            "{\"name\":\"test flag\",\"description\":\"Test flag\",\"enabled\":true}";
    private static final String PATCH_ENABLED_JSON = "{\"enabled\":false}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeatureFlagService service;

    private FeatureFlag flag1;
    private FeatureFlag flag2;

    @BeforeEach
    void setUp() {
        flag1 = new FeatureFlag();
        flag1.setId(1L);
        flag1.setName("test flag");
        flag1.setDescription("Test flag");
        flag1.setEnabled(true);

        flag2 = new FeatureFlag();
        flag2.setId(2L);
        flag2.setName("dark mode");
        flag2.setDescription("Enable dark mode");
        flag2.setEnabled(false);
    }

    @Test
    void testCreateFeatureFlag() throws Exception {
        when(service.createFeatureFlag(any(FeatureFlag.class))).thenReturn(flag1);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_FLAG_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(flag1.getName()))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void testGetAllFeatureFlags() throws Exception {
        when(service.getFeatureFlags()).thenReturn(List.of(flag1, flag2));

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value(flag1.getName()))
                .andExpect(jsonPath("$[1].name").value(flag2.getName()));
    }

    @Test
    void testGetFeatureFlag() throws Exception {
        when(service.getFeatureFlag(1L)).thenReturn(flag1);

        mockMvc.perform(get(BASE_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect((jsonPath("$.name").value(flag1.getName())));
    }

    @Test
    void testUpdateFeatureFlag() throws Exception {
        when(service.updateFeatureFlag(eq(1L), any(FeatureFlagPatchDTO.class))).thenReturn(flag1);

        mockMvc.perform(patch(BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_ENABLED_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(flag1.getName()))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void testDeleteFeatureFlag() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteFeatureFlag(1L);
    }

    @Test
    void testEvaluateFeatureFlag() throws Exception {
        when(service.evaluateFeatureFlag("test flag")).thenReturn(true);

        mockMvc.perform(get(BASE_PATH + "/" + flag1.getName() + "/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(flag1.getName()))
                .andExpect(jsonPath("$.enabled").value(true));
    }

}
