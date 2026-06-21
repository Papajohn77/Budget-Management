# Deliverable C: Cloud Deployment on Kubernetes - Budget Management System

| | |
|---|---|
| **Course** | Service Oriented Software Development in Cloud Computing |
| **Instructor** | E. Giakoumakis, V. Zafeiris |
| **Institution** | Athens University of Economics and Business |
| **Date** | June 2026 |
| **Authors** | Erika Bairami, Ioannis Papadatos, Chrysa Rizeakou |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [From Docker Compose to Kubernetes](#2-from-docker-compose-to-kubernetes)
3. [Kubernetes Manifests and the Image-Tag Problem](#3-kubernetes-manifests-and-the-image-tag-problem)
4. [The Recreate Strategy and the H2 Single-Writer Constraint](#4-the-recreate-strategy-and-the-h2-single-writer-constraint)
5. [Health Checks and Kubernetes Probes](#5-health-checks-and-kubernetes-probes)
6. [Bringing the Cluster Up and Verifying It](#6-bringing-the-cluster-up-and-verifying-it)
7. [Fault Tolerance for the Cross-Service Calls](#7-fault-tolerance-for-the-cross-service-calls)
8. [Distributed Tracing with OpenTelemetry and Jaeger](#8-distributed-tracing-with-opentelemetry-and-jaeger)

---

## 1. Introduction

...

---

## 2. From Docker Compose to Kubernetes

The migration can best be understood as a translation, where every concept from the Compose stack has a direct Kubernetes counterpart.

| Docker Compose (Deliverable B) | Kubernetes (Deliverable C) |
|---|---|
| A `service` built from the Dockerfile | A **Deployment** (manages the pod that runs the image) plus a **Service** (a stable network identity) |
| Named volume mounted at `/data` | A **PersistentVolumeClaim** mounted at `/data` |
| Compose bridge network, DNS by `service` name | Cluster networking, DNS by **Service name** |
| Only the gateway's port published to the host | Gateway exposed via a **NodePort**; the three services stayed **ClusterIP** (in-cluster only) |
| `docker compose up --build` | `kubectl apply -k` of each service's overlay |
| Single host | Single-node Minikube cluster |

Two parts of the system relied on Compose's name-based DNS, and both survived the migration to Kubernetes untouched:
- **The Rest Client base URLs:** The `budget-service` still calls the `piggybank-service`, and the `piggybank-service` still calls the `identity-service`, by the same hostnames over the same cluster DNS.
- **The gateway's `nginx.conf`:** Its upstreams point at `identity-service:8080`, `piggybank-service:8080`, and `budget-service:8080`, the `service` names from the Compose file. Because each Kubernetes **Service** is named identically and cluster DNS resolves it under that name, the gateway configuration needed no edits.

The resulting topology mirrors the Deliverable B's diagram, now expressed in Kubernetes objects: the gateway is the only externally reachable component, and everything else talks over internal **Service** DNS.

```mermaid
graph TD
    Client["Client (cURL / Postman)<br>Uses: minikube service gateway --url"]
    NodeEP["Node<br>NodeIP:nodePort"]
    GWSVC["Service<br>gateway:80"]
    GW["Pod<br>gateway:80"]

    subgraph "Minikube cluster (ClusterIP, internal DNS only)"
        IDSVC["Service<br>identity-service:8080"]
        PBSVC["Service<br>piggybank-service:8080"]
        BUSVC["Service<br>budget-service:8080"]
        ID["Pod<br>identity-service:8080"]
        PB["Pod<br>piggybank-service:8080"]
        BU["Pod<br>budget-service:8080"]
    end

    IDV[("PVC<br>identity-data")]
    PBV[("PVC<br>piggybank-data")]
    BUV[("PVC<br>budget-data")]

    Client -->|HTTP| NodeEP -->|nodePort → port| GWSVC -->|port → targetPort| GW
    GW -->|/api/v1/users| IDSVC --> ID
    GW -->|/groups · /invitations · /piggy-banks| PBSVC --> PB
    GW -->|/expenses · /incomes · /savings · /balance · ...| BUSVC --> BU

    BU -.->|Rest Client: GET /piggy-banks/totals| PBSVC
    PB -.->|Rest Client: GET /users?email| IDSVC

    ID --- IDV
    PB --- PBV
    BU --- BUV
```

---

## 3. Kubernetes Manifests and the Image-Tag Problem

### 3.1 The problem: one manifest, two environments

Every Kubernetes Deployment must name the container image its Pods will run:

```yaml
image: papajohn77/identity-service:???
```

What goes after the colon pulls in two opposite directions:

- **In Minikube (dev)**, we want whatever was just built locally, because we are iterating on the code.
- **In production**, we want an immutable, traceable tag such as `papajohn77/identity-service:sha-ec0a8bb`, so the running version is always provable, auditable, and one revert away from a rollback. This is exactly the commit-pinned tag the CI/CD pipeline already produces (§2.3 of Deliverable B).

### 3.2 Why the obvious answers don't work

**Idea 1 — Use `:latest` in the manifest, let Kubernetes always pull the newest:**
- The `:latest` tag defaults `imagePullPolicy` to `Always`, so Kubernetes pulls `:latest` from Docker Hub on every pod start. In Minikube this returns the last image CI pushed rather than the one just built locally, so the local build is silently ignored.
- Even in prod, `:latest` is mutable. Two pods scheduled at different times can run different code, and there is no way to roll back to a specific version ("which `:latest`?"). The best practice is to never deploy `:latest`.

**Idea 2 — Maintain two copies of the manifest (`k8s-dev/deployment.yml`, `k8s-prod/deployment.yml`):**
- This works, but ~90% of the two files are identical. Change a probe → update both → eventually forget one → drift between environments.
- The actual differences (image tag, maybe replicas) get buried in 60 lines of duplicated YAML, so what's environment-specific isn't visible at a glance.

**Idea 3 — Keep one manifest, hand-edit the tag with `sed` before applying:**
- The file in git no longer matches what got deployed. The committed manifest says `:placeholder`, but the cluster is running `:sha-ec0a8bb`. This is a disaster for auditing, debugging, and GitOps.

### 3.3 The solution: Kustomize

**Kustomize** is the official Kubernetes tool for this exact problem. Two properties that make it the right fit:
1. **It's built into `kubectl`**, so `kubectl apply -k <folder>` works with no extra tool to install and no templating language to learn.
2. **It only ever produces plain YAML**, there is no code generation; `kubectl kustomize <folder>` renders the final manifest so we can verify it is exactly what we expect.

#### The structure

Each service follows the same layout, a `base/` holding the manifest shape that is identical in every environment, and one thin `overlay/` per environment holding only what differs:

```
services/<service>/k8s/
  base/
    deployment.yml          # shared manifest, with no env-specific values
    kustomization.yml       # tells kustomize "here's the base"
  overlays/
    dev/
      kustomization.yml     # tells kustomize "in dev, use tag :dev"
    prod/
      kustomization.yml     # tells kustomize "in prod, use tag :sha-******"
```

#### The files

The base manifest names the image **without a tag**, so the base alone is intentionally not deployable: choosing an environment requires choosing an overlay. Each overlay starts from `base/` and supplies only the image tag, through Kustomize's canonical `images:` block. The dev and prod overlays are identical except for that one line:

```yaml
# overlays/dev/kustomization.yml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - ../../base
images:
  - name: papajohn77/identity-service
    newTag: dev
```

```yaml
# overlays/prod/kustomization.yml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
resources:
  - ../../base
images:
  - name: papajohn77/identity-service
    newTag: sha-ec0a8bb
```

#### `imagePullPolicy: IfNotPresent`

Three pull policies are possible, and `IfNotPresent` is the only one that works well in both environments:
- `Always` — always pull from the registry. This breaks Minikube, where the image was built locally and doesn't exist in the registry.
- `Never` — never pull. Too strict for production's first deploy, where the image still has to be pulled from the registry.
- `IfNotPresent` — pull only when the image isn't already cached on the node. This works for both: in Minikube the image was built straight into the cluster's Docker daemon, so it is already present and no pull is attempted; in production the image is pulled once and then cached.

This is one of the few settings identical in dev and prod, so it lives in `base/`, not in the overlays.

---

## 4. The Recreate Strategy and the H2 Single-Writer Constraint

### 4.1 The binding constraint: H2 file locking

Each service owns a single **file-backed H2 database** under its `/data` mount (§3.2 of Deliverable B). H2 in embedded/file mode acquires an **exclusive lock** when a JVM opens the database file. If a second JVM tries to open the same file, H2 refuses it with `Database may be already in use`. This is the actual binding constraint behind every storage and rollout decision in this section: a file-backed H2 database can have at most one writer.

This is why each service runs as a single replica. Two pods of the same service would mount the same `/data`, open the same H2 file, and H2 would reject the second. Horizontal scaling is therefore capped at one pod per service. Lifting that cap would mean moving to a **server-mode DBMS**, which is out of scope for this iteration.

### 4.2 Encoding the constraint: `Recreate` plus `ReadWriteOncePod`

**Storage layer: `ReadWriteOncePod`.** Each service's PersistentVolumeClaim requests the `ReadWriteOncePod` access mode, the strictest Kubernetes offers: the volume may be mounted read-write by a **single pod** across the whole cluster. This encodes the single-writer constraint in the storage layer as a hard infrastructure invariant.

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: identity-data
spec:
  accessModes:
    - ReadWriteOncePod
  resources:
    requests:
      storage: 1Gi
```

**Rollout layer: `strategy.type: Recreate`.** Kubernetes defaults a Deployment to `RollingUpdate`, which starts the new pod **before** terminating the old one, so that downtime can be avoided. With `ReadWriteOncePod` in force, that default deadlocks: while the old pod still holds the volume, the scheduler cannot place the new pod, so it stays `Pending`, with the scheduler reporting `PersistentVolumeClaim with ReadWriteOncePod access mode already in-use by another pod`. Because `RollingUpdate` will not terminate the old pod until the new one is Ready, the rollout never progresses. `Recreate` is the matching strategy because it tears the old pod down completely and releasing the volume, before bringing the new pod up.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: identity-service
spec:
  replicas: 1
  strategy:
    type: Recreate
  ...
```

> The **gateway** is the deliberate exception, it is stateless and keeps the default `RollingUpdate`.

### 4.3 Scaling out the budget service would need ShedLock

There is a second obstacle to running more than one replica, specific to the **budget-service**. It runs two in-process scheduled background jobs through the Quarkus scheduler: `RecurringExpenseScheduler` and `RecurringIncomeScheduler`.

With the replica count pinned to one, exactly one scheduler instance exists, so the jobs are safe today. But the cron is wired into the application itself, not into Kubernetes, so **every** pod of the **budget-service** would run its own copy. The moment we scale to N replicas, all N schedulers would fire the same job at the same instant, racing to apply the same recurring entries for the same date, risking double-posting them. Solving this requires **ShedLock**, which lets the schedulers share a lock (a row in a shared store) so that only one pod can acquire it and execute the job on any given time, while the others skip it.

---

## 5. Health Checks and Kubernetes Probes

Kubernetes needs three operational signals from each pod: whether it has finished booting up, whether the running container is broken and must be restarted, and whether it is ready to serve traffic. The services expose these through the **MicroProfile Health API** (implemented by the Quarkus `smallrye-health` extension), which publishes one endpoint per signal, and each Deployment consumes the per-signal endpoints through **probes**.

### 5.1 What we did not write, and why

The `smallrye-health` extension exposes three endpoints out of the box:
- `/q/health/started` - The application is started.
- `/q/health/live` - The application is up and running.
- `/q/health/ready` - The application is ready to serve requests.

Each behaves sensibly by default:
- **Startup:** The default behavior is to aggregate checks with a logical AND, and that AND over an empty set is UP, so `/q/health/started` reports UP the moment the endpoint is reachable, which is the moment Quarkus has finished booting up.
- **Liveness:** The default behavior is to aggregate checks with a logical AND, and that AND over an empty set is UP, so `/q/health/live` reports UP for as long as the process can answer the probe requests.
- **Database readiness:** The Agroal extension already contributes a readiness check named `Database connections health check`, which validates a pooled connection on every `/q/health/ready` probe.

The `/q/health/started` and `/q/health/live` endpoints never report DOWN explicitly. Failure is inferred by the fact that the process cannot answer UP at all (a timeout or a refused connection), which is exactly the signal we want from them.

### 5.2 The one check worth adding: JWT keys (readiness)

The only custom check we added checks if the JWT keys can be loaded. The **identity-service** both issues and verifies tokens, so it loads a private signing key and a public verification key (`smallrye.jwt.sign.key.location` and `mp.jwt.verify.publickey.location`), while the **piggybank-service** and **budget-service** only verify the tokens on the requests they receive, so each loads the public verification key alone (`mp.jwt.verify.publickey.location`). In every case, if the required key material cannot be loaded, the service is running yet unable to perform authentication, something the framework has no built-in check for.

The custom check surfaces as `JWT keys` in the `/q/health/ready` response, next to the Agroal `Database connections health check`. When both checks pass, the aggregate is UP and the pod stays in rotation:

```json
{
    "status": "UP",
    "checks": [
        {
            "name": "JWT keys",
            "status": "UP"
        },
        {
            "name": "Database connections health check",
            "status": "UP",
            "data": {
                "<default>": "UP"
            }
        }
    ]
}
```

However, if the key material cannot be loaded, `JWT keys` reports DOWN with the underlying error in its `data`. Readiness aggregates with a logical AND, so that single DOWN check flips the whole response to DOWN and pulls the pod from rotation, while the database check stays UP and localizes the failure:

```json
{
    "status": "DOWN",
    "checks": [
        {
            "name": "JWT keys",
            "status": "DOWN",
            "data": {
                "error": "Cannot invoke \"java.io.InputStream.read(byte[])\" because \"contentIS\" is null"
            }
        },
        {
            "name": "Database connections health check",
            "status": "UP",
            "data": {
                "<default>": "UP"
            }
        }
    ]
}
```

### 5.3 The probes and their timing

Each service's container declares all three probes. Only the **Startup** probe carries an `initialDelaySeconds`, a brief head start for the JVM before the first check. **Liveness** and **Readiness** probes begin only after the **Startup** has already reported the application up, which also shields a slow boot from being restarted before it is ready.

```yaml
startupProbe:
  httpGet:
    path: /q/health/started
    port: http
  initialDelaySeconds: 5
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 12
livenessProbe:
  httpGet:
    path: /q/health/live
    port: http
  periodSeconds: 10
  timeoutSeconds: 3
  failureThreshold: 3
readinessProbe:
  httpGet:
    path: /q/health/ready
    port: http
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 2
```

Each value follows from what a failure of that probe actually costs:

| Probe | Endpoint | Window | Rationale |
|---|---|---|---|
| startup | `/q/health/started` | ~65s to boot (5s delay + 5s × 12) | Generous against the typical 5-20s Quarkus boot. The budget covers application startup only; the image pull happens before the container starts and is not counted. |
| liveness | `/q/health/live` | restart after 30s (10s × 3) | Conservative by design. The remedy is a disruptive restart, so a single transient stall or a long GC pause must not trigger it. |
| readiness | `/q/health/ready` | out of rotation in 10s (5s × 2), back within 5s | Aggressive by design. The remedy is "stop sending traffic", which is cheap and immediately reversible, so reacting quickly is preferred. With the default `successThreshold` of 1, the pod rejoins rotation on the first success after recovery. |

---

## 6. Bringing the Cluster Up and Verifying It

### 6.1 Bring the system up

Beyond the Deliverable B prerequisites (**Java 17**, **Maven**, **Docker**), the cluster also requires **Minikube** and **kubectl**. With those in place, three commands are sufficient to bring the whole system up:

```bash
minikube start --driver=docker
./services/k8s-up.sh
minikube service gateway --url
```

The *first* command starts **Minikube**, our single-node Kubernetes cluster, using the Docker driver, so the node itself runs as a Docker container on the host rather than in a VM.

The *second* is the Kubernetes counterpart of the Deliverable B convenience script, and it runs the steps that must happen in order:
1. `mvn package` builds the three services so the Quarkus `quarkus-app` layout exists for the image build.
2. It points the shell's Docker CLI at **Minikube's own Docker daemon**, so the images are built directly inside the cluster (this is what makes `imagePullPolicy: IfNotPresent` find them with no registry involved; the alternative, `minikube image load`, instead copies a host-built image into the node).
3. It builds the three service images and the gateway image, all tagged `:dev`.
4. It applies each service's dev overlay with `kubectl apply -k`, then issues a `kubectl rollout restart` so the pods pick up the freshly built `:dev` images.

The *third* command prints the externally reachable URL of the gateway's NodePort Service. That URL is the single entry point to the system, exactly as the published gateway port was under Docker Compose.

### 6.2 Verifying with the Postman collection

Because the migration from Docker Compose to Kubernetes changed how the system is deployed and not what it does, the Deliverable B Postman collection is the acceptance test. We take the URL printed by `minikube service gateway --url` and set it as the `{{baseUrl}}` variable of the [`Budget Management.postman_collection.json`](../Budget%20Management.postman_collection.json) collection.

Executing the Postman collection against the Minikube cluster passes all the assertions. That is the concrete proof of the migration: the same client, the same requests, and the same assertions that validated the Docker Compose stack now validate the system running on Kubernetes, with the only change being the single `baseUrl` value.

---

## 7. Fault Tolerance for the Cross-Service Calls

Two calls cross a service boundary at runtime, and both are synchronous, blocking Rest Client calls (§2 diagram):
- **budget-service → piggybank-service** => `GET /piggy-banks/totals`
- **piggybank-service → identity-service** => `GET /users?email`

Communication with external systems is inherently unreliable, and that raises the resiliency demands on the application. So we must not let a remote call hang, because a blocking call left waiting pins the resources it runs on, the request's worker thread and a connection from the pool, and one slow or unreachable dependency can drain both. **Timeout** and **Circuit Breaker** (MicroProfile Fault Tolerance, via the Quarkus `smallrye-fault-tolerance` extension) make these calls **fail fast** instead.

### 7.1 The patterns we left out, and the ones we used

**Retry was deliberately not used.** Each remote call runs inside a `@Transactional` service method, so a JDBC connection from the pool is **pinned** for the whole duration of the method, including the remote call. A retry policy would stack several attempts plus back-off onto that same pinned connection, multiplying how long it is held and making **connection-pool starvation** likely even under light concurrency. Dropping retries removes that amplifier.

> Note: Both calls are read-only, so in principle we could restructure each method to perform the remote call first, outside of any transaction, and only then open a short transaction for the local work, keeping the connection out of the pool while waiting on the network. However, for our current situation, the two patterns we have applied are more than sufficient, so we note it as the clean next step if traffic grows.

**Fallback was not applicable.** Neither remote call has a sensible degraded answer:
- For the invitation, there is no way to guess the invitee's id, and without it the invitation cannot be created.
- For the balance, returning a partially correct number would be a silent, **wrong** financial figure. It is obviously better to report that the balance could not be computed than to report an inaccurate one.

**Why Timeout and Circuit Breaker.** Both reinforce **fail-fast**, which is what frees our resources (the request thread and the pinned connection) quickly rather than letting them block on a dependency that cannot answer. The circuit breaker adds one thing a timeout alone cannot: when a dependency is already failing, often because it is overloaded, it stops sending it traffic for a while instead of piling more load onto a service that is already struggling.

### 7.2 The circuit breaker, and why it sits on the service method

A circuit breaker is a state machine over a rolling window of recent outcomes:
- **Closed** (normal): calls pass through. Once the window has enough calls (`requestVolumeThreshold = 4`) and the failure ratio reaches the threshold (`failureRatio = 0.5`, half or more failing), it changes to Open.
- **Open**: calls return immediately with `CircuitBreakerOpenException` without calling the dependency, giving it room to recover. After a cool-down `delay` it moves to Half-Open.
- **Half-Open**: a single trial call is allowed through. If it succeeds the breaker closes, if it fails it opens again.

We placed `@CircuitBreaker` on the **service methods** (`getBalance`, `sendInvitation`), not on the Rest Client interfaces, and the reason was the distinction between a failing dependency and an ordinary business outcome. The Rest Client collapses every non-2xx response into a single `WebApplicationException`, so on the Rest Client side a `404` (the invitee does not exist) is indistinguishable by exception type from a `503`. If the breaker watched the Rest Client directly, those expected business responses would count as failures and could change its state. By keeping the breaker on the service method, the surrounding `try/catch` first translates the business statuses into our own exceptions, `404 → NotFoundException`, which are **not** in the breaker's `failOn` set, so they never count as failures as far as the breaker is concerned. The breaker only ever sees genuine unavailability of the remote dependency.

### 7.3 What counts as "unavailable": the three failOn types

The breaker counts exactly three exception types (`failOn = {WebApplicationException, TimeoutException, ProcessingException}`). Everything else, including the translated business exceptions and the local "not found" lookups, is treated as a normal outcome:
- **`WebApplicationException`** - once the expected business statuses have been caught and translated away, the only responses left in this type are ones a healthy service would not return, chiefly `5xx` server errors. The service answered, but with a server-side failure.
- **`TimeoutException`** (MicroProfile Fault Tolerance) - the call exceeded its time budget. Under normal conditions the response arrives well within that budget, so exceeding it likely points to a deeper issue that may let us waiting for the much longer default read timeout (30 seconds).
- **`ProcessingException`** (JAX-RS) - the call never completed at the transport layer: connection refused, host unreachable, or DNS failure. The failure is at the transport layer rather than in a response, so the service is unreachable, the clearest of the three signals that it is unavailable.

All three mean "the dependency cannot serve this call right now," which is exactly what should drive the breaker to Open. Business exceptions says nothing about the dependency's health, which is exactly why they were excluded.

### 7.4 The timeout duration

Each client method carries `@Timeout(value = 2, unit = SECONDS)` (`IdentityClient.findByEmail`, `PiggyBankClient.getTotals`). Both are trivial reads, a single indexed lookup and a small sum, whose healthy latency is in the tens of milliseconds. The timeout is therefore sized to bound how long we should wait on a **stalled** remote dependency and, because the call is inside `@Transactional`, how long the pooled connection stays pinned. Two seconds is a deliberate, generous multiple over healthy latency, large enough that a transient pause (a GC pause, a cold connection, cluster network jitter) won't reach it falsely, yet small enough to release the connection and feed the breaker within a few seconds.

### 7.5 Exercising the patterns: runtime-switchable fault simulation

To simulate the downstream dependencies misbehaving on demand, we used a textbook **Strategy Pattern**:
- A `SimulatedCondition` interface with a single `apply()` operation.
- Three strategies: `HealthyCondition` (a no-op, the service behaves normally), `SlowCondition` (sleeps for a configurable delay, default 5 seconds, to drive the caller's timeout), and `FailingCondition` (throws a `503`, to drive the caller's circuit breaker).
- A `ConditionSimulator` holds the current strategy in an `AtomicReference`, defaulting to `HealthyCondition`. The instrumented endpoint calls `simulate()`, which applies whatever strategy is currently selected, before doing its real work.

The benefit of using this pattern is that it allow us to switch strategies at **runtime**. A `PUT` endpoint under the provider's `/simulate` path takes an enum (`HEALTHY`, `SLOW`, `FAILING`) and swaps the live strategy at runtime. Changing the simulated condition requires no restart, no redeploy, and no configuration reload.

Because an endpoint that can make a service fail on command must never be reachable in production, it is guarded by a **feature flag that is closed by default**:

```properties
feature.condition-simulation.enabled=false
```

---

## 8. Distributed Tracing with OpenTelemetry and Jaeger

### 8.1 Why tracing, and the two pieces that provide it

Once a request spans across multiple services, individual service logs stop being enough. A `GET /balance` now runs partly in **budget-service** and partly in **piggybank-service**, and a `POST /invitations` runs partly in **piggybank-service** and partly in **identity-service**. Each service logs to its own stdout, so reconstructing "what happened during *this one request*, across every service it touched, and where the time went" by hand is tedious and error-prone. **Distributed tracing** solves exactly this: it stitches the per-service work of a single request into one timeline.

Two pieces provide it, and they live in different places:
- **OpenTelemetry (OTel)** is the producer side: a vendor-neutral standard (an API, an SDK, and a wire protocol called **OTLP**) for generating and exporting telemetry. Telemetry comes in three kinds, traces, metrics, and logs; we focus on **traces**. A trace is a tree of **spans**, where the **root span** is the whole request and each child span is one operation it triggered (an SQL statement, an outbound REST call, etc). Because the code is instrumented against the OTel API, the choice of backend stays a configuration detail, not a code dependency.
- **Jaeger** is the backend side. It receives spans over OTLP, stores them, and renders the searchable waterfall view shown below. We run the `jaegertracing/all-in-one` image, which bundles the OTLP collector, storage, and query UI in one process.

The two connect over OTLP: the services export spans to Jaeger, Jaeger draws the picture.

### 8.2 Instrumenting the services

Instrumentation centers on a single extension, `quarkus-opentelemetry`, added to all three services (a small `opentelemetry-jdbc` companion library is added alongside it so that database telemetry can emit SQL spans, see below). Quarkus pre-instruments the parts that matter for us out of the box: the **JAX-RS endpoints** (each inbound request becomes a span) and the **MicroProfile Rest Client** (each outbound cross-service call becomes a span and carries the trace context forward in the `traceparent` header). This automatic context propagation is why a single trace can span two services with **no code changes**: when budget-service calls piggybank-service, the child spans land in the same trace as the parent.

The rest is configuration, identical in shape across the three services:

```properties
# OpenTelemetry & distributed tracing (traces exported to Jaeger over OTLP/gRPC)
quarkus.application.name=service-name
quarkus.otel.exporter.otlp.traces.endpoint=${OTEL_EXPORTER_OTLP_TRACES_ENDPOINT:http://jaeger-collector:4317}
quarkus.datasource.jdbc.telemetry=true
quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, spanId=%X{spanId} [%c{2.}] (%t) %s%e%n
%dev.quarkus.otel.sdk.disabled=true
%test.quarkus.otel.sdk.disabled=true
```

Line by line:
- **`quarkus.application.name`** is the name the service reports in every trace (the `budget-service` / `piggybank-service` / `identity-service` labels in the screenshots below).
- **`quarkus.otel.exporter.otlp.traces.endpoint`** points at the cluster internal Jaeger Service over gRPC (port 4317), through the same `${VAR:default}` override idiom used for the Rest Client URLs (§4.1 of Deliverable B), so the same image can be repointed at a different collector using an environment variable.
- **`quarkus.datasource.jdbc.telemetry=true`** turns every SQL statement into its own span. This is what produces the `SELECT` and `INSERT` rows nested under each service in the traces, and it is what makes the two readings below possible.
- **`quarkus.log.console.format`** injects the `traceId` into each console line, so a log can be pivoted to its trace and back.
- **The two `%dev` / `%test` lines** disable the SDK where there is no collector to reach. In the cluster (the default profile) the SDK is on.

### 8.3 Deploying Jaeger, and where it sits relative to the gateway

Jaeger has two ports that matter the most for us: **4317** (OTLP ingest, where the services push spans) and **16686** (the query UI, where we can read them). The deployment runs a single replica with `COLLECTOR_OTLP_ENABLED=true` and **in-memory storage**: traces are ephemeral and lost on a Jaeger restart, which a production deployment would fix by backing Jaeger with Elasticsearch or Cassandra.

The **ingest path** (services to `jaeger-collector:4317`) is cluster internal traffic, so that Service stays **ClusterIP** forever. The **UI** is the only Jaeger part we reach, and it's deliberately **not** routed through the Nginx API Gateway. The gateway's role is to be the single front door for the *client-facing product API* (§5.1 of Deliverable B), whereas the trace UI is an *ops* tool with a different audience.

That intent is encoded with the same Kustomize base/overlay split as the services (§3). The `base` keeps **both** Jaeger Services `ClusterIP`, the secure default, so nothing is exposed unless an overlay opts in:

```yaml
# overlays/dev: expose only the UI, only in dev
patches:
  - target:
      kind: Service
      name: jaeger-query
    patch: |-
      - op: replace
        path: /spec/type
        value: NodePort
```

The **dev** overlay patches only `jaeger-query` to `NodePort`, so the UI is reachable with `minikube service jaeger-query --url`. The **prod** overlay adds no such patch, so in production the UI stays `ClusterIP` and is reached only over an authenticated tunnel (`kubectl port-forward` or a VPN), never publicly. `k8s-up.sh` applies the dev overlay alongside the services.

### 8.4 Reading the balance trace

Running the Deliverable B Postman collection (§6.2) produces traces like this one for `GET /api/v1/balance`:

![Get balance trace](images/Get-Balance-Trace.png)

The root span is budget-service's `GET /api/v1/balance`, and nested inside it is the Rest Client call out to piggybank-service's `GET /api/v1/piggy-banks/totals`, with each service's SQL statements as their own child spans. One request, two services, one timeline.

Upon looking more carefully, someone might notice something that seems weird at first: the user is read twice, on two separate connections (two `DataSource.getConnection` spans, each followed by its own user `SELECT`). This is the **`EnsureUserShadowFilter`** (§4.2 of Deliverable A) at work. The filter runs as a request filter, *before* the request reaches the resource method and *outside* any transaction, so to check whether the shadow `User` row exists it borrows its own short-lived connection for that one `SELECT`, then returns it. When the resource method then runs inside its own transactional unit of work, it borrows a *second* connection and reads the user again as part of the handler's logic. The doubled connection-and-select is the filter and the handler each doing their own bounded piece of work, not a leak.

### 8.5 Reading the invitation trace, and Hibernate's flush order

The `POST /api/v1/invitations` trace shows the second documented cross-service call, the email-to-`user_id` resolution (§4.4 of Deliverable A):

![Send invitation trace](images/Send-Invitation-Trace.png)

Again the cross-service shape is clear: piggybank-service's `POST /api/v1/invitations` is the root, and nested within it is the outbound `GET /api/v1/users` to identity-service, with identity's own `SELECT` underneath.

The instructive detail here is *ordering*. The two **`INSERT`** spans (the shadow `User` and the `Invitation`) sit at the very bottom of the trace, after every `SELECT`, even though the shadow user is created earlier in the code than some of those reads. This is **Hibernate's write-behind**: it does not send an `INSERT` to the database the moment `persist` is called, it queues the change and **flushes at transaction commit**. The trace plots SQL by its actual execution time, so every write the transaction accumulated lands together at the end, after the reads. The trace is therefore showing the database's view of the request.
