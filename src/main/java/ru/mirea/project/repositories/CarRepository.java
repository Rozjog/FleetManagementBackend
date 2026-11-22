
package ru.mirea.project.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.mirea.project.models.Car;
import ru.mirea.project.enums.CarBodyType;
import ru.mirea.project.enums.CarStatus;
import ru.mirea.project.enums.CarTransmission;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByLicensePlateIgnoreCase(String licensePlate);

    Optional<Car> findByLicensePlateIgnoreCaseAndDeletedAtIsNull(
            String licensePlate
    );

    List<Car> findByBrandContainingIgnoreCaseAndDeletedAtIsNull(
            String brand
    );

    List<Car> findByModelContainingIgnoreCaseAndDeletedAtIsNull(
            String model
    );

    List<Car> findByStatusAndDeletedAtIsNull(CarStatus status);

    List<Car> findByBodyTypeAndDeletedAtIsNull(CarBodyType bodyType);

    List<Car> findByStatusAndBodyTypeAndDeletedAtIsNull(
            CarStatus status,
            CarBodyType bodyType
    );

    List<Car> findByTransmissionAndDeletedAtIsNull(
            CarTransmission transmission
    );

    List<Car> findAllByDeletedAtIsNull();

    List<Car> findAllByDeletedAtIsNull(Sort sort);

    Optional<Car> findByIdAndDeletedAtIsNull(Long id);
}
