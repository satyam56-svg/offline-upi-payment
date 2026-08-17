package com.satyam.offline_upi_payment.controller;

import com.satyam.offline_upi_payment.dto.LoginRequest;
import com.satyam.offline_upi_payment.dto.LoginResponse;
import com.satyam.offline_upi_payment.dto.UserRegistrationRequest;
import com.satyam.offline_upi_payment.entity.User;
import com.satyam.offline_upi_payment.security.JwtUtil;
import com.satyam.offline_upi_payment.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void shouldRegisterUser() throws Exception {

        User user = new User();
        user.setId(1L);
        user.setFullName("Rahul");
        user.setUpiId("rahul@test");

        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(user);

        String requestJson = """
                {
                    "fullName": "Rahul",
                    "upiId": "rahul@test",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void shouldLoginUser() throws Exception {

        LoginResponse response =
                new LoginResponse(
                        "test-jwt-token",
                        "rahul@test",
                        "Rahul"
                );

        when(userService.loginUser(any(LoginRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "upiId": "rahul@test",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}