package ru.mirea.project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.project.services.CarCsvService;

import java.util.Map;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarCsvController {

    private final CarCsvService carCsvService;

    @PostMapping(
            value = "/import/validate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> validateImport(
            @RequestParam("file") MultipartFile file
    ) {
        return carCsvService.validateCars(file);
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Integer> importCars(
            @RequestParam("file") MultipartFile file
    ) {
        int count = carCsvService.importCars(file);

        return Map.of("imported", count);
    }
}