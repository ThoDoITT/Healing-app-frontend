plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.healingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.healingapp"
        minSdk = 24
        targetSdk = 34
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

//   buildFeatures {
//       viewBinding = true
//   }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.18")

    val room_version = "2.7.1"
    val retrofit_version = "2.9.0"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")


    // Lifecycle components (for LiveData/ViewModel if you expand later)
    val lifecycle_version = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:$retrofit_version") // Kiểm tra phiên bản mới nhất
    implementation ("com.squareup.retrofit2:converter-gson:$retrofit_version") // Bộ chuyển đổi Gson

    // OkHttp
    implementation ("com.squareup.okhttp3:okhttp:4.12.0") // Kiểm tra phiên bản mới nhất
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // lưu session login
    implementation("androidx.security:security-crypto:1.0.0")

    // chart

//    implementation ("com.github.AnyChart:AnyChart-Android:1.1.5")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
}