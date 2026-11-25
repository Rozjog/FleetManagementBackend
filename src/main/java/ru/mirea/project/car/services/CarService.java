package ru.mirea.project.car.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.project.car.dto.CarBulkRequest;
import ru.mirea.project.car.dto.CarBulkResponse;
import ru.mirea.project.car.enums.CarBodyType;
import ru.mirea.project.car.enums.CarBulkOperation;
import ru.mirea.project.car.enums.CarStatus;
import ru.mirea.project.car.enums.CarTransmission;
import ru.mirea.project.car.exceptions.DuplicateLicensePlateException;
import ru.mirea.project.car.models.Car;
import ru.mirea.project.car.repositories.CarRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;

    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "brand", "model", "year", "mileage",
            "licensePlate", "status", "bodyType", "transmission"
    );

    public Page<Car> searchCars(
            String search,
            CarStatus status,
            CarBodyType bodyType,
            CarTransmission transmission,
            boolean archived,
            int page,
            int size,
            String sort
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Номер страницы должен быть >= 0, размер - от 1 до 100."
            );
        }

        String[] parts = sort.split(",", -1);

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Формат сортировки: поле,направление."
            );
        }

        String sortBy = parts[0].trim();
        String direction = parts[1].trim();

        if (!SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Недопустимое поле сортировки: " + sortBy
            );
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException(
                    "Направление сортировки: asc или desc."
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(direction), sortBy)
        );

        Specification<Car> spec = (root, query, cb) ->
                archived
                        ? cb.isNotNull(root.get("deletedAt"))
                        : cb.isNull(root.get("deletedAt"));

        if (search != null && !search.isBlank()) {
            String value = "%" + search.trim()
                    .toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_") + "%";

            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("brand")), value, '\\'),
                    cb.like(cb.lower(root.get("model")), value, '\\'),
                    cb.like(cb.lower(root.get("licensePlate")), value, '\\')
            ));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (bodyType != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("bodyType"), bodyType));
        }

        if (transmission != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("transmission"), transmission));
        }

        return carRepository.findAll(spec, pageable);
    }

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
            throw new DuplicateLicensePlateException(
                    "Автомобиль с таким госномером уже зарегистрирован."
            );
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

    @Transactional
    public CarBulkResponse bulkCars(CarBulkRequest request) {

        if (request.operation() == CarBulkOperation.CHANGE_STATUS
                && request.status() == null) {
            throw new IllegalArgumentException(
                    "Для изменения статуса необходимо указать status."
            );
        }

        List<Long> successfulIds = new ArrayList<>();
        List<CarBulkResponse.Failure> errors = new ArrayList<>();

        for (Long id : new LinkedHashSet<>(request.ids())) {

            Optional<Car> optionalCar = carRepository.findById(id);

            if (optionalCar.isEmpty()) {
                errors.add(new CarBulkResponse.Failure(
                        id, "Автомобиль не найден."
                ));
                continue;
            }

            Car car = optionalCar.get();
            String error = null;

            switch (request.operation()) {
                case CHANGE_STATUS -> {
                    if (car.getDeletedAt() != null) {
                        error = "Автомобиль находится в архиве.";
                    } else {
                        car.setStatus(request.status());
                    }
                }

                case ARCHIVE -> {
                    if (car.getDeletedAt() != null) {
                        error = "Автомобиль уже архивирован.";
                    } else {
                        car.archive();
                    }
                }

                case RESTORE -> {
                    if (car.getDeletedAt() == null) {
                        error = "Автомобиль не находится в архиве.";
                    } else {
                        car.restore();
                    }
                }
            }

            if (error == null) {
                successfulIds.add(id);
            } else {
                errors.add(new CarBulkResponse.Failure(id, error));
            }
        }

        return new CarBulkResponse(successfulIds, errors);
    }

    public String exportCarsToCsv(
            String search,
            CarStatus status,
            CarBodyType bodyType,
            CarTransmission transmission,
            boolean archived,
            String sort
    ) {
        List<Car> cars = new ArrayList<>();

        int page = 0;
        Page<Car> result;

        do {
            result = searchCars(
                    search, status, bodyType, transmission,
                    archived, page, 100, sort
            );

            cars.addAll(result.getContent());
            page++;

        } while (result.hasNext());

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
