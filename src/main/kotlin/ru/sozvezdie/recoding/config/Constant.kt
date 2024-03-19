package ru.sozvezdie.recoding.config

object Constant {

    const val APPLICATION_NAME_PROPERTY = "spring.application.name"
    const val SERVER_PORT_PROPERTY = "server.port"
    const val SERVER_CONTEXT_PATH_PROPERTY = "server.servlet.context-path"
    const val SERVER_SSL_KEY_STORE_PROPERTY = "server.ssl.key-store"

    const val DEFAULT_TRUST_STORE_LOCATION = "classpath:ssl/truststore.jks"
    const val DEFAULT_TRUST_STORE_PASSWORD = "*****"
    const val TRUST_STORE_SYSTEM_PROPERTY = "javax.net.ssl.trustStore"
    const val TRUST_STORE_PASSWORD_SYSTEM_PROPERTY = "javax.net.ssl.trustStorePassword"

    const val SPRING_PROFILE_DEVELOPMENT = "dev"
    const val SPRING_PROFILE_PRODUCTION = "prod"

    const val KEY_DELIMITER = "\u007f"

    object Topic {
        const val RECODE_MAPPING_CDC_DATA = "star-recode.public.recode_mapping"
        const val RECODE_MAPPING_VALUE_CDC_DATA = "star-recode.public.recode_mapping_value"
        const val RECODE_SCHEMA_CDC_DATA = "star-recode.public.recode_schema"
        const val RECODE_SCHEMA_HANDLER_CDC_DATA = "star-recode.public.recode_schema_handler"
        const val RECODE_SCHEMA_LINK_CDC_DATA = "star-recode.public.recode_schema_link"

        const val CHANGELOG_POSTFIX = "-changelog"
    }

    object StoreName {
        const val RECODE_SCHEMA_LINK = "star-recode-schema-link"
    }

    object ConsumerGroup {
        const val BUS_POSTFIX = "-bus"
        const val SEPARATOR_CHAR = "-"
    }
}
