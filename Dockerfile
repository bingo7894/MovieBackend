# ===== Stage 1: Build the application =====
# ใช้ Maven เวอร์ชัน 3.9 (ตามโปรเจกต์คุณ) และ Java 17
FROM maven:3.9.12-eclipse-temurin-17 AS build

WORKDIR /build

# ก๊อปปี้ไฟล์ pom.xml และโหลด Dependencies ล่วงหน้า
COPY pom.xml .
RUN mvn dependency:go-offline

# ก๊อปปี้โค้ดทั้งหมดแล้วสั่ง Build
COPY src ./src
RUN mvn clean package -DskipTests

# ===== Stage 2: Run the application =====
# ใช้ Java 17 แบบตัวเล็กสุดๆ เพื่อรันแอปพลิเคชัน
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# ไปหยิบไฟล์ .jar ที่ Build เสร็จแล้วจาก Stage 1 มาใส่ใน Image นี้
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]