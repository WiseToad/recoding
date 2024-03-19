package ru.sozvezdie.recoding.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "application")
class ApplicationProperties {

    var trustStoreLocation: String = Constant.DEFAULT_TRUST_STORE_LOCATION
    var trustStorePassword: String = Constant.DEFAULT_TRUST_STORE_PASSWORD

    var kafkaPrefix = ""
    var kafkaPostfix = ""

    var isIndividualConsumerGroup = false

    var skipKafkaDeserializationError: Boolean = false

    companion object {
        @JvmStatic
        val hostName: String =
            System.getenv("HOSTNAME") ?:
            System.getenv("COMPUTERNAME") ?:
            throw RuntimeException("Failed to obtain host name")
    }
}
