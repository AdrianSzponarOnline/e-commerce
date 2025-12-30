package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.model.Product;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.mapper.orm.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class SearchIndexer implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchIndexer.class);
    private final EntityManager em;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Starting ElasticSearch indexing process");
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(status -> {
            try {
                SearchSession searchSession = Search.session(em);

                MassIndexer indexer = searchSession.massIndexer(Product.class)
                        .threadsToLoadObjects(4)
                        .batchSizeToLoadObjects(25);

                indexer.startAndWait();

                logger.info("ElasticSearch indexing completed successfully!");
            } catch (InterruptedException e) {
                logger.error("ElasticSearch indexing failed: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            return null;
        });
    }
}
