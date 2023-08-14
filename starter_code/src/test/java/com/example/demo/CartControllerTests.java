package com.example.demo;


import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
public class CartControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private ItemRepository itemRepository;

    @Autowired
    private JacksonTester<ModifyCartRequest> jsonRequest;

    private static final String DEFAULT_USERNAME = "simpsonh";
    private static final String DEFAULT_PASSWORD = "password1234";
    private User defaultUser;
    private Cart defaultEmptyCart;
    private Cart defaultFullCart;
    private Item defaultItem;

    @Before
    public void before() {
        defaultUser = new User();
        defaultEmptyCart = new Cart();
        defaultFullCart = new Cart();
        defaultItem = new Item();

        defaultUser.setUsername(DEFAULT_USERNAME);
        defaultUser.setPassword(DEFAULT_PASSWORD);
        defaultUser.setId(1L);
        defaultUser.setCart(defaultEmptyCart);

        defaultItem.setId(1L);
        defaultItem.setDescription("item description");
        defaultItem.setName("item 1");
        defaultItem.setPrice(BigDecimal.valueOf(1.49));

        defaultEmptyCart.setUser(defaultUser);
        defaultEmptyCart.setId(1L);

        defaultFullCart.setUser(defaultUser);
        defaultFullCart.setId(1L);
        defaultFullCart.addItem(defaultItem);
        defaultFullCart.setTotal(defaultItem.getPrice());
    }

    @Test
    public void addToCart_happyPath() throws Exception {
        ModifyCartRequest request = getDefaultModifyCartRequest();

        given(userRepository.findByUsername(DEFAULT_USERNAME)).willReturn(defaultUser);
        given(itemRepository.findById(request.getItemId())).willReturn(Optional.of(defaultItem));

        mockMvc.perform(post("/api/cart/addToCart")
                    .content(jsonRequest.write(request).getJson())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultEmptyCart.getId()))
                .andExpect(jsonPath("$.items[0].id").value(defaultItem.getId()))
                .andExpect(jsonPath("$.items[0].name").value(defaultItem.getName()))
                .andExpect(jsonPath("$.items[0].price").value(defaultItem.getPrice()))
                .andExpect(jsonPath("$.items[0].description").value(defaultItem.getDescription()))
                .andExpect(jsonPath("$.user.id").value(defaultUser.getId()))
                .andExpect(jsonPath("$.user.username").value(defaultUser.getUsername()))
                .andExpect(jsonPath("$.total").value(defaultItem.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()))));

        verify(cartRepository, times(1)).save(any());
    }

    @Test
    public void addToCart_userNotFound() throws Exception {
        ModifyCartRequest request = getDefaultModifyCartRequest();

        given(userRepository.findByUsername(DEFAULT_USERNAME)).willReturn(null);

        mockMvc.perform(post("/api/cart/addToCart")
                .content(jsonRequest.write(request).getJson())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addToCart_itemNotFound() throws Exception {
        ModifyCartRequest request = getDefaultModifyCartRequest();

        given(userRepository.findByUsername(DEFAULT_USERNAME)).willReturn(defaultUser);
        given(itemRepository.findById(request.getItemId())).willReturn(Optional.empty());

        mockMvc.perform(post("/api/cart/addToCart")
                        .content(jsonRequest.write(request).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeFromCart_happyPath() throws Exception {
        ModifyCartRequest request = getDefaultModifyCartRequest();

        defaultUser.setCart(defaultFullCart);
        given(userRepository.findByUsername(DEFAULT_USERNAME)).willReturn(defaultUser);
        given(itemRepository.findById(request.getItemId())).willReturn(Optional.of(defaultItem));

        mockMvc.perform(post("/api/cart/removeFromCart")
                        .content(jsonRequest.write(request).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(defaultEmptyCart.getId()))
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.user.id").value(defaultUser.getId()))
                .andExpect(jsonPath("$.user.username").value(defaultUser.getUsername()))
                .andExpect(jsonPath("$.total").value(BigDecimal.valueOf(0.0)));

        verify(cartRepository, times(1)).save(any());
    }

    @Test
    public void removeFromCart_userNotFound() throws Exception {
        ModifyCartRequest request = getDefaultModifyCartRequest();

        defaultUser.setCart(defaultFullCart);
        given(userRepository.findByUsername(DEFAULT_USERNAME)).willReturn(null);

        mockMvc.perform(post("/api/cart/removeFromCart")
                        .content(jsonRequest.write(request).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeFromCart_itemNotFound() throws Exception {
        ModifyCartRequest request = getDefaultModifyCartRequest();

        defaultUser.setCart(defaultFullCart);
        given(userRepository.findByUsername(DEFAULT_USERNAME)).willReturn(defaultUser);
        given(itemRepository.findById(request.getItemId())).willReturn(Optional.empty());

        mockMvc.perform(post("/api/cart/removeFromCart")
                        .content(jsonRequest.write(request).getJson())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }

    private ModifyCartRequest getDefaultModifyCartRequest() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setItemId(1L);
        request.setQuantity(1);
        request.setUsername(DEFAULT_USERNAME);
        return request;
    }
}
