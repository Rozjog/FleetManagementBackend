package ru.mirea.project.car.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import ru.mirea.project.car.enums.CarBodyType;
import ru.mirea.project.car.enums.CarStatus;
import ru.mirea.project.car.enums.CarTransmission;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="cars")
@Getter
@NoArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="car_id")
    private Long id;

    @Column(name="brand",  nullable = false, length = 30)
    private String brand;

    @Column(name="model",  nullable = false, length = 30)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false, length = 30)
    private CarTransmission transmission;

    @Column(name="year",  nullable = false)
    private int year;

    @Column(name="license_plate", unique = true, nullable = false, length = 30)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name="body_type",  nullable = false, length = 30)
    private CarBodyType bodyType;

    @Enumerated(EnumType.STRING)
    @Column(name="status",  nullable = false, length = 30)
    private CarStatus status;

    @Column(name="mileage",  nullable = false)
    private int mileage;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;


    public Car(
            Long id,
            String brand,
            String model,
            CarTransmission transmission,
            int year,
            String licensePlate,
            CarBodyType bodyType,
            CarStatus status,
            int mileage
    ) {
        this.id = id;
        setBrand(brand);
        setModel(model);
        setTransmission(transmission);
        setYear(year);
        setLicensePlate(licensePlate);
        setBodyType(bodyType);
        setStatus(status);
        setMileage(mileage);
    }

    public void setBrand(String brand){
        if (brand == null || brand.isBlank()){
            throw new IllegalArgumentException("Марка автомобиля не может быть пустой!");
        }
        this.brand = brand.trim();
    }

    public void setModel(String model){
        if (model == null || model.isBlank()){
            throw new IllegalArgumentException("Модель автомобиля не может быть пустой!");
        }
        this.model = model.trim();
    }

    public void setTransmission(CarTransmission transmission){
        if (transmission == null){
            throw new IllegalArgumentException("Тип коробки не может быть пустым!");
        }
        this.transmission = transmission;
    }

    public void setYear(int year){
        int currentYear = LocalDate.now().getYear();
        if (year > currentYear || year < 1900){
            throw new IllegalArgumentException("Год введен неверно!");
        }

        this.year = year;
    }

    public void setLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("Номер автомобиля не может быть пустой!");
        }
        this.licensePlate = licensePlate.trim();
    }

    public void setBodyType(CarBodyType bodyType){
        if (bodyType == null){
            throw new IllegalArgumentException("Тип кузова не может быть пустым!");
        }
        this.bodyType = bodyType;
    }

    public void setStatus(CarStatus status) {
        if (status == null){
            throw new IllegalArgumentException("Статус не может быть пустым!");
        }
        this.status = status;
    }

    public void setMileage(int mileage){
        if (mileage < 0){
            throw new IllegalArgumentException("Пробег не может быть отрицательным!");
        }
        this.mileage = mileage;
    }

    public void archive() {
        if (deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    public void restore() {
        if (deletedAt != null) {
            deletedAt = null;
        }
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", transmission=" + transmission +
                ", year=" + year +
                ", licensePlate='" + licensePlate + '\'' +
                ", bodyType=" + bodyType +
                ", status=" + status +
                ", mileage=" + mileage +
                '}';
    }

}
