package ru.mirea.project.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mirea.project.dto.CarBulkRequest;
import ru.mirea.project.dto.CarBulkResponse;
import ru.mirea.project.dto.CarRequest;
import ru.mirea.project.dto.CarResponse;
import ru.mirea.project.enums.CarBodyType;
import ru.mirea.project.enums.CarStatus;
import ru.mirea.project.enums.CarTransmission;
import ru.mirea.project.models.Car;
import ru.mirea.project.services.CarService;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public Page<CarResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CarStatus status,
            @RequestParam(required = false) CarBodyType bodyType,
            @RequestParam(required = false) CarTransmission transmission,
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "brand,asc") String sort
    ) {
        return carService.searchCars(
                search, status, bodyType, transmission,
                archived, page, size, sort
        ).map(this::toResponse);
    }

    @GetMapping("/all")
    public List<CarResponse> findAllIncludingArchived() {
        return carService.findAllIncludingArchived()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CarResponse findById(@PathVariable Long id) {
        Car car = carService.getCarById(id);

        return toResponse(car);
    }

    @PostMapping
    public ResponseEntity<CarResponse> createCar(
            @Valid
            @RequestBody CarRequest request
    ) {
        Car car = toEntity(request);

        Car createdCar = carService.createCar(car);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(createdCar));
    }

    @PutMapping("/{id}")
    public CarResponse updateCar(
            @PathVariable Long id,
            @Valid
            @RequestBody CarRequest request
    )  {
        Car car = toEntity(request);

        Car updatedCar = carService.updateCar(id, car);

        return toResponse(updatedCar);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable Long id) {
        carService.deleteCarById(id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreCar(@PathVariable Long id) {
        carService.restoreCarById(id);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCars(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CarStatus status,
            @RequestParam(required = false) CarBodyType bodyType,
            @RequestParam(required = false) CarTransmission transmission,
            @RequestParam(defaultValue = "false") boolean archived,
            @RequestParam(defaultValue = "brand,asc") String sort
    ) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cars.csv\""
                )
                .contentType(
                        MediaType.parseMediaType("text/csv; charset=UTF-8")
                )
                .body(carService.exportCarsToCsv(
                        search, status, bodyType,
                        transmission, archived, sort
                ));
    }

    @GetMapping("/statistics")
    public CarService.CarStatistics getStatistics() {
        return carService.getStatistics();
    }

    private Car toEntity(CarRequest request) {
        return new Car(
                null,
                request.brand(),
                request.model(),
                request.transmission(),
                request.year(),
                request.licensePlate(),
                request.bodyType(),
                request.status(),
                request.mileage()
        );
    }

    @PostMapping("/bulk")
    public CarBulkResponse bulkCars(
            @Valid @RequestBody CarBulkRequest request
    ) {
        return carService.bulkCars(request);
    }

    private CarResponse toResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getTransmission(),
                car.getYear(),
                car.getLicensePlate(),
                car.getBodyType(),
                car.getStatus(),
                car.getMileage(),
                car.getDeletedAt()
        );
    }
}
