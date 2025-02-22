plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

apply(from = "../gradle/common.gradle.kts")

android {
    namespace = "io.github.dovecoteescapee.byedpi.common.system"
    compileSdk = extra["compileSdkVersion"] as Int

    defaultConfig {
        minSdk = extra["minSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    kotlinOptions {
        jvmTarget = extra["kotlinJvmTarget"] as String
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}