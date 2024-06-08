plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "online.lemonxy.downloader_le"
    compileSdk = 34

    defaultConfig {
        applicationId = "online.lemonxy.downloader_le"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_20
        targetCompatibility = JavaVersion.VERSION_20
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
}