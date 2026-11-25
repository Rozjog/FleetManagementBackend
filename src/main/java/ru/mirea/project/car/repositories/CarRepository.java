
package ru.mirea.project.car.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.mirea.project.car.models.Car;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car>  {

    Optional<Car> findByLicensePlateIgnoreCase(String licensePlate);

    List<Car> findAllByDeletedAtIsNull();

    Optional<Car> findByIdAndDeletedAtIsNull(Long id);
}
