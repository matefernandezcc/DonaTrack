# =========================
# DonaTrack - Makefile
# =========================

.DEFAULT_GOAL := help
SERVER_MODULE=donatrack-server

# =========================
# BUILD
# =========================

build:
	mvn clean install

package:
	mvn clean package -Dskiptest
	docker compose up --build -d

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
	mvn fmt:format
	mvn test

git-clean:
	git fetch --prune
	git branch -vv | grep 'gone' | awk '{print $1}' | xargs -r git branch -D 

PROFILE ?= local

# =========================
# RUN APP
# =========================

run:
	mvn install -pl $(SERVER_MODULE) -am && SPRING_PROFILES_ACTIVE=$(PROFILE) mvn spring-boot:run -pl $(SERVER_MODULE)

donaciones:
	mvn install -pl donatrack-donaciones -am && SPRING_PROFILES_ACTIVE=$(PROFILE) mvn spring-boot:run -pl donatrack-donaciones

incentivos:
	mvn install -pl donatrack-incentivos -am && SPRING_PROFILES_ACTIVE=$(PROFILE) mvn spring-boot:run -pl donatrack-incentivos

logistica:
	mvn install -pl donatrack-logistica -am && SPRING_PROFILES_ACTIVE=$(PROFILE) mvn spring-boot:run -pl donatrack-logistica

notificaciones:
	mvn install -pl donatrack-notificaciones -am && SPRING_PROFILES_ACTIVE=$(PROFILE) mvn spring-boot:run -pl donatrack-notificaciones

server:
	mvn install -pl donatrack-server -am && SPRING_PROFILES_ACTIVE=$(PROFILE) mvn spring-boot:run -pl donatrack-server

ngrok:
	ngrok http --url=exclude-stoplight-registrar.ngrok-free.dev 8080

run-clean:
	mvn clean install && mvn spring-boot:run -pl $(SERVER_MODULE)

# =========================
# CLEAN
# =========================

clean:
	mvn clean

deep-clean:
	find . -type d -name target -exec rm -rf {} +

# =========================
# CI STYLE (local pipeline)
# =========================

ci:
	mvn clean verify

# =========================
# N8N
# =========================

N8N_CONTAINER=n8n_donatrack

docker-up:
	docker compose up -d

docker-down:
	docker compose down

n8n-import:
	docker cp ./n8n/workflows/workflows.json \
		$(N8N_CONTAINER):/tmp/workflows.json
	docker exec $(N8N_CONTAINER) \
		n8n import:workflow --input=/tmp/workflows.json

n8n-export:
	docker exec $(N8N_CONTAINER) \
		n8n export:workflow --all --output=/tmp/workflows.json && \
	docker cp $(N8N_CONTAINER):/tmp/workflows.json ./n8n/workflows/workflows.json

setup:
	docker compose up -d
	@echo "Waiting for n8n..."
	sleep 15
	make n8n-import

# =========================
# HELP
# =========================

help:
	@echo "Commands:"
	@echo "  make build           -> build full project"
	@echo "  make test            -> run tests"
	@echo "  make format          -> format code"
	@echo "  make run             -> start server"
	@echo "  make donaciones      -> start donaciones service (port 8000)"
	@echo "  make incentivos      -> start incentivos service (port 8001)"
	@echo "  make logistica       -> start logistica service (port 8002)"
	@echo "  make notificaciones  -> start notificaciones service (port 8003)"
	@echo "  make server          -> start general server service (port 8080)"
	@echo "  make ci              -> full pipeline"
	@echo "  make setup           -> start docker and import n8n workflows"
	@echo "  make n8n-export      -> export workflows to repo"