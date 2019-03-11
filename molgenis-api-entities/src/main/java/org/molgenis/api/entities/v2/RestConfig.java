package org.molgenis.api.entities.v2;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import java.util.Set;
import org.molgenis.api.entities.SortConverter;
import org.molgenis.web.rsql.AggregateQueryRsqlConverter;
import org.molgenis.web.rsql.QueryRsqlConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RestConfig implements WebMvcConfigurer {
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(attributeFilterConverter());
    registry.addConverter(sortConverter());
    registry.addConverter(rsqlQueryConverter());
    registry.addConverter(aggregateQueryRsqlConverter());
  }

  @Bean
  public AttributeFilterConverter attributeFilterConverter() {
    return new AttributeFilterConverter();
  }

  @Bean
  public SortConverter sortConverter() {
    return new SortConverter();
  }

  @Bean
  public QueryRsqlConverter rsqlQueryConverter() {
    return new QueryRsqlConverter(rsqlParser());
  }

  @Bean
  public AggregateQueryRsqlConverter aggregateQueryRsqlConverter() {
    return new AggregateQueryRsqlConverter(rsqlParser());
  }

  @Bean
  public RSQLParser rsqlParser() {
    Set<ComparisonOperator> operators = RSQLOperators.defaultOperators();
    operators.add(new ComparisonOperator("=q=", false));
    operators.add(new ComparisonOperator("=notlike=", false));
    operators.add(new ComparisonOperator("=rng=", true));
    operators.add(new ComparisonOperator("=like=", false));
    return new RSQLParser(operators);
  }
}
