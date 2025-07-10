plugins {
	java
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.4"
	id("io.freefair.lombok") version "8.6"
}

group = "vn.hoidanit"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
	options.compilerArgs.add("-parameters")
}

repositories {
	mavenCentral()
}

dependencies {
    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    // Thymeleaf extras
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j")

    // Flyway database migration
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // ModelMapper
    implementation("org.modelmapper:modelmapper:3.1.1")

    // File storage with Cloudinary
    implementation("com.cloudinary:cloudinary-http44:1.37.0")
    implementation("commons-io:commons-io:2.11.0")

    // LangChain4j - AI integration
    implementation("dev.langchain4j:langchain4j-spring-boot-starter:1.0.0-alpha1")
    implementation("dev.langchain4j:langchain4j-open-ai-spring-boot-starter:1.0.0-alpha1")

    // Development tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}