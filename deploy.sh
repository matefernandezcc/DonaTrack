#!/usr/bin/env bash
set -e
git pull origin dev
docker compose up -d --build
docker compose ps
