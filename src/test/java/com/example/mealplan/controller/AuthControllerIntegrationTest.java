package com.example.mealplan.controller;

import com.example.mealplan.dto.LoginRequest;
import com.example.mealplan.dto.RegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_validData_returns200() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("integrationuser");
        request.setEmail("integration@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void register_duplicateUsername_returns400() throws Exception {
        RegistrationRequest request1 = new RegistrationRequest();
        request1.setUsername("dupuser");
        request1.setEmail("dup1@test.com");
        request1.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        RegistrationRequest request2 = new RegistrationRequest();
        request2.setUsername("dupuser");
        request2.setEmail("dup2@test.com");
        request2.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("baduser");
        request.setEmail("not-an-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("shortpwd");
        request.setEmail("short@test.com");
        request.setPassword("12345");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returnsJwt() throws Exception {
        RegistrationRequest regRequest = new RegistrationRequest();
        regRequest.setUsername("loginuser");
        regRequest.setEmail("login@test.com");
        regRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.username").value("loginuser"));
    }

    @Test
    void login_invalidCredentials_returnsError() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wronguser");
        loginRequest.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_noToken_returns403() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withToken_returns200() throws Exception {
        RegistrationRequest regRequest = new RegistrationRequest();
        regRequest.setUsername("authuser");
        regRequest.setEmail("auth@test.com");
        regRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("authuser");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("authuser"));
    }
}
