# =========================
# DonaTrack - Makefile
# =========================

.DEFAULT_GOAL := help
SERVER_MODULE=donatrack-web
MVN := $(shell command -v mvn 2>/dev/null || echo ./mvnw)

# =========================
# BUILD
# =========================

build:
	$(MVN) clean install

package:
	$(MVN) clean package -DskipTests
	docker compose --profile local up --build -d

# =========================
# TEST
# =========================

test:
	$(MVN) test

test-donaciones:
	$(MVN) test -pl donatrack-donaciones

test-incentivos:
	$(MVN) test -pl donatrack-incentivos

test-logistica:
	$(MVN) test -pl donatrack-logistica

test-notificaciones:
	$(MVN) test -pl donatrack-notificaciones

test-auth:
	$(MVN) test -pl donatrack-auth

test-web:
	$(MVN) test -pl donatrack-web

# =========================
# QUALITY / FORMAT
# =========================

format:
	$(MVN) fmt:format

quality:
	$(MVN) fmt:format
	$(MVN) test

git-clean:
	git fetch --prune
	git branch -vv | grep 'gone' | awk '{print $1}' | xargs -r git branch -D 

PROFILE ?= local

# =========================
# RUN APP
# =========================

run:
	$(MVN) install -pl $(SERVER_MODULE) -am && SPRING_PROFILES_ACTIVE=$(PROFILE) $(MVN) spring-boot:run -pl $(SERVER_MODULE)

donaciones:
	$(MVN) install -pl donatrack-donaciones -am && SPRING_PROFILES_ACTIVE=$(PROFILE) $(MVN) spring-boot:run -pl donatrack-donaciones

incentivos:
	$(MVN) install -pl donatrack-incentivos -am && SPRING_PROFILES_ACTIVE=$(PROFILE) $(MVN) spring-boot:run -pl donatrack-incentivos

logistica:
	$(MVN) install -pl donatrack-logistica -am && SPRING_PROFILES_ACTIVE=$(PROFILE) $(MVN) spring-boot:run -pl donatrack-logistica

notificaciones:
	$(MVN) install -pl donatrack-notificaciones -am && SPRING_PROFILES_ACTIVE=$(PROFILE) $(MVN) spring-boot:run -pl donatrack-notificaciones

auth:
	$(MVN) install -pl donatrack-auth -am && SPRING_PROFILES_ACTIVE=$(PROFILE) $(MVN) spring-boot:run -pl donatrack-auth

web:
	$(MVN) install -pl donatrack-web -am && SPRING_PROFILES_ACTIVE=$(PROFILE) $(MVN) spring-boot:run -pl donatrack-web

server: web

ngrok:
	ngrok http --url=exclude-stoplight-registrar.ngrok-free.dev 8080

run-clean:
	$(MVN) clean install && $(MVN) spring-boot:run -pl $(SERVER_MODULE)

# =========================
# CLEAN
# =========================

clean:
	$(MVN) clean

deep-clean:
	find . -type d -name target -exec rm -rf {} +

# =========================
# CI STYLE (local pipeline)
# =========================

ci:
	$(MVN) clean verify

# =========================
# N8N
# =========================

N8N_CONTAINER=n8n_donatrack
POSTGRES_CONTAINER=postgres_donatrack

docker-up:
	docker compose --profile local up -d

docker-down:
	docker compose --profile local down

prod-up:
	docker compose up -d --build

prod-down:
	docker compose down

prod-logs:
	docker compose logs -f --tail=100

prod-deploy:
	docker compose up -d --build
	@echo "Esperando a n8n..."
	sleep 10
	make n8n-import

db-init:
	@docker compose --profile local up -d postgres-donatrack mongo-donatrack
	@echo "Esperando que PostgreSQL esté listo..."
	@until docker exec $(POSTGRES_CONTAINER) pg_isready -U postgres > /dev/null 2>&1; do \
		echo "PostgreSQL está arrancando..."; \
		sleep 2; \
	done
	@echo "Ejecutando init.sql (creación de schemas y tablas)..."
	docker exec -i $(POSTGRES_CONTAINER) psql -U postgres -d donatrack < ./database/init.sql
	@echo "Ejecutando local-seed-data.sql (carga de datos iniciales en PostgreSQL)..."
	docker exec -i $(POSTGRES_CONTAINER) psql -U postgres -d donatrack < ./database/local-seed-data.sql
	@make mongo-init
	@echo "Bases de datos (PostgreSQL y MongoDB) inicializadas con éxito."

mongo-init:
	@echo "Ejecutando mongo-init.js (precarga de datos en MongoDB)..."
	docker exec -i mongo_donatrack mongosh -u donatrack -p donatrack --authenticationDatabase admin < ./database/mongo-init.js
	@echo "MongoDB precargado con éxito."

n8n-import:
	docker cp ./n8n/workflows/workflows.json \
		$(N8N_CONTAINER):/tmp/workflows.json
	docker exec $(N8N_CONTAINER) \
		n8n import:workflow --input=/tmp/workflows.json
	docker exec $(N8N_CONTAINER) n8n publish:workflow --id=donatrack-notificador
	docker exec $(N8N_CONTAINER) n8n publish:workflow --id=donatrack-insignias
	docker exec $(N8N_CONTAINER) n8n publish:workflow --id=donatrack-ranking
	docker exec $(N8N_CONTAINER) n8n publish:workflow --id=2zpu451nLHxitRhz
	docker exec $(N8N_CONTAINER) n8n publish:workflow --id=donatrack-notificador-real
	docker restart $(N8N_CONTAINER)

n8n-export:
	docker exec $(N8N_CONTAINER) \
		n8n export:workflow --all --output=/tmp/workflows.json && \
	docker cp $(N8N_CONTAINER):/tmp/workflows.json ./n8n/workflows/workflows.json

setup:
	docker compose --profile local up -d
	make db-init
	@echo "Esperando a n8n..."
	sleep 10
	make n8n-import

# =========================
# HELP
# =========================

help:
	@echo "Commands:"
	@echo "  make build               -> build full project"
	@echo "  make test                -> run all tests"
	@echo "  make test-donaciones     -> run donaciones tests"
	@echo "  make test-incentivos     -> run incentivos tests"
	@echo "  make test-logistica      -> run logistica tests"
	@echo "  make test-notificaciones -> run notificaciones tests"
	@echo "  make test-auth           -> run auth tests"
	@echo "  make test-web            -> run web tests"
	@echo "  make format              -> format code"
	@echo "  make run                 -> start web SSR client (port 8080)"
	@echo "  make donaciones          -> start donaciones service (port 8000)"
	@echo "  make incentivos          -> start incentivos service (port 8001)"
	@echo "  make logistica           -> start logistica service (port 8002)"
	@echo "  make notificaciones      -> start notificaciones service (port 8003)"
	@echo "  make auth                -> start auth service (port 8004)"
	@echo "  make web                 -> start web SSR client (port 8080)"
	@echo "  make ci                  -> full pipeline"
	@echo "  make docker-up           -> start docker containers (local profile)"
	@echo "  make docker-down         -> stop and remove local docker containers"
	@echo "  make db-init             -> init schemas and seed data in PostgreSQL and MongoDB"
	@echo "  make mongo-init          -> seed data in MongoDB"
	@echo "  make n8n-import          -> import and publish n8n workflows"
	@echo "  make n8n-export          -> export workflows to repo"
	@echo "  make setup               -> start docker, init databases, and import n8n workflows"