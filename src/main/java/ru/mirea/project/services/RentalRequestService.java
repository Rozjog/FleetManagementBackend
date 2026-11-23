package ru.mirea.project.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.project.dto.RentalRequestCreateRequestDTO;
import ru.mirea.project.dto.RentalRequestDTO;
import ru.mirea.project.dto.RentalRequestUpdateRequestDTO;
import ru.mirea.project.enums.Priority;
import ru.mirea.project.enums.RequestStatus;
import ru.mirea.project.models.RentalRequest;
import ru.mirea.project.repositories.RentalRequestRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RentalRequestService {

    private final RentalRequestRepository repository;

    public RentalRequestService(RentalRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<RentalRequestDTO> findAll(int page, int size, RequestStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RentalRequest> result = (status == null)
                ? repository.findByArchivedFalse(pageable)
                : repository.findByStatusAndArchivedFalse(status, pageable);
        return result.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<RentalRequestDTO> search(RequestStatus status, Priority priority,
                                         LocalDate from, LocalDate to,
                                         int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.search(status, priority, from, to, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public RentalRequestDTO findById(UUID id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Заявка не найдена: " + id));
    }

    @Transactional
    public RentalRequestDTO create(RentalRequestCreateRequestDTO dto) {
        validatePeriod(dto.getStartDate(), dto.getEndDate());

        if (dto.getExternalId() != null && repository.existsByExternalId(dto.getExternalId())) {
            throw new IllegalArgumentException("Заявка с таким внешним идентификатором уже существует");
        }

        RentalRequest entity = new RentalRequest();
        entity.setExternalId(dto.getExternalId());
        entity.setCarId(dto.getCarId());
        entity.setUserId(dto.getUserId());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setMonthlyPrice(dto.getMonthlyPrice());
        entity.setStatus(RequestStatus.NEW);
        entity.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.NORMAL);
        entity.setIsLongTerm(true);
        entity.setArchived(false);
        entity.setNotes(dto.getNotes());
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        RentalRequest saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public RentalRequestDTO update(UUID id, RentalRequestUpdateRequestDTO dto) {
        RentalRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Заявка не найдена"));

        if (dto.getVersion() != null && !dto.getVersion().equals(entity.getVersion())) {
            throw new IllegalArgumentException("Конфликт версий. Данные были изменены другим пользователем");
        }

        if (dto.getStartDate() != null) entity.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) entity.setEndDate(dto.getEndDate());
        if (dto.getMonthlyPrice() != null) entity.setMonthlyPrice(dto.getMonthlyPrice());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getPriority() != null) entity.setPriority(dto.getPriority());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());

        validatePeriod(entity.getStartDate(), entity.getEndDate());
        entity.setUpdatedAt(OffsetDateTime.now());

        RentalRequest saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public void archive(UUID id) {
        RentalRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Заявка не найдена"));
        entity.setArchived(true);
        entity.setArchivedAt(OffsetDateTime.now());
        repository.save(entity);
    }

    @Transactional
    public void restore(UUID id) {
        RentalRequest entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Заявка не найдена"));
        entity.setArchived(false);
        entity.setArchivedAt(null);
        repository.save(entity);
    }

    @Transactional
    public void bulkAction(List<UUID> ids, String action) {
        for (UUID id : ids) {
            if ("ARCHIVE".equalsIgnoreCase(action)) {
                archive(id);
            } else if ("RESTORE".equalsIgnoreCase(action)) {
                restore(id);
            }
        }
    }

    @Transactional(readOnly = true)
    public Map<RequestStatus, Long> getStatistics() {
        return java.util.Arrays.stream(RequestStatus.values())
                .collect(Collectors.toMap(
                        status -> status,
                        repository::countByStatusAndArchivedFalse
                ));
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (end == null || start == null || !end.isAfter(start.plusDays(29))) {
            throw new IllegalArgumentException("Минимальный срок аренды — 30 дней");
        }
    }

    private RentalRequestDTO toDto(RentalRequest e) {
        RentalRequestDTO dto = new RentalRequestDTO();
        dto.setId(e.getId());
        dto.setCarId(e.getCarId());
        dto.setUserId(e.getUserId());
        dto.setStartDate(e.getStartDate());
        dto.setEndDate(e.getEndDate());
        dto.setMonthlyPrice(e.getMonthlyPrice());
        dto.setStatus(e.getStatus());
        dto.setPriority(e.getPriority());
        dto.setIsLongTerm(e.getIsLongTerm());
        dto.setArchived(e.getArchived());
        dto.setNotes(e.getNotes());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    @Transactional
    public int importFromCsv(MultipartFile file) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) continue;

                String[] parts = line.split(",");
                if (parts.length < 6) continue;

                RentalRequest request = new RentalRequest();
                request.setExternalId(parts[0].trim());
                request.setUserId(Long.parseLong(parts[1].trim()));
                request.setCarId(Long.parseLong(parts[2].trim()));
                request.setStartDate(LocalDate.parse(parts[3].trim()));
                request.setEndDate(LocalDate.parse(parts[4].trim()));
                request.setMonthlyPrice(Integer.parseInt(parts[5].trim()));
                request.setStatus(RequestStatus.NEW);
                request.setPriority(Priority.NORMAL);
                request.setNotes(parts.length > 6 ? parts[6].trim() : null);
                request.setCreatedAt(OffsetDateTime.now());
                request.setUpdatedAt(OffsetDateTime.now());
                request.setArchived(false);
                request.setIsLongTerm(true);

                RentalRequest saved = repository.save(request);
                count++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка импорта CSV", e);
        }
        return count;
    }
}