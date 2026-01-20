package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.statistics.MonthlySalesDTO;
import com.ecommerce.E_commerce.dto.statistics.SalesStatisticsDTO;
import com.ecommerce.E_commerce.dto.statistics.TopProductDTO;

import java.time.Instant;
import java.util.List;

public interface StatisticsService {
    
    /**
     * Get top products by quantity sold in a given period
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @param limit Maximum number of products to return
     * @return List of top products ordered by quantity sold
     */
    List<TopProductDTO> getTopProductsByQuantity(Instant startDate, Instant endDate, int limit);
    
    /**
     * Get top products by revenue in a given period
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @param limit Maximum number of products to return
     * @return List of top products ordered by revenue
     */
    List<TopProductDTO> getTopProductsByRevenue(Instant startDate, Instant endDate, int limit);
    
    /**
     * Get top products by quantity sold in a specific month
     * @param year Year
     * @param month Month (1-12)
     * @param limit Maximum number of products to return
     * @return List of top products ordered by quantity sold
     */
    List<TopProductDTO> getTopProductsByQuantityForMonth(int year, int month, int limit);
    
    /**
     * Get top products by revenue in a specific month
     * @param year Year
     * @param month Month (1-12)
     * @param limit Maximum number of products to return
     * @return List of top products ordered by revenue
     */
    List<TopProductDTO> getTopProductsByRevenueForMonth(int year, int month, int limit);
    
    /**
     * Get overall sales statistics for a given period
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return Sales statistics including total revenue, orders count, etc.
     */
    SalesStatisticsDTO getSalesStatistics(Instant startDate, Instant endDate);
    
    /**
     * Get monthly sales statistics for a given period
     * @param startDate Start date of the period
     * @param endDate End date of the period
     * @return List of monthly sales statistics
     */
    List<MonthlySalesDTO> getMonthlySales(Instant startDate, Instant endDate);
}
