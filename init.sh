#!/bin/bash

echo "🎯 Ajustando estructura a los servicios oficiales de la consigna..."

# 1. Limpiar carpetas con nombres viejos
rm -rf donatrack-colaboradores donatrack-comunicaciones donatrack-heladeras donatrack-usuarios donatrack-server target

# 2. Variables de configuración
GROUP_ID="com.donatrack"
VERSION="4.0.6"
# Nombres oficiales de la consigna
MODULOS_NEGOCIO=("donaciones" "logistica" "incentivos" "notificaciones" "auth")

# 3. Crear el POM Padre
cat <<EOF > pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>$VERSION</version>
        <relativePath/>
    </parent>
    
    <groupId>$GROUP_ID</groupId>
    <artifactId>donatrack-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>DonaTrack Parent</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <modules>
        <module>donatrack-common</module>
        <module>donatrack-donaciones</module>
        <module>donatrack-logistica</module>
        <module>donatrack-incentivos</module>
        <module>donatrack-notificaciones</module>
        <module>donatrack-auth</module>
        <module>donatrack-server</module>
    </modules>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
EOF

# Función para crear módulos oficiales
crear_modulo() {
    MOD=$1
    DIR="donatrack-$MOD"
    # El paquete ahora es com.donatrack.[nombre-consigna]
    PKG_PATH="src/main/java/com/donatrack/$MOD"
    TEST_PATH="src/test/java/com/donatrack/$MOD"

    mkdir -p "$DIR/$PKG_PATH/domain"
    mkdir -p "$DIR/$PKG_PATH/application"
    mkdir -p "$DIR/$PKG_PATH/infrastructure/api"
    mkdir -p "$DIR/$PKG_PATH/infrastructure/persistence"
    
    mkdir -p "$DIR/$TEST_PATH/domain"
    mkdir -p "$DIR/$TEST_PATH/application"
    mkdir -p "$DIR/$TEST_PATH/infrastructure/api"
    mkdir -p "$DIR/$TEST_PATH/infrastructure/persistence"

    find "$DIR" -type d -empty -exec touch {}/.gitkeep \;

    cat <<EOF > "$DIR/pom.xml"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>$GROUP_ID</groupId>
        <artifactId>donatrack-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>donatrack-$MOD</artifactId>

    <dependencies>
        <dependency>
            <groupId>$GROUP_ID</groupId>
            <artifactId>donatrack-common</artifactId>
            <version>\${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
EOF
}

# 4. Re-crear Common
mkdir -p "donatrack-common/src/main/java/com/donatrack/common/utils"
mkdir -p "donatrack-common/src/main/java/com/donatrack/common/exceptions"
cat <<EOF > "donatrack-common/pom.xml"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>$GROUP_ID</groupId>
        <artifactId>donatrack-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>donatrack-common</artifactId>
</project>
EOF

# 5. Generar módulos de la consigna
for MOD in "${MODULOS_NEGOCIO[@]}"; do
    crear_modulo "$MOD"
done

# 6. Crear Módulo Server (Bootstrap)
mkdir -p "donatrack-server/src/main/java/com/donatrack"
mkdir -p "donatrack-server/src/main/resources"
cat <<EOF > "donatrack-server/pom.xml"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>$GROUP_ID</groupId>
        <artifactId>donatrack-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>donatrack-server</artifactId>
    <dependencies>
        $(for MOD in "${MODULOS_NEGOCIO[@]}"; do echo "<dependency><groupId>$GROUP_ID</groupId><artifactId>donatrack-$MOD</artifactId><version>\${project.version}</version></dependency>"; done)
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>
    </dependencies>
    <build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin></plugins></build>
</project>
EOF

cat <<EOF > "donatrack-server/src/main/java/com/donatrack/DonatrackApplication.java"
package com.donatrack;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class DonatrackApplication {
    public static void main(String[] args) {
        SpringApplication.run(DonatrackApplication.class, args);
    }
}
EOF

touch "donatrack-server/src/main/resources/application.properties"

echo "✅ ESTRUCTURA ALINEADA CON LA CONSIGNA. ¡Dale al clean install!"