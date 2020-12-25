import top.laoshuzi.dependencies.AndroidBuildConfig
import top.laoshuzi.dependencies.deps.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

val signKeyAlias: String by project
val signKeyPassword: String by project
val signStoreFile: String by project
val signStorePassword: String by project

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    compileSdkVersion(AndroidBuildConfig.target_sdk)
    defaultConfig {
        applicationId = AndroidBuildConfig.application_id
        minSdkVersion(AndroidBuildConfig.min_sdk)
        targetSdkVersion(AndroidBuildConfig.target_sdk)
        versionCode = AndroidBuildConfig.version_code
        versionName = AndroidBuildConfig.version_name
        testInstrumentationRunner = AndroidBuildConfig.test_instrumentation_runner
        consumerProguardFiles(AndroidBuildConfig.consumer_file)
        multiDexEnabled = true
    }
    lintOptions {
        isAbortOnError = false
    }
    signingConfigs {
        create("release") {
            keyAlias = signKeyAlias
            keyPassword = signKeyPassword
            storeFile = file(signStoreFile)
            storePassword = signStorePassword
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                AndroidBuildConfig.proguard_file
            )
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                AndroidBuildConfig.proguard_file
            )
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    implementation(project(":signatureview"))

    //kotlin
    implementation(deps(Kotlin.stdlib_jdk8))

    //common
    implementation(deps(Common.core))

    //test
//    testImplementation(deps(Junit.junit))
//    androidTestImplementation(deps(AndroidX.test_runner))
//    androidTestImplementation(deps(AndroidX.test_espresso))

    //koin
    implementation(deps(Koin.android))
    implementation(deps(Koin.android_ext))
    implementation(deps(Koin.android_scope))
    implementation(deps(Koin.android_viewmodel))

    //component
    implementation(deps(AndroidX.multidex))
    implementation(deps(AndroidX.android_material))
    implementation(deps(AndroidX.legacy_support_v4))
    implementation(deps(AndroidX.appcompat))
    implementation(deps(AndroidX.recyclerview))
    implementation(deps(AndroidX.cardview))
    implementation(deps(AndroidX.constraint_layout))

    //lifecycle
    implementation(deps(AndroidX.lifecycle_livedata_ktx))
    implementation(deps(AndroidX.lifecycle_viewmodel_ktx))
    implementation(deps(AndroidX.lifecycle_reactivestreams_ktx))
    implementation(deps(AndroidX.lifecycle_extensions))
//    kapt(deps(AndroidX.lifecycle_compiler))

    //room
    implementation(deps(AndroidX.room_runtime))
    implementation(deps(AndroidX.room_rxjava2))
    implementation(deps(AndroidX.room_ktx))
//    implementation(deps(AndroidX.room_guava))
    kapt(deps(AndroidX.room_compiler))

    //paging
    implementation(deps(AndroidX.paging_runtime_ktx))
    implementation(deps(AndroidX.paging_rxjava2_ktx))

    //navigation
    implementation(deps(AndroidX.navigation_fragment_ktx))
    implementation(deps(AndroidX.navigation_ui_ktx))

    //arouter
    implementation(deps(Arouter.api))
    kapt(deps(Arouter.compiler))

    //rxjava
    implementation(deps(ReactiveX.rx_java))
    implementation(deps(ReactiveX.rx_kotlin))
    implementation(deps(ReactiveX.rx_android))
    implementation(deps(ReactiveX.rx_http))
    kapt(deps(ReactiveX.rx_http_compiler))

    //bus
    implementation(deps(LiveEventBus.live_event_busx))

    //retrofit
    implementation(deps(Retrofit.retrofit))
    implementation(deps(Retrofit.adapter_rxjava2))
    implementation(deps(Retrofit.converter_gson))

    //okhttp
    implementation(deps(Okhttp.okhttp))
    implementation(deps(Okhttp.okhttp_logging_interceptor))

    //json
    implementation(deps(Fastjson.fastjson))
    implementation(deps(Jackson.jackson_annotations))
    implementation(deps(Jackson.jackson_databind))

    //glide
    implementation(deps(Glide.glide))
    kapt(deps(Glide.glide_compiler))

    //tools
    implementation(deps(Utilcode.utilcodex))
    implementation(deps(RvAdapter.zhy_rv_adapter))
    implementation(deps(Fragmentation.fragmentationx_xuexuan))

}
