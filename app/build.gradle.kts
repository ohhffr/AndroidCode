
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.englishgptapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.englishgptapplication"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.github.bumptech.glide:glide:4.13.2")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation("androidx.databinding:databinding-runtime:7.1.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("io.github.everythingme:overscroll-decor-android:1.1.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("io.github.scwang90:refresh-layout-kernel:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-classics:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-footer-classics:3.0.0-alpha")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("io.github.ShawnLin013:number-picker:2.4.13")
    implementation("com.github.lukelorusso:VerticalSeekBar:1.2.7")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}