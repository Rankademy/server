val querydslVersion = "5.1.0"

plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.spotbugs") version "6.1.11"
}

group = "maruhxn"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven("https://repo.springdoc.org/snapshot")
}

val mockitoAgent: Configuration = configurations.create("mockitoAgent")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // ORM
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // querydsl
    implementation("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // dev-docker
    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")

    // security
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // database
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    // test
    testCompileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // jjwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // webflux
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // File Upload
    implementation(platform("software.amazon.awssdk:bom:2.25.30"))
    implementation("software.amazon.awssdk:s3")

    // logging
    implementation("com.kdgregory.logging:logback-aws-appenders:2.4.1")

    // api-docs
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    mockitoAgent("org.mockito:mockito-core:5.18.0") { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}

spotbugs {
    excludeFilter.set(file("${projectDir}/spotbugs-exclude-filter.xml"))
}

// QueryDSL settings
val querydslDir = "src/main/generated"

sourceSets {
    main {
        java {
            srcDirs(querydslDir)
        }
    }
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory.set(file(querydslDir))
}

tasks.register("cleanQuerydsl", Delete::class) {
    delete(querydslDir)
}

tasks.named("clean") {
    dependsOn("cleanQuerydsl")
}
