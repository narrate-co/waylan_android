/**
 * All versioning used across all build files of all modules, sorted alphabetically.
 *
 * Build files should only need access to build versioning from this object and instead get
 * library dependencies from the [Libs] object.
 */
object Versions {
    // Build constants
    const val compileSdk = 29
    const val minSdk = 23
    const val targetSdk = 29

    // Library versions
    const val androidX = "1.2.0-alpha04"
    const val appCompat = "1.1.0"
    const val constraintLayout = "1.1.3"
    const val coroutines = "1.2.1"
    const val crashlytics = "2.10.1"
    const val dagger = "2.16"
    const val dokka = "0.9.17"
    const val espresso = "3.3.0-alpha02"
    const val fabric = "1.28.0"
    const val firebaseAuth = "19.0.0"
    const val firebaseCore = "17.2.0"
    const val firestore = "21.1.1"
    const val fragment = "1.1.0"
    const val googlePlayBilling = "1.2"
    const val googleServices = "4.2.0"
    const val googleTruth = "0.44"
    const val gson = "2.8.4"
    const val hamcrest = "1.3"
    const val junit = "4.12"
    const val koin = "2.0.1"
    const val kotlin = "1.3.50"
    const val lifecycle = "2.1.0"
    const val material = "1.2.0-alpha02"
    const val mockito = "2.27.0"
    const val navigation = "2.1.0"
    const val okhttp3 = "3.10.0"
    const val retrofit = "2.4.0"
    const val robolectric = "4.3"
    const val room = "2.2.0-rc01"
    const val testCore = "1.2.1-alpha02"
    const val testCoreKtx = "1.2.0-beta01"
    const val testExtKotlinRunner = "1.1.1-beta01"
    const val testRules = "1.2.0-beta01"
    const val threetenbpGson = "1.0.2"
    const val threetenbp = "1.3.6"
}

/**
 * All libraries used across all modules, sorted alphabetically.
 *
 * Any build files should add dependencies using static accessors to libraries here.
 */
object Libs {
    // AppCompat
    val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"

    // Arch
    val archCoreTesting = "androidx.arch.core:core-testing:${Versions.lifecycle}"

    // Core
    val coreKtx = "androidx.core:core-ktx:${Versions.androidX}"

    // Constraint
    val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"

    // Crashlytics
    val crashlytics = "com.crashlytics.sdk.android:crashlytics:${Versions.crashlytics}"

    // Espresso
    val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    val espressoContrib = "androidx.test.espresso:espresso-contrib:${Versions.espresso}"
    val espressoIdlingResource = "androidx.test.espresso:espresso-idling-resource:${Versions.espresso}"
    val espressoIdlingConcurrent = "androidx.test.espresso.idling:idling-concurrent:${Versions.espresso}"
    val espressoIntents = "androidx.test.espresso:espresso-intents:${Versions.espresso}"

    // Firebase & Firestore
    val firebase = "com.google.firebase:firebase-core:${Versions.firebaseCore}"
    val firebaseAuth = "com.google.firebase:firebase-auth:${Versions.firebaseAuth}"
    val firebaseFirestore = "com.google.firebase:firebase-firestore:${Versions.firestore}"

    // Fragment
    val fragment = "androidx.fragment:fragment:${Versions.fragment}"
    val fragmentTesting = "androidx.fragment:fragment-testing:${Versions.fragment}"

    // GSON
    val gson = "com.google.code.gson:gson:${Versions.gson}"

    // Hamcrest
    val hamcrest = "org.hamcrest:hamcrest-all:${Versions.hamcrest}"

    // Unit tests
    val junit = "junit:junit:${Versions.junit}"
    val junitKtx = "androidx.test.ext:junit-ktx:${Versions.testExtKotlinRunner}"

    // Koin
    val koin = "org.koin:koin-java:${Versions.koin}"
    val koinAndroidViewModel = "org.koin:koin-android-viewmodel:${Versions.koin}"
    val koinTest = "org.koin:koin-test:${Versions.koin}"

    // Kotlin
    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    val kotlinCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"

    // AndroidX
    val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
    val lifecycleCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycle}"

    // Material
    val material = "com.google.android.material:material:${Versions.material}"

    // Mockito
    val mockito =  "org.mockito:mockito-core:${Versions.mockito}"
    val mockitoInline = "org.mockito:mockito-inline:${Versions.mockito}"

    // Navigation
    val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"

    // Google Play Billing
    val playBilling = "com.android.billingclient:billing:${Versions.googlePlayBilling}"

    // Retrofit
    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val retrofitConverterSimpleXml = "com.squareup.retrofit2:converter-simplexml:${Versions.retrofit}"
    val retrofitLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp3}"

    // Robolectric
    val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    val robolectricAnnotations = "org.robolectric:annotations:${Versions.robolectric}"

    // Room
    val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    val roomTesting = "androidx.room:room-testing:${Versions.room}"

    // ThreeTen BP - Date & Time and GSON adapter
    val threeTenBp = "org.threeten:threetenbp:${Versions.threetenbp}"
    val threeTenBpGsonAdapter = "org.aaronhe:threetenbp-gson-adapter:${Versions.threetenbpGson}"

    // AndroidX Testing
    val testCore = "androidx.test:core:${Versions.testCore}"
    val testCoreKtx = "androidx.test:core-ktx:${Versions.testCoreKtx}"
    val testRules = "androidx.test:rules:${Versions.testRules}"

    // Google Truth
    val truth = "com.google.truth:truth:${Versions.googleTruth}"

}