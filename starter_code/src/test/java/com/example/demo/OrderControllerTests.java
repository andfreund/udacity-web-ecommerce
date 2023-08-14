package com.example.demo;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
public class OrderControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    private static final String DEFAULT_USERNAME = "simpsonh";
    private static final String DEFAULT_PASSWORD = "password1234";
    private User defaultUser;
    private Cart defaultFullCart;
    private Item defaultItem;
    private List<UserOrder> defaultUserOrders;

    @Before
    public void before() {
        defaultUser = new User();
        defaultFullCart = new Cart();
        defaultItem = new Item();
        defaultUserOrders = new ArrayList<>();

        defaultUser.setUsername(DEFAULT_USERNAME);
        defaultUser.setPassword(DEFAULT_PASSWORD);
        defaultUser.setId(1L);
        defaultUser.setCart(defaultFullCart);

        defaultItem.setId(1L);
        defaultItem.setDescription("item description");
        defaultItem.setName("item 1");
        defaultItem.setPrice(BigDecimal.valueOf(1.49));

        defaultFullCart.setUser(defaultUser);
        defaultFullCart.setId(1L);
        defaultFullCart.addItem(defaultItem);
        defaultFullCart.addItem(defaultItem);
        defaultFullCart.setTotal(defaultItem.getPrice().multiply(BigDecimal.valueOf(2)));

        UserOrder order = new UserOrder();
        order.setItems(defaultFullCart.getItems());
        order.setUser(defaultFullCart.getUser());
        order.setTotal(defaultFullCart.getTotal());
        order.setId(1L);
        defaultUserOrders.add(order);
    }

    @Test
    public void submit_happyPath() throws Exception {
        given(userRepository.findByUsername(defaultUser.getUsername())).willReturn(defaultUser);

        mockMvc.perform(post("/api/order/submit/{username}", defaultUser.getUsername())
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.items[0].id").value(defaultItem.getId()))
                .andExpect(jsonPath("$.items[0].name").value(defaultItem.getName()))
                .andExpect(jsonPath("$.items[0].price").value(defaultItem.getPrice()))
                .andExpect(jsonPath("$.items[0].description").value(defaultItem.getDescription()))
                .andExpect(jsonPath("$.items[1].id").value(defaultItem.getId()))
                .andExpect(jsonPath("$.items[1].name").value(defaultItem.getName()))
                .andExpect(jsonPath("$.items[1].price").value(defaultItem.getPrice()))
                .andExpect(jsonPath("$.items[1].description").value(defaultItem.getDescription()))
                .andExpect(jsonPath("$.user.id").value(defaultUser.getId()))
                .andExpect(jsonPath("$.user.username").value(defaultUser.getUsername()))
                .andExpect(jsonPath("$.total").value(defaultItem.getPrice().multiply(BigDecimal.valueOf(2))))
                .andExpect(status().isOk());

        verify(orderRepository, times(1)).save(any());
    }

    @Test
    public void submit_userNotFound() throws Exception {
        given(userRepository.findByUsername(defaultUser.getUsername())).willReturn(null);

        mockMvc.perform(post("/api/order/submit/{username}", defaultUser.getUsername())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

        verify(orderRepository, times(0)).save(any());
    }

    @Test
    public void history_isEmpty() throws Exception {
        given(userRepository.findByUsername(defaultUser.getUsername())).willReturn(defaultUser);
        given(orderRepository.findByUser(any())).willReturn(new ArrayList<>());

        mockMvc.perform(get("/api/order/history/{username}", defaultUser.getUsername())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").isEmpty())
                .andExpect(status().isOk());
    }

    @Test
    public void history_happyPath() throws Exception {
        given(userRepository.findByUsername(defaultUser.getUsername())).willReturn(defaultUser);
        given(orderRepository.findByUser(any())).willReturn(defaultUserOrders);

        mockMvc.perform(get("/api/order/history/{username}", defaultUser.getUsername())
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.[0].items[0].id").value(defaultItem.getId()))
                .andExpect(jsonPath("$.[0].items[0].name").value(defaultItem.getName()))
                .andExpect(jsonPath("$.[0].items[0].price").value(defaultItem.getPrice()))
                .andExpect(jsonPath("$.[0].items[0].description").value(defaultItem.getDescription()))
                .andExpect(jsonPath("$.[0].items[1].id").value(defaultItem.getId()))
                .andExpect(jsonPath("$.[0].items[1].name").value(defaultItem.getName()))
                .andExpect(jsonPath("$.[0].items[1].price").value(defaultItem.getPrice()))
                .andExpect(jsonPath("$.[0].items[1].description").value(defaultItem.getDescription()))
                .andExpect(jsonPath("$.[0].user.id").value(defaultUser.getId()))
                .andExpect(jsonPath("$.[0].user.username").value(defaultUser.getUsername()))
                .andExpect(jsonPath("$.[0].total").value(defaultItem.getPrice().multiply(BigDecimal.valueOf(2))))
                .andExpect(status().isOk());
    }

    @Test
    public void history_userNotFound() throws Exception {
        given(userRepository.findByUsername(defaultUser.getUsername())).willReturn(null);

        mockMvc.perform(get("/api/order/history/{username}", defaultUser.getUsername())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());
    }
}
