package my.maleva.api.module.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataDto {
    private KPIStats kpiStats;
    private List<DepartmentPerformanceDto> teamPerformance;
    private List<ApprovalQueueDto> approvalQueue;
    private List<RevenueDataDto> weeklyRevenue;
    private List<RevenueDataDto> monthlyRevenue;
    private SystemHealthDto systemHealth;
    private List<DashboardGridItemDto> todayPickups;
    private List<DashboardGridItemDto> tomorrowPickups;
    private List<DashboardGridItemDto> pendingVesselPayments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KPIStats {
        private double totalRevenue;
        private int activeOperations;
        private double teamEfficiency;
        private int pendingApprovals;
        private double revenueChangePercent;
        private int operationsChange;
        // New fields from legacy AdminDashboard.js
        private int todaySalesCount;
        private double todaySalesAmount;
        private int yesterdaySalesCount;
        private double yesterdaySalesAmount;
        private int weekSalesCount;
        private double weekSalesAmount;
        private int monthSalesCount;
        private double monthSalesAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardGridItemDto {
        private Long id;
        private String name;
        private String jobNo;
        private String customerName;
        private double amount;
        private int statusId; // SDId in legacy
        private String detailedId;
        private String pkg;
        private String blCopy;
        private String awbNo;
        private String sPort;
        private String oPort;
        private String employeeName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentPerformanceDto {
        private String department;
        private double score;
        private int staffCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalQueueDto {
        private String item;
        private String priority;
        private String timeRemaining;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataDto {
        private String label;
        private double amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealthDto {
        private int usersOnline;
        private double uptimePercent;
        private int totalRegisteredUsers;
    }
}
