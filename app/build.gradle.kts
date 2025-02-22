plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.github.dovecoteescapee.byedpi"
    compileSdk = 35

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    defaultConfig {
        applicationId = "io.github.romanvht.byedpi"
        minSdk = 23
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 10
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
    }

    buildFeatures {
        compose = true
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            buildConfigField("String", "VERSION_NAME",  "\"${defaultConfig.versionName}\"")

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            buildConfigField("String", "VERSION_NAME",  "\"${defaultConfig.versionName}-debug\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        viewBinding = true
    }

    // https://android.izzysoft.de/articles/named/iod-scan-apkchecks?lang=en#blobs
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
}

dependencies {
    implementation(project(":feature-bypass-api"))
    implementation(project(":feature-bypass-test"))
    implementation(project(":app-bypass-services"))
    implementation(project(":feature-bypass-api"))
    implementation(project(":feature-connection-ui"))
    implementation(project(":feature-settings"))
    implementation(project(":common-ui"))
    implementation(project(":common-system"))
    implementation(project(":common-storage"))

    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.runtime)

    //noinspection GradleDependency
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.preferencex)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.gson)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

tasks.register<Exec>("runNdkBuild") {
    group = "build"

    val ndkDir = android.ndkDirectory
    executable = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) {
        "$ndkDir\\ndk-build.cmd"
    } else {
        "$ndkDir/ndk-build"
    }
    setArgs(listOf(
        "NDK_PROJECT_PATH=build/intermediates/ndkBuild",
        "NDK_LIBS_OUT=src/main/jniLibs",
        "APP_BUILD_SCRIPT=src/main/jni/Android.mk",
        "NDK_APPLICATION_MK=src/main/jni/Application.mk"
    ))

    println("Command: $commandLine")
}

tasks.preBuild {
    dependsOn("runNdkBuild")
}
