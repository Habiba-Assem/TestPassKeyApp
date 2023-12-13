plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.test.passkeyapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.test.passkeyapp"
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
            buildConfigField("String", "API_KEY", project.properties["API_KEY"].toString())
            buildConfigField("String", "HOST_URL", project.properties["HOST_URL"].toString())
        }

        debug {
            buildConfigField("String", "API_KEY", project.properties["API_KEY"].toString())
            buildConfigField("String", "HOST_URL", project.properties["STAGING_URL"].toString())
        }
    }

    dataBinding {
        enable = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        kotlinOptions {
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                kotlinOptions.jvmTarget = "1.8"
            }
        }
    }
}

val hiltVersion: String by rootProject.extra
val navVersion: String by rootProject.extra

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Security
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

    // Hilt
    implementation("androidx.hilt:hilt-navigation-fragment:1.1.0")
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.databinding:databinding-runtime:8.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.paging:paging-common-ktx:3.2.1")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.credentials:credentials:1.2.0")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.github.mrmike:ok2curl:0.8.0")

    //credential manager
    implementation ("androidx.credentials:credentials-play-services-auth:1.2.0")
    implementation ("androidx.credentials:credentials:1.2.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}