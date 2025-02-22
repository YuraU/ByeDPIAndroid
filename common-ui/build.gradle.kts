plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

apply(from = "../gradle/common.gradle.kts")

android {
    namespace = "io.github.dovecoteescapee.byedpi.common.ui"
    compileSdk = extra["compileSdkVersion"] as Int

    defaultConfig {
        minSdk = extra["minSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = extra["kotlinJvmTarget"] as String
    }
}

dependencies {
    implementation(project(":common-system"))

    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.material3:material3:$1.3.1")


    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}