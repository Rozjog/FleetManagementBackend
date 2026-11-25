package ru.mirea.project.request.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import ru.mirea.project.request.dto.RentalRequestCreateRequestDTO;
import ru.mirea.project.request.dto.RentalRequestDTO;
import ru.mirea.project.request.dto.RentalRequestUpdateRequestDTO;
import ru.mirea.project.request.enums.Priority;
import ru.mirea.project.request.enums.RequestStatus;
import ru.mirea.project.request.services.RentalRequestService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
public class RentalRequestController {

    private final RentalRequestService rentalRequestService;

    public RentalRequestController(RentalRequestService rentalRequestService) {
        this.rentalRequestService = rentalRequestService;
    }

    @GetMapping
    public Page<RentalRequestDTO> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RequestStatus status) {
        return rentalRequestService.findAll(page, size, status);
    }

    @GetMapping("/search")
    public Page<RentalRequestDTO> search(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return rentalRequestService.search(status, priority, from, to, page, size);
    }

    @GetMapping("/{id}")
    public RentalRequestDTO getById(@PathVariable UUID id) {
        return rentalRequestService.findById(id);
    }

    @PostMapping
    public RentalRequestDTO create(
            @Valid @RequestBody RentalRequestCreateRequestDTO dto) {
        return rentalRequestService.create(dto);
    }

    @PutMapping("/{id}")
    public RentalRequestDTO update(
            @PathVariable UUID id,
            @Valid @RequestBody RentalRequestUpdateRequestDTO dto) {
        return rentalRequestService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable UUID id) {
        rentalRequestService.archive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable UUID id) {
        rentalRequestService.restore(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> bulk(@RequestBody Map<String, Object> body) {
        List<UUID> ids = ((List<String>) body.get("ids")).stream()
                .map(UUID::fromString)
                .toList();
        String action = (String) body.get("action");
        rentalRequestService.bulkAction(ids, action);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics")
    public Map<RequestStatus, Long> analytics() {
        return rentalRequestService.getStatistics();
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Integer>> importCsv(@RequestParam("file") MultipartFile file) {
        int created = rentalRequestService.importFromCsv(file);
        return ResponseEntity.ok(Map.of("created", created));
    }
}