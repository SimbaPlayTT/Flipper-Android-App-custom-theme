import java.util.Properties

plugins {
    id("flipper.android-compose")
    id("flipper.anvil")
    id("kotlinx-serialization")
}

android.namespace = "com.flipperdevices.settings.impl"

// Read from the git-ignored local.properties (never committed) so the feedback webhook URL
// never ends up in source control - public builds simply get an empty default and the feature
// silently no-ops. Set `feedback_webhook_url=...` in your own local.properties to enable it.
val feedbackWebhookUrl = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}.getProperty("feedback_webhook_url", "")

android {
    buildTypes {
        defaultConfig {
            buildConfigField(
                "String",
                "FEEDBACK_WEBHOOK_URL",
                "\"$feedbackWebhookUrl\""
            )
        }
    }
}

dependencies {
    implementation(projects.components.settings.api)

    implementation(projects.components.core.di)
    implementation(projects.components.core.log)
    implementation(projects.components.core.preference)
    implementation(projects.components.core.ktx)
    implementation(projects.components.core.share)
    implementation(projects.components.core.activityholder)
    implementation(projects.components.analytics.shake2report.api)
    implementation(projects.components.analytics.metric.api)
    implementation(projects.components.core.ui.res)
    implementation(projects.components.core.ui.ktx)
    implementation(projects.components.core.ui.lifecycle)
    implementation(projects.components.core.ui.dialog)
    implementation(projects.components.core.ui.decompose)

    implementation(projects.components.core.ui.theme)

    implementation(projects.components.debug.api)
    implementation(projects.components.firstpair.api)
    implementation(projects.components.selfupdater.api)
    implementation(projects.components.nfc.mfkey32.api)
    implementation(projects.components.faphub.installation.all.api)
    implementation(projects.components.selfupdater.api)
    implementation(projects.components.notification.api)
    implementation(projects.components.inappnotification.api)
    implementation(projects.components.filemngr.main.api)

    implementation(projects.components.bridge.dao.api)
    implementation(projects.components.bridge.connection.feature.common.api)
    implementation(projects.components.bridge.connection.feature.provider.api)
    implementation(projects.components.bridge.connection.feature.restartrpc.api)
    implementation(projects.components.bridge.connection.feature.rpc.api)
    implementation(projects.components.bridge.connection.pbutils)
    implementation(projects.components.bridge.synchronization.api)
    implementation(libs.ble.common)

    implementation(libs.appcompat)

    // Compose
    implementation(libs.compose.activity)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    implementation(libs.compose.ui)
    implementation(libs.compose.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.bundles.decompose)

    implementation(libs.lifecycle.compose)

    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.immutable.collections)

    implementation(libs.ktor.client)
}
