package com.elsys.safebanking.controller;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elsys.safebanking.model.User;
import com.elsys.safebanking.model.UserRole;
import com.elsys.safebanking.repository.UserRepository;
import com.elsys.safebanking.validation.EPinPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    void registrationAcceptsManualEPinAndStoresOnlyItsHash() throws Exception {
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
                .andExpect(jsonPath("$.oneTimeEPin").isEmpty())
                .andReturn();

        User savedUser = userRepository.findByEmailIgnoreCase("client@example.com").orElseThrow();
        assertNotEquals("123456", savedUser.getEPinHash());
        assertTrue(passwordEncoder.matches("123456", savedUser.getEPinHash()));
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
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        String generatedEPin = response.get("oneTimeEPin").asText();
        assertTrue(generatedEPin.matches("\\d{6}"));

        User savedUser = userRepository.findByEmailIgnoreCase("client@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches(generatedEPin, savedUser.getEPinHash()));
    }

    @Test
    void registrationFlushesEPinHashBeforeReturningToken() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");

        User savedUser = userRepository.findByEmailIgnoreCase("client@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("123456", savedUser.getEPinHash()));

        mockMvc.perform(get("/api/users/e-pin/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.set").value(true))
                .andExpect(jsonPath("$.ePin").doesNotExist());
    }

    @Test
    void testsUseProductionPasswordEncoderBean() {
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
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
    void registrationAcceptsLeadingZeroEPin() throws Exception {
        registerWithEPin("client@example.com", "012345");
        User savedUser = userRepository.findByEmailIgnoreCase("client@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("012345", savedUser.getEPinHash()));
    }

    @Test
    void registrationRejectsEPinEdgeCases() throws Exception {
        String[] invalidValues = {"12345", "1234567", "12345a", "      "};

        for (int index = 0; index < invalidValues.length; index++) {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of(
                                    "email", "invalid" + index + "@example.com",
                                    "password", "strongPassword123",
                                    "firstName", "Alex",
                                    "lastName", "Morgan",
                                    "dateOfBirth", "1995-04-12",
                                    "ePin", invalidValues[index]
                            ))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.ePin").exists());
        }
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
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.oneTimeEPin").isEmpty());
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
    void authenticatedUserCanChangeEPinWithCurrentPasswordAndEPin() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "currentEPin", "123456",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.set").value(true))
                .andExpect(jsonPath("$.ePin").doesNotExist());

        mockMvc.perform(get("/api/users/e-pin/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.set").value(true));

        User savedUser = userRepository.findByEmailIgnoreCase("client@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("654321", savedUser.getEPinHash()));
        assertFalse(passwordEncoder.matches("123456", savedUser.getEPinHash()));
    }

    @Test
    void authenticatedUserCanVerifyEPinWithoutExposingIt() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");

        mockMvc.perform(post("/api/users/e-pin/verify")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("ePin", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.ePin").doesNotExist());
    }

    @Test
    void verifyEPinRejectsWrongOrMalformedPin() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");

        mockMvc.perform(post("/api/users/e-pin/verify")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("ePin", "000000"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("E-PIN Verification Failed"))
                .andExpect(jsonPath("$.message").value("Current password or E-PIN is incorrect"));

        mockMvc.perform(post("/api/users/e-pin/verify")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("ePin", "12345"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.ePin").exists());
    }

    @Test
    void changeEPinRejectsWrongPasswordAndInvalidValue() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "wrongPassword",
                                "currentEPin", "123456",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("E-PIN Verification Failed"))
                .andExpect(jsonPath("$.message").value("Current password or E-PIN is incorrect"));

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "currentEPin", "000000",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("E-PIN Verification Failed"))
                .andExpect(jsonPath("$.message").value("Current password or E-PIN is incorrect"));

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "currentEPin", "123456",
                                "newEPin", "12345"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newEPin").exists());
    }

    @Test
    void changeEPinRateLimitsAfterRepeatedFailures() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");
        String clientIp = "203.0.113.10";

        for (int attempt = 0; attempt < EPinPolicy.MAX_FAILED_ATTEMPTS; attempt++) {
            mockMvc.perform(put("/api/users/e-pin")
                            .header("Authorization", "Bearer " + token)
                            .header("X-Forwarded-For", clientIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of(
                                    "currentPassword", "strongPassword123",
                                    "currentEPin", "000000",
                                    "newEPin", "654321"
                            ))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Current password or E-PIN is incorrect"));
        }

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Forwarded-For", clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "currentEPin", "000000",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Too Many E-PIN Attempts"))
                .andExpect(jsonPath("$.ePin").doesNotExist());
    }

    @Test
    void changeEPinRejectsMissingCurrentEPinAndReuse() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "currentEPin", "",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.currentEPin").exists());

        mockMvc.perform(put("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "currentEPin", "123456",
                                "newEPin", "123456"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid E-PIN Change"))
                .andExpect(jsonPath("$.message").value("New E-PIN must be different from the current E-PIN"));
    }

    @Test
    void legacyReversibleValueIsTreatedAsUnsetAndCanBeReplaced() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");
        User user = userRepository.findByEmailIgnoreCase("client@example.com").orElseThrow();
        String legacyValue = "legacy-reversible-value";
        user.updateEPinHash(legacyValue);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/users/e-pin/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.set").value(false))
                .andExpect(jsonPath("$.ePin").doesNotExist());

        mockMvc.perform(post("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.set").value(true));

        User updatedUser = userRepository.findByEmailIgnoreCase("client@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("654321", updatedUser.getEPinHash()));
    }

    @Test
    void existingUserCanSetEPinOnceWithCurrentPassword() throws Exception {
        userRepository.save(new User(
                "existing@example.com",
                passwordEncoder.encode("strongPassword123"),
                "Existing",
                "User"
        ));
        String token = login("existing@example.com", "strongPassword123");

        mockMvc.perform(get("/api/users/e-pin/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.set").value(false));

        mockMvc.perform(post("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "wrongPassword",
                                "newEPin", "123456"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Current password or E-PIN is incorrect"));

        mockMvc.perform(post("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "newEPin", "123456"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.set").value(true));

        User updatedUser = userRepository.findByEmailIgnoreCase("existing@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("123456", updatedUser.getEPinHash()));

        mockMvc.perform(post("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "currentPassword", "strongPassword123",
                                "newEPin", "654321"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("E-PIN Already Set"));
    }

    @Test
    void plaintextEPinRetrievalEndpointIsNotAvailable() throws Exception {
        String token = registerWithEPin("client@example.com", "123456");

        mockMvc.perform(get("/api/users/e-pin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isMethodNotAllowed());
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

    private String registerWithEPin(String email, String ePin) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", "strongPassword123",
                                "firstName", "Alex",
                                "lastName", "Morgan",
                                "dateOfBirth", "1995-04-12",
                                "ePin", ePin
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
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
