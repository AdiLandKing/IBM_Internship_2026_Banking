package com.elsys.safebanking.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elsys.safebanking.model.User;
import com.elsys.safebanking.model.UserRole;
import com.elsys.safebanking.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesUserAndReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "client@example.com",
                                "password", "strongPassword123",
                                "firstName", "Alex",
                                "lastName", "Morgan",
                                "dateOfBirth", "1995-04-12"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.user.email").value("client@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("Alex"))
                .andExpect(jsonPath("$.user.lastName").value("Morgan"))
                .andExpect(jsonPath("$.user.dateOfBirth").value("1995-04-12"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.ePin").doesNotExist());
    }

    @Test
    void registrationAcceptsManualEPinAndStoresItEncrypted() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "client@example.com",
                                "password", "strongPassword123",
                                "firstName", "Alex",
                                "lastName", "Morgan",
                                "dateOfBirth", "1995-04-12",
                                "ePin", "123456"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.ePin").doesNotExist())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asText();

        mockMvc.perform(get("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ePin").value("123456"));

        User savedUser = userRepository.findByEmailIgnoreCase("client@example.com").orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals("123456", savedUser.getEPin());
    }

    @Test
    void registrationGeneratesEPinWhenEmpty() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "client@example.com",
                                "password", "strongPassword123",
                                "firstName", "Alex",
                                "lastName", "Morgan",
                                "dateOfBirth", "1995-04-12",
                                "ePin", ""
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asText();

        mockMvc.perform(get("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ePin").value(matchesPattern("\\d{6}")));
    }

    @Test
    void registrationRejectsInvalidEPin() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "client@example.com",
                                "password", "strongPassword123",
                                "firstName", "Alex",
                                "lastName", "Morgan",
                                "dateOfBirth", "1995-04-12",
                                "ePin", "12345"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.ePin").value("E-PIN must contain exactly 6 digits"));
    }

    @Test
    void loginReturnsTokenForRegisteredUser() throws Exception {
        register("client@example.com", "strongPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "client@example.com",
                                "password", "strongPassword123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.user.email").value("client@example.com"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void duplicateRegistrationReturnsConflict() throws Exception {
        register("client@example.com", "strongPassword123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "CLIENT@example.com",
                                "password", "anotherPassword123",
                                "firstName", "Casey",
                                "lastName", "Rivera",
                                "dateOfBirth", "1991-08-24"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists"));
    }

    @Test
    void invalidLoginReturnsUnauthorized() throws Exception {
        register("client@example.com", "strongPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "client@example.com",
                                "password", "wrongPassword"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void profileRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanReadAndUpdateProfile() throws Exception {
        String token = register("client@example.com", "strongPassword123");

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("client@example.com"))
                .andExpect(jsonPath("$.firstName").value("Alex"));

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "firstName", "Jordan",
                                "lastName", "Stone"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("client@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jordan"))
                .andExpect(jsonPath("$.lastName").value("Stone"));
    }

    @Test
    void authenticatedUserCanChangeEPinWithCurrentPassword() throws Exception {
        String token = register("client@example.com", "strongPassword123");

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ePin").value("654321"));

        mockMvc.perform(get("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ePin").value("654321"));
    }

    @Test
    void changeEPinRejectsWrongPasswordAndInvalidValue() throws Exception {
        String token = register("client@example.com", "strongPassword123");

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "wrongPassword",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "newEPin", "12345"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newEPin").exists());
    }

    @Test
    void regularUserCannotAccessAdminSession() throws Exception {
        String token = register("client@example.com", "strongPassword123");

        mockMvc.perform(get("/api/admin/session")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUserCanAccessAdminSession() throws Exception {
        userRepository.save(new User(
                "admin@example.com",
                passwordEncoder.encode("adminPassword123"),
                "Admin",
                "User",
                UserRole.ADMIN
        ));

        String token = login("admin@example.com", "adminPassword123");

        mockMvc.perform(get("/api/admin/session")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void invalidRegisterRequestReturnsValidationErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "not-an-email",
                                "password", "short",
                                "firstName", "",
                                "lastName", "Morgan"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists());
    }

    private String register(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password,
                                "firstName", "Alex",
                                "lastName", "Morgan",
                                "dateOfBirth", "1995-04-12"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("accessToken").asText();
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("accessToken").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
