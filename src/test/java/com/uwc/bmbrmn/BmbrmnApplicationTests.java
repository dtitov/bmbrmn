package com.uwc.bmbrmn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration MVC REST tests
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BmbrmnApplication.class)
@WebAppConfiguration
public class BmbrmnApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Value("${arena.width:13}")
    private int width;

    @Value("${arena.height:11}")
    private int height;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void getArena() throws Exception {
        mockMvc.perform(get("/getArena"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(width)))
                .andExpect(jsonPath("$[0]", hasSize(height)));
    }

    @Test
    public void updateStatus() throws Exception {
        mockMvc.perform(get("/updateStatus"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/event-stream"))
                .andExpect(content().string(containsString("retry: 200")));
    }

    @Test
    public void moveUp() throws Exception {
        mockMvc.perform(get("/moveUp"))
                .andExpect(status().isOk());
    }

    @Test
    public void moveDown() throws Exception {
        mockMvc.perform(get("/moveDown"))
                .andExpect(status().isOk());
    }

    @Test
    public void moveLeft() throws Exception {
        mockMvc.perform(get("/moveLeft"))
                .andExpect(status().isOk());
    }

    @Test
    public void moveRight() throws Exception {
        mockMvc.perform(get("/moveRight"))
                .andExpect(status().isOk());
    }

    @Test
    public void plantBomb() throws Exception {
        mockMvc.perform(get("/plantBomb"))
                .andExpect(status().isOk());
    }

    @Test
    public void newGame() throws Exception {
        mockMvc.perform(get("/newGame"))
                .andExpect(status().isOk());
    }

}
