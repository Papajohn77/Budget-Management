#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

MODULES="identity-service,piggybank-service,budget-service"

mvn package --projects "$MODULES" --also-make -DskipTests
docker compose up --build
