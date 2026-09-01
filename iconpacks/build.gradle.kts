plugins {
    alias(libs.plugins.android.application)
}

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

android {
    namespace = "com.r2h_widget.iconpack"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
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

    flavorDimensions += "pack"
    productFlavors {
        create("fluffy") {
            dimension = "pack"
            applicationId = "com.r2h_widget.iconpack.fluffy"
        }
        create("liquidDarkGlass") {
            dimension = "pack"
            applicationId = "com.r2h_widget.iconpack.darkglass"
        }
        create("liquidSiriGlass") {
            dimension = "pack"
            applicationId = "com.r2h_widget.iconpack.siriglass"
        }
        create("luxuryVip") {
            dimension = "pack"
            applicationId = "com.r2h_widget.iconpack.luxuryvip"
        }
    }

    buildTypes {
        release {
            // Nothing Launcher consumes the appfilter/drawable resource names
            // directly. Keep those static names intact in the distributed APK;
            // release resource optimization would otherwise rename them.
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
}

dependencies {
    // The icon-pack companion is intentionally a small platform-only APK.
}
