plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

apply(from = "../gradle/common.gradle.kts")

android {
    namespace = "io.github.dovecoteescapee.byedpi.feature.connection.ui"
    compileSdk = extra["compileSdkVersion"] as Int

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    buildFeatures {
        compose = true
    }

    defaultConfig {
        minSdk = extra["minSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = extra["kotlinJvmTarget"] as String
    }
}

dependencies {
    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.runtime) // Добавляем runtime

    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)

    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.activity.compose)

    implementation(project(":app-bypass-services"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-bypass-api"))
    implementation(project(":common-storage"))
    implementation(project(":common-system"))
    implementation(project(":common-ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}