package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.statistics.MonthlySalesDTO;
import com.ecommerce.E_commerce.dto.statistics.SalesStatisticsDTO;
import com.ecommerce.E_commerce.dto.statistics.TopProductDTO;
import com.ecommerce.E_commerce.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Validated
public class StatisticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    private final StatisticsService statisticsService;
    
    /**
     * Get top products by quantity sold in a given period
     */
    @GetMapping("/products/top-by-quantity")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsByQuantity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "Start date cannot be in the future") Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "End date cannot be in the future") Instant endDate,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be at least 1") @Max(value = 100, message = "Limit cannot exceed 100") int limit) {
        
        // Default to last 30 days if not specified
        if (startDate == null) {
            startDate = Instant.now().minusSeconds(30 * 24 * 60 * 60);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            logger.warn("GET /api/statistics/products/top-by-quantity - Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        logger.info("GET /api/statistics/products/top-by-quantity - startDate={}, endDate={}, limit={}", 
            startDate, endDate, limit);
        
        List<TopProductDTO> topProducts = statisticsService.getTopProductsByQuantity(startDate, endDate, limit);
        logger.info("GET /api/statistics/products/top-by-quantity - Successfully retrieved {} top products", topProducts.size());
        return ResponseEntity.ok(topProducts);
    }
    
    /**
     * Get top products by revenue in a given period
     */
    @GetMapping("/products/top-by-revenue")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsByRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "Start date cannot be in the future") Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "End date cannot be in the future") Instant endDate,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be at least 1") @Max(value = 100, message = "Limit cannot exceed 100") int limit) {
        
        if (startDate == null) {
            startDate = Instant.now().minusSeconds(30 * 24 * 60 * 60);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            logger.warn("GET /api/statistics/products/top-by-revenue - Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        logger.info("GET /api/statistics/products/top-by-revenue - startDate={}, endDate={}, limit={}", 
            startDate, endDate, limit);
        
        List<TopProductDTO> topProducts = statisticsService.getTopProductsByRevenue(startDate, endDate, limit);
        logger.info("GET /api/statistics/products/top-by-revenue - Successfully retrieved {} top products", topProducts.size());
        return ResponseEntity.ok(topProducts);
    }
    
  
    @GetMapping("/products/top-by-quantity/month")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsByQuantityForMonth(
            @RequestParam @Min(value = 2000, message = "Year must be at least 2000") @Max(value = 2100, message = "Year cannot exceed 2100") int year,
            @RequestParam @Min(value = 1, message = "Month must be between 1 and 12") @Max(value = 12, message = "Month must be between 1 and 12") int month,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be at least 1") @Max(value = 100, message = "Limit cannot exceed 100") int limit) {
        
        logger.info("GET /api/statistics/products/top-by-quantity/month - year={}, month={}, limit={}", 
            year, month, limit);
        
        List<TopProductDTO> topProducts = statisticsService.getTopProductsByQuantityForMonth(year, month, limit);
        logger.info("GET /api/statistics/products/top-by-quantity/month - Successfully retrieved {} top products for {}/{}", 
            topProducts.size(), year, month);
        return ResponseEntity.ok(topProducts);
    }
    
    
    @GetMapping("/products/top-by-revenue/month")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TopProductDTO>> getTopProductsByRevenueForMonth(
            @RequestParam @Min(value = 2000, message = "Year must be at least 2000") @Max(value = 2100, message = "Year cannot exceed 2100") int year,
            @RequestParam @Min(value = 1, message = "Month must be between 1 and 12") @Max(value = 12, message = "Month must be between 1 and 12") int month,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be at least 1") @Max(value = 100, message = "Limit cannot exceed 100") int limit) {
        
        logger.info("GET /api/statistics/products/top-by-revenue/month - year={}, month={}, limit={}", 
            year, month, limit);
        
        List<TopProductDTO> topProducts = statisticsService.getTopProductsByRevenueForMonth(year, month, limit);
        logger.info("GET /api/statistics/products/top-by-revenue/month - Successfully retrieved {} top products for {}/{}", 
            topProducts.size(), year, month);
        return ResponseEntity.ok(topProducts);
    }
    
    
    @GetMapping("/sales")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SalesStatisticsDTO> getSalesStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "Start date cannot be in the future") Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "End date cannot be in the future") Instant endDate) {
        
        if (startDate == null) {
            startDate = Instant.now().minusSeconds(30 * 24 * 60 * 60);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            logger.warn("GET /api/statistics/sales - Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        logger.info("GET /api/statistics/sales - startDate={}, endDate={}", startDate, endDate);
        
        SalesStatisticsDTO statistics = statisticsService.getSalesStatistics(startDate, endDate);
        logger.info("GET /api/statistics/sales - Successfully retrieved sales statistics: totalRevenue={}, totalOrders={}, totalProductsSold={}", 
            statistics.totalRevenue(), statistics.totalOrders(), statistics.totalProductsSold());
        return ResponseEntity.ok(statistics);
    }
    
  
    @GetMapping("/sales/monthly")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<MonthlySalesDTO>> getMonthlySales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "Start date cannot be in the future") Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @PastOrPresent(message = "End date cannot be in the future") Instant endDate) {
        
        if (startDate == null) {
            LocalDate twelveMonthsAgo = LocalDate.now().minusMonths(12);
            startDate = twelveMonthsAgo.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            logger.warn("GET /api/statistics/sales/monthly - Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            return ResponseEntity.badRequest().build();
        }
        
        logger.info("GET /api/statistics/sales/monthly - startDate={}, endDate={}", startDate, endDate);
        
        List<MonthlySalesDTO> monthlySales = statisticsService.getMonthlySales(startDate, endDate);
        logger.info("GET /api/statistics/sales/monthly - Successfully retrieved monthly sales statistics: {} months", monthlySales.size());
        return ResponseEntity.ok(monthlySales);
    }
}
