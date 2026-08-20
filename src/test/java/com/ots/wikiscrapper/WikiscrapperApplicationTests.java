package com.ots.wikiscrapper;

import jakarta.servlet.http.Cookie;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WikiscrapperApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private static Cookie enCookie() {
        return new Cookie("wiki_lang", "en");
    }

    private static Cookie plCookie() {
        return new Cookie("wiki_lang", "pl");
    }

    @Test
    void dashboardRendersInEnglish() throws Exception {
        mockMvc.perform(get("/").cookie(enCookie()).locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Synchronize with Wikipedia")));
    }

    @Test
    void dashboardRendersInPolish() throws Exception {
        mockMvc.perform(get("/").cookie(plCookie()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Synchronizuj z Wikipedią")));
    }

    @Test
    void voivodeshipsApiReturnsSeededRows() throws Exception {
        mockMvc.perform(get("/api/voivodeships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(16));
    }

    @Test
    void voivodeshipsPageRendersInEnglish() throws Exception {
        mockMvc.perform(get("/voivodeships").cookie(enCookie()).locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Polish Voivodeships")));
    }

    @Test
    void voivodeshipsPageRendersInPolish() throws Exception {
        mockMvc.perform(get("/voivodeships").cookie(plCookie()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Polskie województwa")));
    }

    @Test
    void logsPageRendersInEnglish() throws Exception {
        mockMvc.perform(get("/logs").cookie(enCookie()).locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Application Logs")));
    }

    @Test
    void syncStatusApiReturnsJson() throws Exception {
        mockMvc.perform(get("/api/sync/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRunning").value(false));
    }
}
