package pe.com.acme.worflowacme.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

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

    @Bean(name = "batchJpaVendorAdapter")
    public JpaVendorAdapter batchJpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean(name = "acmeEntityManager")
    public LocalContainerEntityManagerFactoryBean acmeEntityManager() {
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(acmeDataSource());
        emfBean.setPackagesToScan("pe.com.acme.worflowacme");
        emfBean.setBeanName("acmeEntityManager");
        emfBean.setJpaVendorAdapter(batchJpaVendorAdapter());

        Properties jpaProps = new Properties();
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        jpaProps.put("hibernate.jdbc.fetch_size", "200");
        jpaProps.put("hibernate.jdbc.batch_size", "100");
        jpaProps.put("hibernate.order_inserts", "true");
        jpaProps.put("hibernate.order_updates", "true");
        jpaProps.put("hibernate.show_sql", "true");
        jpaProps.put("hibernate.format_sql", "true");

        emfBean.setJpaProperties(jpaProps);

        return emfBean;
    }

    /*@Bean
    public LocalContainerEntityManagerFactoryBean acmeEntityManager(EntityManagerFactoryBuilder entityManagerFactoryBuilder,
                                                                    @Qualifier("acmeDataSource") DataSource dataSource) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        props.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        props.put("hibernate.show_sql", "true");
        props.put("hibernate.format_sql", "true");

        return entityManagerFactoryBuilder
                .dataSource(dataSource)
                .packages("pe.com.acme.worflowacme.domain")
                .persistenceUnit("acmePU")
                .properties(props)
                .build();
    }*/

    @Bean
    public PlatformTransactionManager acmeTransactionManager() {
        return new JpaTransactionManager(acmeEntityManager().getObject());
    }

}
