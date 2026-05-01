# 1. Crear la carpeta de tests en el módulo server
mkdir -p donatrack-server/src/test/java/com/donatrack

# 2. Crear el archivo de test de carga de contexto
cat <<EOF > "donatrack-server/src/test/java/com/donatrack/DonatrackApplicationTests.java"
package com.donatrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DonatrackApplicationTests {

	@Test
	void contextLoads() {
        // Este test verifica que el servidor de DonaTrack arranque sin errores
	}

}
EOF