package com.elsys.safebanking.controller;

import com.elsys.safebanking.service.AppUserDetailsService;
import com.elsys.safebanking.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FrontendController.class)
@AutoConfigureMockMvc(addFilters = false)
class FrontendControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void accountsRouteForwardsToFrontend() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void profileRouteForwardsToFrontend() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }
}
