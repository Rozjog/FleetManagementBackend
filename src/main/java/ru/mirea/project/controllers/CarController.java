package ru.mirea.project.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mirea.project.dto.CarRequest;
import ru.mirea.project.dto.CarResponse;
import ru.mirea.project.enums.CarBodyType;
import ru.mirea.project.enums.CarStatus;
import ru.mirea.project.models.Car;
import ru.mirea.project.services.CarService;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public List<CarResponse> findAll(
            @RequestParam(name="status", required = false) CarStatus status,
            @RequestParam(name="bodyType", required = false) CarBodyType bodyType
    ) {
        List<Car> cars;

        if (status == null && bodyType == null) {
            cars = carService.findAll();
        } else if (status == null) {
            cars = carService.filterByBodyType(bodyType);
        } else if (bodyType == null) {
            cars = carService.filterByStatus(status);
        } else {
            cars = carService.filterByStatusAndBodyType(status, bodyType);
        }

        return cars.stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/sort")
    public List<CarResponse> sortCars(
            @RequestParam(name="by", required = true) String by
    ) {
        List<Car> cars;

        switch (by) {
            case "year":
                cars = carService.sortByYear();
                break;

            case "mileage":
                cars = carService.sortByMileage();
                break;

            default:
                throw new IllegalArgumentException(
                        "Недопустимое поле сортировки: " + by
                );
        }

        return cars.stream()
                .map(this::toResponse)
                .toList();
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

    @GetMapping("/search")
    public List<CarResponse> searchCars(
            @RequestParam(name = "by") String by,
            @RequestParam(name = "value") String value
    ) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "Значение поиска не может быть пустым."
            );
        }

        List<Car> cars;

        switch (by) {
            case "brand":
                cars = carService.findByBrand(value);
                break;

            case "model":
                cars = carService.findByModel(value);
                break;

            case "licensePlate":
                cars = carService.findByLicensePlate(value)
                        .map(List::of)
                        .orElseGet(List::of);
                break;

            default:
                throw new IllegalArgumentException(
                        "Недопустимое поле поиска: " + by
                );
        }

        return cars.stream()
                .map(this::toResponse)
                .toList();
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
    public ResponseEntity<String> exportCars() {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cars.csv\""
                )
                .contentType(
                        MediaType.parseMediaType("text/csv; charset=UTF-8")
                )
                .body(carService.exportCarsToCsv());
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
