package ru.mirea.project.user.dto;

public record UserAnalyticsResponse(
        long totalUsers,
        long activeUsers,
        long archivedUsers,
        long regularUsers,
        long vipUsers,
        double vipPercentage,
        long usersWithRequests,
        long usersWithoutRequests,
        long totalRequests,
        double averageRequestsPerUser
) {}
