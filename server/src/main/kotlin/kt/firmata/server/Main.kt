package kt.firmata.server

import net.harawata.appdirs.AppDirsFactory
import org.springframework.boot.runApplication
import java.nio.file.Path
import kotlin.io.path.createDirectories

fun main(args: Array<String>) {
    val appDirs = AppDirsFactory.getInstance()
    val appDir = Path.of(appDirs.getUserConfigDir("KtFirmataServer", "0.1.0", "tiagohm")).createDirectories()

    System.setProperty("app.dir", "$appDir")

    runApplication<App>(*args)
}
