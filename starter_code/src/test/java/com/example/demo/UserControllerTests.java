package com.example.demo;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JacksonTester<CreateUserRequest> jsonRequest;

    private static final String DEFAULT_USERNAME = "simpsonh";
    private static final String DEFAULT_PASSWORD = "password1234";
    private User defaultUser;
    private CreateUserRequest defaultCreateUserRequest;

    @Before
    public void before() {
        defaultUser = new User();
        defaultCreateUserRequest = new CreateUserRequest();

        defaultUser.setUsername(DEFAULT_USERNAME);
        defaultUser.setPassword(DEFAULT_PASSWORD);
        defaultUser.setId(1L);
        defaultUser.setCart(null);

        defaultCreateUserRequest.setUsername(defaultUser.getUsername());
        defaultCreateUserRequest.setPassword(defaultUser.getPassword());
        defaultCreateUserRequest.setConfirmPassword(defaultUser.getPassword());
    }

    @Test
    public void findById() throws Exception {
        given(userRepository.findById(1L)).willReturn(Optional.of(defaultUser));

        mockMvc.perform(get("/api/user/id/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultUser.getId()))
                .andExpect(jsonPath("$.username").value(defaultUser.getUsername()));
    }

    @Test
    public void findByUsername() throws Exception {
        given(userRepository.findByUsername(defaultUser.getUsername())).willReturn(defaultUser);

        mockMvc.perform(get("/api/user/{username}", defaultUser.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultUser.getId()))
                .andExpect(jsonPath("$.username").value(defaultUser.getUsername()));
    }

    @Test
    public void createUser_happyPath() throws Exception {
        given(bCryptPasswordEncoder.encode(defaultUser.getPassword())).willReturn("encoded-password");

        mockMvc.perform(post("/api/user/create")
                    .content(jsonRequest.write(defaultCreateUserRequest).getJson())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.username").value(defaultUser.getUsername()));

        verify(cartRepository, times(1)).save(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void createUser_passwordInsufficient() throws Exception {
        defaultCreateUserRequest.setPassword("1234");
        defaultCreateUserRequest.setConfirmPassword("1234");

        mockMvc.perform(post("/api/user/create")
                        .content(jsonRequest.write(defaultCreateUserRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());

        verify(bCryptPasswordEncoder, never()).encode(any());
        verify(cartRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void createUser_passwordsDoNotMatch() throws Exception {
        defaultCreateUserRequest.setConfirmPassword(defaultCreateUserRequest.getPassword() + "1");

        mockMvc.perform(post("/api/user/create")
                        .content(jsonRequest.write(defaultCreateUserRequest).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());

        verify(bCryptPasswordEncoder, never()).encode(any());
        verify(cartRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}
