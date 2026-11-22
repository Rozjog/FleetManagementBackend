
package ru.mirea.project.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.mirea.project.models.Car;
import ru.mirea.project.enums.CarBodyType;
import ru.mirea.project.enums.CarStatus;
import ru.mirea.project.enums.CarTransmission;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car>  {

    Optional<Car> findByLicensePlateIgnoreCase(String licensePlate);

    List<Car> findAllByDeletedAtIsNull();

    Optional<Car> findByIdAndDeletedAtIsNull(Long id);
}
