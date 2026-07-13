package com.careeros.preference.web;

import com.careeros.preference.application.UserPreferenceService;
import com.careeros.preference.domain.UserPreference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserPreferenceController.class)
class UserPreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserPreferenceService userPreferenceService;

    @Test
    void createReturns201() throws Exception {
        UserPreference preference = UserPreference.create(
                List.of("Backend Engineer"), List.of("Java"), List.of("Remote"), 70, true);
        when(userPreferenceService.create(any())).thenReturn(preference);

        UserPreferenceRequest request = new UserPreferenceRequest(
                List.of("Backend Engineer"), List.of("Java"), List.of("Remote"), 70, true);

        mockMvc.perform(post("/api/v1/preferences")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.minimumScore").value(70))
                .andExpect(jsonPath("$.remoteOnly").value(true));
    }

    @Test
    void createReturns400WhenMinimumScoreOutOfRange() throws Exception {
        UserPreferenceRequest request = new UserPreferenceRequest(List.of(), List.of(), List.of(), 150, false);

        mockMvc.perform(post("/api/v1/preferences")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
