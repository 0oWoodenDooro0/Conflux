import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            api(libs.ktor.client.websockets)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.serialization)
            api(libs.ktor.client.logging)
        }
        
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js) // wasmJs uses js engine for now
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

