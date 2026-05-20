# =========================
# DonaTrack - Makefile
# =========================

SERVER_MODULE=donatrack-server

# =========================
# BUILD
# =========================

build:
	mvn clean install

package:
	mvn clean package

# =========================
# TEST
# =========================

test:
	mvn test

# =========================
# QUALITY / FORMAT
# =========================

format:
	mvn fmt:format

quality:
	mvn fmt:format && mvn test

# =========================
# RUN APP
# =========================

run:
	mvn spring-boot:run -pl $(SERVER_MODULE)

run-clean:
	mvn clean install && mvn spring-boot:run -pl $(SERVER_MODULE)

# =========================
# CLEAN
# =========================

clean:
	mvn clean

deep-clean:
	rm -rf target
	mvn clean

# =========================
# CI STYLE (local pipeline)
# =========================

ci:
	mvn clean install
	mvn test

# =========================
# HELP
# =========================

help:
	@echo "Commands:"
	@echo "  make build     -> build full project"
	@echo "  make test      -> run tests"
	@echo "  make format    -> format code"
	@echo "  make run       -> start server"
	@echo "  make ci        -> full pipeline"