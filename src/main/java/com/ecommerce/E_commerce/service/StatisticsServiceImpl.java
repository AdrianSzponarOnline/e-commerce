package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.statistics.MonthlySalesDTO;
import com.ecommerce.E_commerce.dto.statistics.SalesStatisticsDTO;
import com.ecommerce.E_commerce.dto.statistics.TopProductDTO;
import com.ecommerce.E_commerce.model.OrderStatus;
import com.ecommerce.E_commerce.repository.OrderItemRepository;
import com.ecommerce.E_commerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    
    private static final List<OrderStatus> COMPLETED_STATUSES = Arrays.asList(
        OrderStatus.CONFIRMED,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED,
        OrderStatus.COMPLETED
    );
    
    @Override
    public List<TopProductDTO> getTopProductsByQuantity(Instant startDate, Instant endDate, int limit) {
        logger.info("Getting top products by quantity: startDate={}, endDate={}, limit={}", startDate, endDate, limit);
        List<Object[]> results = orderItemRepository.findTopProductsByQuantity(
            COMPLETED_STATUSES, startDate, endDate
        );
        
        List<TopProductDTO> topProducts = results.stream()
            .limit(limit)
            .map(this::mapToTopProductDTO)
            .collect(Collectors.toList());
        
        logger.info("Top products by quantity retrieved: count={}, period={} to {}", 
            topProducts.size(), startDate, endDate);
        return topProducts;
    }
    
    @Override
    public List<TopProductDTO> getTopProductsByRevenue(Instant startDate, Instant endDate, int limit) {
        logger.info("Getting top products by revenue: startDate={}, endDate={}, limit={}", startDate, endDate, limit);
        List<Object[]> results = orderItemRepository.findTopProductsByRevenue(
            COMPLETED_STATUSES, startDate, endDate
        );
        
        List<TopProductDTO> topProducts = results.stream()
            .limit(limit)
            .map(this::mapToTopProductDTO)
            .collect(Collectors.toList());
        
        logger.info("Top products by revenue retrieved: count={}, period={} to {}", 
            topProducts.size(), startDate, endDate);
        return topProducts;
    }
    
    @Override
    public List<TopProductDTO> getTopProductsByQuantityForMonth(int year, int month, int limit) {
        logger.info("Getting top products by quantity for month: year={}, month={}, limit={}", year, month, limit);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        
        List<TopProductDTO> topProducts = getTopProductsByQuantity(start, end, limit);
        logger.info("Top products by quantity for month retrieved: count={}, month={}/{}", 
            topProducts.size(), year, month);
        return topProducts;
    }
    
    @Override
    public List<TopProductDTO> getTopProductsByRevenueForMonth(int year, int month, int limit) {
        logger.info("Getting top products by revenue for month: year={}, month={}, limit={}", year, month, limit);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        
        List<TopProductDTO> topProducts = getTopProductsByRevenue(start, end, limit);
        logger.info("Top products by revenue for month retrieved: count={}, month={}/{}", 
            topProducts.size(), year, month);
        return topProducts;
    }
    
    @Override
    public SalesStatisticsDTO getSalesStatistics(Instant startDate, Instant endDate) {
        logger.info("Getting sales statistics: startDate={}, endDate={}", startDate, endDate);
        
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(
            COMPLETED_STATUSES, startDate, endDate
        );
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        Long totalOrders = orderRepository.countOrders(
            COMPLETED_STATUSES, startDate, endDate
        );
        if (totalOrders == null) {
            totalOrders = 0L;
        }
        
        Long totalProductsSold = orderItemRepository.countTotalProductsSold(
            COMPLETED_STATUSES, startDate, endDate
        );
        if (totalProductsSold == null) {
            totalProductsSold = 0L;
        }
        
        BigDecimal averageOrderValue = orderRepository.calculateAverageOrderValue(
            COMPLETED_STATUSES, startDate, endDate
        );
        if (averageOrderValue == null) {
            averageOrderValue = BigDecimal.ZERO;
        }
        
        logger.info("Sales statistics calculated: totalRevenue={}, totalOrders={}, totalProductsSold={}, averageOrderValue={}",
            totalRevenue, totalOrders, totalProductsSold, averageOrderValue);
        
        return new SalesStatisticsDTO(
            totalRevenue.setScale(2, RoundingMode.HALF_UP),
            totalOrders,
            totalProductsSold,
            averageOrderValue.setScale(2, RoundingMode.HALF_UP),
            startDate,
            endDate
        );
    }
    
    @Override
    public List<MonthlySalesDTO> getMonthlySales(Instant startDate, Instant endDate) {
        logger.info("Getting monthly sales: startDate={}, endDate={}", startDate, endDate);
        List<Object[]> results = orderRepository.findMonthlySales(
            COMPLETED_STATUSES, startDate, endDate
        );
        
        List<MonthlySalesDTO> monthlySales = results.stream()
            .map(this::mapToMonthlySalesDTO)
            .collect(Collectors.toList());
        
        logger.info("Monthly sales statistics retrieved: {} months, period={} to {}", 
            monthlySales.size(), startDate, endDate);
        return monthlySales;
    }
    
    private TopProductDTO mapToTopProductDTO(Object[] row) {
        Long productId = ((Number) row[0]).longValue();
        String productName = (String) row[1];
        String productSku = (String) row[2];
        Long totalQuantity = ((Number) row[3]).longValue();
        BigDecimal totalRevenue = (BigDecimal) row[4];
        Long orderCount = ((Number) row[5]).longValue();
        
        return new TopProductDTO(
            productId,
            productName,
            productSku,
            totalQuantity,
            totalRevenue != null ? totalRevenue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
            orderCount
        );
    }
    
    private MonthlySalesDTO mapToMonthlySalesDTO(Object[] row) {
        Integer year = ((Number) row[0]).intValue();
        Integer month = ((Number) row[1]).intValue();
        BigDecimal totalRevenue = (BigDecimal) row[2];
        Long totalOrders = ((Number) row[3]).longValue();
        
        String[] monthNames = {
            "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
            "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
        };
        
        // Get total products sold for this month
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        Instant monthStartInstant = monthStart.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant monthEndInstant = monthEnd.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        
        Long totalProductsSold = orderItemRepository.countTotalProductsSold(
            COMPLETED_STATUSES, monthStartInstant, monthEndInstant
        );
        if (totalProductsSold == null) {
            totalProductsSold = 0L;
        }
        
        return new MonthlySalesDTO(
            year,
            month,
            monthNames[month - 1],
            totalRevenue != null ? totalRevenue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
            totalOrders,
            totalProductsSold
        );
    }
}
