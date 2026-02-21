plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication10"
    compileSdk {
        version = release(35)
    }

    val userHome = System.getProperty("user.home")
    val localJksFile = file("$userHome/android.jks")
    val hasLocalKeystore = localJksFile.exists()

    signingConfigs {
        if (hasLocalKeystore) {
            getByName("debug") {
                storeFile = localJksFile
                storePassword = "123456Aa@"
                keyPassword = "123456Aa@"
                keyAlias = "key0"
            }
            create("release") {
                storeFile = localJksFile
                storePassword = "123456Aa@"
                keyPassword = "123456Aa@"
                keyAlias = "key0"
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.myapplication10"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        jniLibs {
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (hasLocalKeystore) signingConfigs.getByName("release") else null

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = if (hasLocalKeystore) signingConfigs.getByName("debug") else null
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.foundation:foundation")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended") // 添加这一行
}
