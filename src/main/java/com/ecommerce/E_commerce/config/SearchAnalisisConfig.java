package com.ecommerce.E_commerce.config;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;
import org.springframework.stereotype.Component;

@Component("AnalysisConfigurer")
public class SearchAnalisisConfig implements ElasticsearchAnalysisConfigurer {
    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext elasticsearchAnalysisConfigurationContext) {
        elasticsearchAnalysisConfigurationContext.analyzer("english").type("english");
    }
}
