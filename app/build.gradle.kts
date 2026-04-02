plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("jacoco")
}

android {
    namespace = "com.rodbailey.covid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rodbailey.covid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"?
        testInstrumentationRunner = "com.rodbailey.covid.core.di.CustomTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:strongSkipping=true"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoUnitTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*",
        "**/*_Hilt*.*", "**/Hilt_*.*", "**/*_Factory*.*", "**/*_MembersInjector*.*"
    )
    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/*.exec")
    })
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.03"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Retrofit 2.6.0+ is required to get "suspend fun" support
    // Note: Retrofit 3.x and OkHttp 5.x require Kotlin 2.x — upgrade separately
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

//    def room_version = "2.6.1"

    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // To use Kotlin annotation processing tool (kapt)
//    kapt("androidx.room:room-compiler:2.6.1")
    // To use Kotlin Symbol Processing (KSP)
    //ksp "androidx.room:room-compiler:$room_version"

    // Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")

    // Enables " = viewModel()" access to view model in compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Constraint layout for compose (used by data panel)
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Turbine - for testing Flows
    androidTestImplementation("app.cash.turbine:turbine:1.1.0")
}