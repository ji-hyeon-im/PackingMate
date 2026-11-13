package com.packingmate.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.packingmate.service.CountryCatalog;
import com.packingmate.service.CountryCatalog.Country;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CountryController.class)
class CountryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CountryCatalog countryCatalog;

    @Test
    void returnsSupportedCountriesWithRepresentativeCities() throws Exception {
        when(countryCatalog.findAll()).thenReturn(List.of(
                new Country("JP", "일본", "도쿄"),
                new Country("VN", "베트남", "하노이"),
                new Country("US", "미국", "워싱턴 D.C."),
                new Country("TH", "태국", "방콕"),
                new Country("TW", "대만", "타이베이")));

        mockMvc.perform(get("/api/v1/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(5)))
                .andExpect(jsonPath("$[0].countryCode").value("JP"))
                .andExpect(jsonPath("$[0].name").value("일본"))
                .andExpect(jsonPath("$[0].representativeCity").value("도쿄"));
    }
}
