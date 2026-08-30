# Deploying OrbitNet to Kubernetes

Plain manifests, applied in filename order. Numeric prefixes encode the dependency order —
namespace, then config and secrets, then the backing tier, then the services, then the
Ingress.

## Prerequisites

- A cluster with a default StorageClass. No `storageClassName` is pinned anywhere, so the
  PersistentVolumeClaims use whatever the cluster provides (gp2/gp3 on EKS, standard-rwo on
  GKE, default on AKS).
- An nginx ingress controller:
  ```
  helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
    --namespace ingress-nginx --create-namespace
  ```
- The images on Docker Hub under `azam99/orbit-net-*`. Publish them with
  `mvn package -Prelease` from the repo root.

## Deploy

```bash
kubectl apply -f k8s/00-namespace.yaml

cp k8s/11-secrets.example.yaml k8s/secrets.yaml   # gitignored
$EDITOR k8s/secrets.yaml                          # fill in real values
kubectl apply -f k8s/secrets.yaml

kubectl apply -f k8s/                             # the rest, in order
```

Set the Ingress hostname before applying: `40-ingress.yaml` ships with
`orbit-net.example.com`.

## Verify

```bash
kubectl -n orbit-net get pods -w
kubectl -n orbit-net get ingress
```

Postgres, Kafka and Neo4j must reach Ready before the services settle. The services do not
declare init containers or `dependsOn` — Kubernetes has no equivalent of compose's
`depends_on: service_healthy` — so on a cold cluster they crash-loop for a minute or two
while the backing tier starts. That is expected, and the restart backoff resolves it.

## Notes on the shape of this

**discovery-service is not deployed.** Every service sets `register-with-eureka: false` and
`fetch-registry: false` under the `k8s` profile: service discovery here is Kubernetes DNS,
and Eureka would be a second, redundant registry. There is deliberately no manifest for it.

**Services listen on port 80.** The `*_SERVICE_URI` values in the `application-k8s` files
carry no port (`http://user-service`), so each Service maps 80 to the container's real
port. Changing one means changing the other.

**Kafka is a single KRaft broker** advertising its own pod DNS, which is what makes
`bootstrap-servers: kafka:9092` resolve from every pod. It is sized for one broker
(`replication factor 1`); a production cluster wants three and a StatefulSet to match.

**Probes use a separate management port (9090).** On the main port, actuator would sit
under each service's `server.servlet.context-path` — a different probe path per service —
and would be reachable through the Ingress. `management.server.port: 9090` in the `k8s`
profile puts it on its own port, off the Service, with one path for everything.

**Secrets are plain manifests.** `stringData` is base64-encoded by Kubernetes, not
encrypted. Anyone who can read the Secret can read the values. A real cluster wants
External Secrets, sealed-secrets, or the cloud provider's secret store.
