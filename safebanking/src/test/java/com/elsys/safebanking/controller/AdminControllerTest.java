package com.elsys.safebanking.controller;

import com.elsys.safebanking.service.AdminService;
import com.elsys.safebanking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AdminService adminService;

    @Test
    void getAllUsers_shouldReturnStatus200() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }
}