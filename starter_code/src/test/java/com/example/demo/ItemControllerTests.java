package com.example.demo;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
public class ItemControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRepository itemRepository;

    private List<Item> defaultItems;

    @Before
    public void before() {
        Item defaultItem0 = new Item();
        Item defaultItem1 = new Item();
        defaultItems = new ArrayList<>();

        defaultItem0.setName("item one");
        defaultItem0.setDescription("item one description");
        defaultItem0.setId(1L);
        defaultItem0.setPrice(BigDecimal.valueOf(17.99));

        defaultItem1.setName("item two");
        defaultItem1.setDescription("item two description");
        defaultItem1.setId(2L);
        defaultItem1.setPrice(BigDecimal.valueOf(0.89));

        defaultItems.add(defaultItem0);
        defaultItems.add(defaultItem1);
    }

    @Test
    public void getItems() throws Exception {
        given(itemRepository.findAll()).willReturn(defaultItems);

        mockMvc.perform(get("/api/item/")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.[0].id").value(defaultItems.get(0).getId()))
                .andExpect(jsonPath("$.[0].name").value(defaultItems.get(0).getName()))
                .andExpect(jsonPath("$.[0].description").value(defaultItems.get(0).getDescription()))
                .andExpect(jsonPath("$.[0].price").value(defaultItems.get(0).getPrice()))
                .andExpect(jsonPath("$.[1].id").value(defaultItems.get(1).getId()))
                .andExpect(jsonPath("$.[1].name").value(defaultItems.get(1).getName()))
                .andExpect(jsonPath("$.[1].description").value(defaultItems.get(1).getDescription()))
                .andExpect(jsonPath("$.[1].price").value(defaultItems.get(1).getPrice()))
                .andExpect(status().isOk());
    }

    @Test
    public void getItemById() throws Exception {
        given(itemRepository.findById(1L)).willReturn(Optional.of(defaultItems.get(0)));

        mockMvc.perform(get("/api/item/{id}", "1")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id").value(defaultItems.get(0).getId()))
                .andExpect(jsonPath("$.name").value(defaultItems.get(0).getName()))
                .andExpect(jsonPath("$.description").value(defaultItems.get(0).getDescription()))
                .andExpect(jsonPath("$.price").value(defaultItems.get(0).getPrice()))
                .andExpect(status().isOk());
    }

    @Test
    public void getItemsByName() throws Exception {
        List<Item> items = new ArrayList<>();
        items.add(defaultItems.get(1));
        given(itemRepository.findByName(items.get(0).getName())).willReturn(items);

        mockMvc.perform(get("/api/item/name/{name}", items.get(0).getName())
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$[0].id").value(items.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(items.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(items.get(0).getDescription()))
                .andExpect(jsonPath("$[0].price").value(items.get(0).getPrice()))
                .andExpect(status().isOk());
    }
}
