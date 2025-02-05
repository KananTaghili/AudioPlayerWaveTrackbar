plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.kapt)
//    id(libs.plugins.kotlin.kapt.get().pluginId) apply false
    id("kotlin-kapt")
    id("maven-publish")
}

android {
    namespace = "com.kanant.audioplayerwavetrackbar"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from (components["release"])
                groupId = "com.github.KananTaghili"
                artifactId = "AudioPlayerWaveTrackbar"
                version = "1.0.0"
            }
        }
    }
}

dependencies {
//    implementation(libs.androidx.databinding.runtime)
//    kapt(libs.androidx.databinding.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.waveformseekbar)
    implementation(libs.amplituda)
}