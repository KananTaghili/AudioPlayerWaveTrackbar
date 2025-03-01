import org.gradle.kotlin.dsl.from

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.kanant.audioplayerwavetrackbar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kanant.audioplayerwavetrackbar"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.library")) {
            apply(plugin = "maven-publish")
            publishing {
                publications {
                    create<MavenPublication>("mavenAar") {
                        from(components["release"])
                        groupId = "com.github.KananTaghili"
                        artifactId = "AudioPlayerWaveTrackbar"
                        version = "1.0.0"
                    }
                }
                // ... repositories ...
            }
        }
    }
}
//afterEvaluate {
//    publishing {
//        publications {
//            create<MavenPublication>("maven") {
//                from (components["release"])
//                groupId = "com.github.KananTaghili"
//                artifactId = "AudioPlayerWaveTrackbar"
//                version = "1.0.0"
//            }
//        }
//    }
//}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}