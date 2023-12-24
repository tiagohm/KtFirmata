package kt.firmata.server.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@Configuration
class AppConfig {

    @Bean
    fun appPath(): Path {
        return Path.of(System.getProperty("app.dir"))
    }

    @Bean
    fun propertiesPath(appPath: Path): Path {
        return Path.of("$appPath", "app.properties")
    }

    @Bean
    fun properties(propertiesPath: Path): Properties {
        val properties = Properties()
        propertiesPath.takeIf { it.exists() }?.inputStream()?.use(properties::load)
        LOG.info("Loaded {} properties from {}", properties.size, propertiesPath)
        return properties
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(AppConfig::class.java)
    }
}
