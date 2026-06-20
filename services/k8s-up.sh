#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

MODULES="identity-service,piggybank-service,budget-service"

mvn package --projects "$MODULES" --also-make -DskipTests

eval "$(minikube -p minikube docker-env --shell bash)"

docker build --build-arg SERVICE=identity-service  -t papajohn77/identity-service:dev  ..
docker build --build-arg SERVICE=piggybank-service -t papajohn77/piggybank-service:dev ..
docker build --build-arg SERVICE=budget-service    -t papajohn77/budget-service:dev    ..
docker build -t papajohn77/gateway:dev gateway

kubectl apply -k jaeger/k8s/overlays/dev
kubectl apply -k identity-service/k8s/overlays/dev
kubectl apply -k piggybank-service/k8s/overlays/dev
kubectl apply -k budget-service/k8s/overlays/dev
kubectl apply -k gateway/k8s/overlays/dev

kubectl rollout restart \
  deployment/identity-service \
  deployment/piggybank-service \
  deployment/budget-service \
  deployment/gateway

echo
echo "Cluster up!"
echo "Get the gateway URL with: minikube service gateway --url"
echo "View distributed traces with: minikube service jaeger-query --url"
