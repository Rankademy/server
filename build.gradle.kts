val querydslVersion = "5.1.0"

plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "maruhxn"

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

    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x:4.21.0")

    mockitoAgent("org.mockito:mockito-core:5.18.0") { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    systemProperty("spring.profiles.active", "test")
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

// === 배포 스크립트 ===

apply(from = "version.gradle")
version = extra["versionCode"] as String

val versionFilePath = "version.gradle"
val versionKey = "versionCode"

val semverRegex = Regex("""(\d+)\.(\d+)\.(\d+)""")
val versionGradleRegex = Regex("""$versionKey\s*=\s*['"](\d+)\.(\d+)\.(\d+)['"]""")

// version.gradle을 읽어 versionCode를 파싱하고 (major, minor, patch)로 반환
fun readCurrentVersion(): Triple<Int, Int, Int> {
    val content = file(versionFilePath).readText()
    val match = versionGradleRegex.find(content)
        ?: error("$versionFilePath 에서 $versionKey 를 못 찾았습니다. 형식: ext { $versionKey = '1.2.3' }")
    val (maj, min, pat) = match.destructured
    return Triple(maj.toInt(), min.toInt(), pat.toInt())
}

// version.gradle의 versionCode 값을 정규식 치환으로 새 버전으로 바꿈 + 동시에 project.version도 갱신
fun writeVersionGradle(newVersion: String) {
    val vf = file(versionFilePath)
    val replaced = vf.readText().replace(versionGradleRegex, "$versionKey = '$newVersion'")
    vf.writeText(replaced)
    project.version = newVersion
    println("$versionKey updated to $newVersion")
}

// 현재 버전을 읽고, 새 버전 문자열 검증 및 생성
fun bumpVersion(transform: (major: Int, minor: Int, patch: Int) -> String) {
    val (major, minor, patch) = readCurrentVersion()
    val newVersion = transform(major, minor, patch)
    require(semverRegex.matches(newVersion)) { "잘못된 버전 형식: $newVersion (예: 1.2.3)" }
    writeVersionGradle(newVersion)
}

// 패치 버전 올리기 task
tasks.register("incrementPatchVersion") {
    group = "versioning"
    description = "패치 버전을 +1 올립니다. (x.y.(z+1))"
    doLast {
        bumpVersion { major, minor, patch -> "$major.$minor.${patch + 1}" }
    }
}

// 마이너 버전 올리기 task
tasks.register("incrementMinorVersion") {
    group = "versioning"
    description = "마이너 버전을 +1 올리고 패치를 0으로 초기화합니다. (x.(y+1).0)"
    doLast {
        bumpVersion { major, minor, _ -> "$major.${minor + 1}.0" }
    }
}
