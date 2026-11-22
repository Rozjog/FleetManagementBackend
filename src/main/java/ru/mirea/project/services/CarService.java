package ru.mirea.project.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.project.enums.CarBodyType;
import ru.mirea.project.enums.CarStatus;
import ru.mirea.project.enums.CarTransmission;
import ru.mirea.project.exceptions.DuplicateLicensePlateException;
import ru.mirea.project.models.Car;
import ru.mirea.project.repositories.CarRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;

    @Transactional
    public Car createCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException(
                    "Данные автомобиля отсутствуют."
            );
        }

        if (car.getId() != null) {
            throw new IllegalArgumentException(
                    "При создании ID указывать не нужно."
            );
        }

        if (carRepository.findByLicensePlateIgnoreCase(car.getLicensePlate()).isPresent()) {
            throw new IllegalArgumentException("Автомобиль с таким номером уже зарегистрирован.");
        }

        return carRepository.save(car);
    }

    public List<Car> findAll() {
        return carRepository.findAllByDeletedAtIsNull();
    }

    public List<Car> findAllIncludingArchived() {
        return carRepository.findAll();
    }

    public Car getCarById(Long id) {
        return carRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Автомобиль с ID " + id + " не найден."
                ));
    }

    public Optional<Car> findByLicensePlate(String licensePlate) {
        return carRepository.findByLicensePlateIgnoreCaseAndDeletedAtIsNull(licensePlate);
    }

    public List<Car> findByBrand(String brand) {
        return carRepository.findByBrandContainingIgnoreCaseAndDeletedAtIsNull(brand);
    }

    public List<Car> findByModel(String model) {
        return carRepository.findByModelContainingIgnoreCaseAndDeletedAtIsNull(model);
    }

    public List<Car> filterByStatus(CarStatus status) {
        return carRepository.findByStatusAndDeletedAtIsNull(status);
    }

    public List<Car> filterByBodyType(CarBodyType bodyType) {
        return carRepository.findByBodyTypeAndDeletedAtIsNull(bodyType);
    }

    public List<Car> filterByStatusAndBodyType(
            CarStatus status,
            CarBodyType bodyType
    ) {
        return carRepository.findByStatusAndBodyTypeAndDeletedAtIsNull(
                status, bodyType
        );
    }

    public List<Car> filterByTransmission(CarTransmission transmission) {
        return carRepository.findByTransmissionAndDeletedAtIsNull(transmission);
    }

    public List<Car> sortByYear() {
        return carRepository.findAllByDeletedAtIsNull(
                Sort.by("year")
        );
    }

    public List<Car> sortByMileage() {
        return carRepository.findAllByDeletedAtIsNull(
                Sort.by("mileage")
        );
    }


    @Transactional
    public Car updateCar(Long id, Car data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Данные автомобиля отсутствуют."
            );
        }

        Car currentCar = getCarById(id);

        if(data.getMileage() < currentCar.getMileage()) {
            throw new IllegalArgumentException(
                    "Нельзя уменьшить пробег."
            );
        }

        carRepository.findByLicensePlateIgnoreCase(data.getLicensePlate())
                .filter(car -> !car.getId().equals(id))
                .ifPresent(car -> {
                    throw new DuplicateLicensePlateException(
                            "Госномер " + data.getLicensePlate()
                                    + " уже принадлежит автомобилю с ID "
                                    + car.getId() + "."
                    );
                });

        currentCar.setBrand(data.getBrand());
        currentCar.setModel(data.getModel());
        currentCar.setTransmission(data.getTransmission());
        currentCar.setYear(data.getYear());
        currentCar.setLicensePlate(data.getLicensePlate());
        currentCar.setBodyType(data.getBodyType());
        currentCar.setStatus(data.getStatus());
        currentCar.setMileage(data.getMileage());

        return carRepository.save(currentCar);
    }

    @Transactional
    public void deleteCarById(Long id) {
        Car car = getCarById(id);
        car.archive();
    }

    @Transactional
    public void restoreCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Автомобиль с ID " + id + " не найден."
                ));

        if (car.getDeletedAt() == null) {
            throw new IllegalArgumentException(
                    "Автомобиль с ID " + id + " не находится в архиве."
            );
        }

        car.restore();
    }

    public String exportCarsToCsv() {
        List<Car> cars = findAll();

        StringBuilder csv = new StringBuilder();

        csv.append("ID,Марка,Модель,КПП,Год,Госномер,Кузов,Статус,Пробег\n");

        for (Car car : cars) {
            csv.append(car.getId()).append(",")
                    .append(csvValue(car.getBrand())).append(",")
                    .append(csvValue(car.getModel())).append(",")
                    .append(car.getTransmission().name()).append(",")
                    .append(car.getYear()).append(",")
                    .append(csvValue(car.getLicensePlate())).append(",")
                    .append(car.getBodyType().name()).append(",")
                    .append(car.getStatus().name()).append(",")
                    .append(car.getMileage()).append("\n");
        }

        return csv.toString();
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public record CarStatistics(
            int total,
            long available,
            long maintenance
    ) {}

    public CarStatistics getStatistics() {
        List<Car> cars = findAll();

        long available = cars.stream()
                .filter(car -> car.getStatus() == CarStatus.AVAILABLE)
                .count();

        long maintenance = cars.stream()
                .filter(car -> car.getStatus() == CarStatus.MAINTENANCE)
                .count();

        return new CarStatistics(
                cars.size(),
                available,
                maintenance
        );
    }
}
