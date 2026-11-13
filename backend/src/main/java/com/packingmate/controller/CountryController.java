package com.packingmate.controller;

import com.packingmate.service.CountryCatalog;
import com.packingmate.service.CountryCatalog.Country;
import java.util.List;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/countries")
public class CountryController {
    private final CountryCatalog countryCatalog;

    public CountryController(CountryCatalog countryCatalog) {
        this.countryCatalog = Objects.requireNonNull(countryCatalog);
    }

    @GetMapping
    public List<Country> findAll() {
        return countryCatalog.findAll();
    }
}
