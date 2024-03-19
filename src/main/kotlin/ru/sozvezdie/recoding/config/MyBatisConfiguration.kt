package ru.sozvezdie.recoding.config

import com.puls.centralpricing.handlers.config.MyBatisConfig
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import java.util.*
import javax.sql.DataSource

@Configuration
@MapperScan(basePackages = ["ru.sozvezdie.recoding.mapper"], annotationClass = Mapper::class)
class MyBatisConfiguration(
    private val dataSource: DataSource,
    private val env: Environment
) {
    fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory {
        val sessionFactory = SqlSessionFactoryBean()
        sessionFactory.setDataSource(dataSource)

        val configuration = Objects.requireNonNull(sessionFactory.getObject()).configuration
        val profile = if (env.acceptsProfiles(Profiles.of(Constant.SPRING_PROFILE_PRODUCTION))) Constant.SPRING_PROFILE_PRODUCTION else Constant.SPRING_PROFILE_DEVELOPMENT
        MyBatisConfig.init(configuration, profile)

        return sessionFactory.getObject()
    }

    @Bean
    @Primary
    fun sqlSessionTemplate(): SqlSessionTemplate = SqlSessionTemplate(sqlSessionFactory(dataSource), ExecutorType.REUSE)

    @Bean(name = ["batchSqlSessionTemplate"])
    fun batchSqlSessionTemplate(): SqlSessionTemplate = SqlSessionTemplate(sqlSessionFactory(dataSource), ExecutorType.BATCH)
}
