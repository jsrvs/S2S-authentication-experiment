# S2S Authentication Experiment

Onderzoeksexperiment dat de runtime-overhead van vier service-to-service (S2S) authenticatiemechanismen vergelijkt op een identieke Spring Boot microservice-keten:

| Branch      | Mechanisme                              |
|-------------|-----------------------------------------|
| `main`      | Baseline (geen authenticatie)           |
| `api-keys`  | Statische gedeelde API key (`X-API-Key`)|
| `oauth2`    | OAuth2 client credentials (JWT)         |
| `mtls`      | Mutual TLS (self-signed CA)             |

Elke branch bevat een volledige, draaibare stack (4 services + Prometheus + Grafana + k6). De meetinfrastructuur is identiek tussen branches, zodat verschillen in resultaten toe te schrijven zijn aan het authenticatiemechanisme.

## Vereisten

- Docker Desktop (met Docker Compose)
- Git Bash (Windows) of een andere bash-shell met `curl`
- Vrije poorten: `3000` (Grafana), `8080` (gateway), `9090` (Prometheus)

## Reproduceerbaar uitvoeren

### 1. Repository klonen

```bash
git clone https://github.com/jsrvs/S2S-authentication-experiment.git
cd S2S-authentication-experiment
```

### 2. Branch kiezen

```bash
git checkout main          # of: api-keys, oauth2, mtls
```

### 3. (Alleen `mtls`) Certificaten genereren

```bash
docker compose run --rm certgen
```

Dit genereert eenmalig een self-signed CA en service-certificaten in `certs/out/`.

### 4. Stack starten

```bash
docker compose build 
docker compose up -d
```

Wacht ~30 seconden tot alle services up zijn. Verifieer:
- Prometheus targets: <http://localhost:9090/targets> (alle targets `UP`)
- Grafana dashboard: <http://localhost:3000>
- Quick-test of de gateway werkt:
  ```bash
  curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d "{}"
  ```

### 5. Meting uitvoeren

Per branch wordt één warm-up run gedraaid (deze resultaten worden verworpen) gevolgd door drie meetruns. Per run:

```bash
mkdir -p results
RUN_NAME=baseline-run1   # pas aan per branch en run-nummer

./scripts/export-metrics.sh > "results/${RUN_NAME}.txt" &
docker compose run --rm k6 run --quiet /scripts/script.js > "results/${RUN_NAME}.log" 2>&1
wait
```

- `export-metrics.sh` poll Prometheus elke 5s en schrijft een tabel met throughput, latency-percentielen, CPU en heap.
- De k6-summary verschijnt in het `.log`-bestand.
- Wacht minimaal 30s tussen runs zodat de JVM-metrics terug naar idle zijn.

### 6. Stack stoppen

```bash
docker compose down -v
```

Tussen branches: `down -v` → `git checkout <branch>` → opnieuw vanaf stap 3 (mTLS) of stap 4.

## k6-load

- 20 virtual users, 2 minuten, closed-loop
- POST `/api/orders` met lege JSON body

Aanpasbaar in [k6/script.js](k6/script.js).

## Output

Per run worden twee bestanden geproduceerd in `results/`:
- `<run>.txt` — tijdreeks van Prometheus-metrics (throughput, avg/p95/p99 latency, CPU, heap)
- `<run>.log` — k6-summary met aggregaten over de hele run
