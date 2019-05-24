package pe.com.acme.worflowacme.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivan on 23/05/2019.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "pe.com.acme.worflowacme",
        entityManagerFactoryRef = "acmeEntityManager",
        transactionManagerRef = "acmeTransactionManager"
)
public class DatabaseAcmeConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "acme.datasource")
    public DataSource acmeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean acmeEntityManager(EntityManagerFactoryBuilder entityManagerFactoryBuilder,
                                                                    @Qualifier("acmeDataSource") DataSource dataSource) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");

        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages("pe.com.acme.worflowacme.domain")
                .persistenceUnit("acmePU")
                .properties(props)
                .build();
    }

    @Bean
    public PlatformTransactionManager acmeTransactionManager(@Qualifier("acmeEntityManager") EntityManagerFactory entityManagerFactory){
        return new JpaTransactionManager(entityManagerFactory);
    }

}
