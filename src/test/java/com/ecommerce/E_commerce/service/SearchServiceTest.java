package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.mapper.ProductMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Note: SearchService uses Hibernate Search which is complex to mock.
 * This test verifies basic structure. For comprehensive testing,
 * consider using integration tests with test containers or embedded Elasticsearch.
 */
@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ProductMapper productMapper;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(entityManager, productMapper);
    }

    @Test
    void searchService_ShouldBeInstantiated() {
        assertNotNull(searchService);
    }
}

