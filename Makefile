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
	mvn fmt:format
	mvn test

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
N8N_WORKFLOW=/workflows/Chatbot\ Workflow\ Automation.json

docker-up:
	docker compose up -d

docker-down:
	docker compose down

n8n-export:
	docker exec -it $(N8N_CONTAINER) \
	n8n export:workflow \
	--all \
	--output=/tmp/workflows.json

	docker cp \
	$(N8N_CONTAINER):/tmp/workflows.json \
	./n8n/workflows/Chatbot\ Workflow\ Automation.json

n8n-import:
	docker exec -it $(N8N_CONTAINER) \
	n8n import:workflow \
	--input=$(N8N_WORKFLOW)

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
	@echo "  make build     -> build full project"
	@echo "  make test      -> run tests"
	@echo "  make format    -> format code"
	@echo "  make run       -> start server"
	@echo "  make ci        -> full pipeline"
	@echo "  make setup     -> start docker and import n8n workflows"
	@echo "  make n8n-export -> export workflows to repo"