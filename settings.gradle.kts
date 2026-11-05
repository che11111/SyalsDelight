import java.util.Properties

rootProject.name = "syalsdelight"

// FarmersDelight 源码位置只从本机配置、Gradle 属性或环境变量读取。
val farmersDelightRepo = "https://github.com/IOVEYOUMC0/Farmersdelight-Plugin.git"
val localProperties = Properties()
val localPropertiesFile = file("local.properties")
if (localPropertiesFile.isFile) {
    localPropertiesFile.reader(Charsets.UTF_8).use { localProperties.load(it) }
}
val configuredCheckout = providers.gradleProperty("farmersDelightCheckout").orNull
    ?: providers.environmentVariable("FARMERSDELIGHT_CHECKOUT").orNull
    ?: localProperties.getProperty("farmersDelightCheckout")
val farmersDelightCheckout = configuredCheckout
    ?.takeIf { it.isNotBlank() }
    ?.let(::file)
    ?.takeIf { it.isDirectory }

sourceControl {
    gitRepository(uri(farmersDelightRepo)) {
        producesModule("com.huidu.farmersdelight:farmersdelight-plugin")
    }
}

if (farmersDelightCheckout != null) {
    includeBuild(farmersDelightCheckout) {
        name = "farmersdelight-plugin"
    }
}

