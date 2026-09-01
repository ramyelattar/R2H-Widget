import org.gradle.api.tasks.Sync

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val debugBundledIconPackAssetRoot = layout.buildDirectory.dir("generated/iconpack-assets/debug")
val releaseBundledIconPackAssetRoot = layout.buildDirectory.dir("generated/iconpack-assets/release")
val debugBundledIconPackPayloadDirectory = debugBundledIconPackAssetRoot.map { it.dir("iconpacks") }
val releaseBundledIconPackPayloadDirectory = releaseBundledIconPackAssetRoot.map { it.dir("iconpacks") }
val debugIconPackApks = mapOf(
    "fluffy.apk" to project(":iconpacks").layout.buildDirectory.file(
        "outputs/apk/fluffy/debug/iconpacks-fluffy-debug.apk",
    ),
    "liquid-dark-glass.apk" to project(":iconpacks").layout.buildDirectory.file(
        "outputs/apk/liquidDarkGlass/debug/iconpacks-liquidDarkGlass-debug.apk",
    ),
    "liquid-siri-glass.apk" to project(":iconpacks").layout.buildDirectory.file(
        "outputs/apk/liquidSiriGlass/debug/iconpacks-liquidSiriGlass-debug.apk",
    ),
    "luxury-vip.apk" to project(":iconpacks").layout.buildDirectory.file(
        "outputs/apk/luxuryVip/debug/iconpacks-luxuryVip-debug.apk",
    ),
)
val releaseIconPackApks = mapOf(
    "fluffy.apk" to project(":iconpacks").layout.buildDirectory.file(
        "outputs/apk/fluffy/release/iconpacks-fluffy-release.apk",
    ),
    "liquid-dark-glass.apk" to project(":iconpacks").layout.buildDirectory.file(
        "outputs/apk/liquidDarkGlass/release/iconpacks-liquidDarkGlass-release.apk",
    ),
    "liquid-siri-glass.apk" to project(":iconpacks").layout.buildDirectory.file(
        "outputs/apk/liquidSiriGlass/release/iconpacks-liquidSiriGlass-release.apk",
    ),
    "luxury-vip.apk" to project(":iconpacks").layout.buildDirectory.file(
        "outputs/apk/luxuryVip/release/iconpacks-luxuryVip-release.apk",
    ),
)

val releaseStoreFile = providers.environmentVariable("R2H_RELEASE_STORE_FILE")
    .orElse(providers.gradleProperty("r2h.release.storeFile"))
    .orNull
val releaseStorePassword = providers.environmentVariable("R2H_RELEASE_STORE_PASSWORD")
    .orElse(providers.gradleProperty("r2h.release.storePassword"))
    .orNull
val releaseKeyAlias = providers.environmentVariable("R2H_RELEASE_KEY_ALIAS")
    .orElse(providers.gradleProperty("r2h.release.keyAlias"))
    .orNull
val releaseKeyPassword = providers.environmentVariable("R2H_RELEASE_KEY_PASSWORD")
    .orElse(providers.gradleProperty("r2h.release.keyPassword"))
    .orNull
val hasLocalReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

val prepareBundledIconPackDebugApks = tasks.register<Sync>("prepareBundledIconPackApks") {
    dependsOn(
        ":iconpacks:assembleFluffyDebug",
        ":iconpacks:assembleLiquidDarkGlassDebug",
        ":iconpacks:assembleLiquidSiriGlassDebug",
        ":iconpacks:assembleLuxuryVipDebug",
    )
    into(debugBundledIconPackPayloadDirectory.get().asFile)
    from(debugIconPackApks.getValue("fluffy.apk")).rename("iconpacks-fluffy-debug.apk", "fluffy.apk")
    from(debugIconPackApks.getValue("liquid-dark-glass.apk"))
        .rename("iconpacks-liquidDarkGlass-debug.apk", "liquid-dark-glass.apk")
    from(debugIconPackApks.getValue("liquid-siri-glass.apk"))
        .rename("iconpacks-liquidSiriGlass-debug.apk", "liquid-siri-glass.apk")
    from(debugIconPackApks.getValue("luxury-vip.apk")).rename("iconpacks-luxuryVip-debug.apk", "luxury-vip.apk")
}

val prepareBundledIconPackReleaseApks = tasks.register<Sync>("prepareBundledIconPackReleaseApks") {
    dependsOn(
        ":iconpacks:assembleFluffyRelease",
        ":iconpacks:assembleLiquidDarkGlassRelease",
        ":iconpacks:assembleLiquidSiriGlassRelease",
        ":iconpacks:assembleLuxuryVipRelease",
    )
    into(releaseBundledIconPackPayloadDirectory.get().asFile)
    from(releaseIconPackApks.getValue("fluffy.apk"))
        .rename("iconpacks-fluffy-release.apk", "fluffy.apk")
    from(releaseIconPackApks.getValue("liquid-dark-glass.apk"))
        .rename("iconpacks-liquidDarkGlass-release.apk", "liquid-dark-glass.apk")
    from(releaseIconPackApks.getValue("liquid-siri-glass.apk"))
        .rename("iconpacks-liquidSiriGlass-release.apk", "liquid-siri-glass.apk")
    from(releaseIconPackApks.getValue("luxury-vip.apk"))
        .rename("iconpacks-luxuryVip-release.apk", "luxury-vip.apk")
}

android {
    namespace = "com.r2h_widget"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.r2h_widget"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        // Keep the checked-in payload as a host-test fallback. Variant-specific
        // generated payloads use the same relative paths and take precedence in
        // the corresponding debug/release asset merge.
        getByName("debug").assets.srcDir(debugBundledIconPackAssetRoot.get().asFile)
        getByName("release").assets.srcDir(releaseBundledIconPackAssetRoot.get().asFile)
    }

    signingConfigs {
        if (hasLocalReleaseSigning) {
            create("r2hLocalRelease") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (hasLocalReleaseSigning) {
                signingConfig = signingConfigs.getByName("r2hLocalRelease")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.configureEach {
    when (name) {
        "preDebugBuild" -> dependsOn(prepareBundledIconPackDebugApks)
        "preReleaseBuild" -> dependsOn(prepareBundledIconPackReleaseApks)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
