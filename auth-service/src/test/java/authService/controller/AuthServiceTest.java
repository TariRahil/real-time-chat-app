package authService.controller;

import authService.dto.LoginRequest;
import authService.model.User;
import authService.repository.UserRepository;
import authService.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import authService.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class AuthServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // Stub the encode() method to return a predefined value
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
    }

    @Test
    public void testRegister_Success() throws Exception {
        // Mock repository behavior
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Create a user object for registration
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        // Perform a POST request to /auth/register
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("User Registered successfully."));

        // Verify that userRepository.save() was called
        Mockito.verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegister_UsernameAlreadyExists() throws Exception {
        // Mock repository behavior
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));

        // Create a user object for registration
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        // Perform a POST request to /auth/register
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Username already exists."));
    }

    @Test
    public void testRegister_EmailAlreadyExists() throws Exception {
        // Mock repository behavior
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        // Create a user object for registration
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");

        // Perform a POST request to /auth/register
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Email already exists."));
    }

    @Test
    public void testLogin_Success() throws Exception {
        // Mock repository behavior
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // Mock password encoder behavior
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // Mock JWT token generation
        when(jwtUtil.generateToken("testuser")).thenReturn("mock-jwt-token");

        // Create a login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Perform a POST request to /auth/login
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    public void testLogin_UserNotFound() throws Exception {
        // Mock repository behavior
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Create a login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Perform a POST request to /auth/login
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testLogin_InvalidCredentials() throws Exception {
        // Mock repository behavior
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // Mock password encoder behavior
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Create a login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // Perform a POST request to /auth/login
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().string("Invalid credentials."));
    }
}