#!/bin/bash
# Live metrics monitor: polled elke 5s terwijl k6 draait en print een tabel regel per regel.
# Gebruik (Git Bash):
#   ./scripts/export-metrics.sh | tee ~/Desktop/results-mtls.txt & docker compose run --rm k6; wait

set -e

PROM="http://localhost:9090"
INTERVAL=5
DURATION=${1:-150}

# Prometheus instant query. Returns de laatste waarde als float (0 bij geen data).
q() {
    local response
    response=$(curl -sG "$PROM/api/v1/query" --data-urlencode "query=$1")
    # Extract value: JSON pattern is "value":[<timestamp>,"<number>"]
    echo "$response" | sed -nE 's/.*"value":\[[^,]*,"([^"]+)".*/\1/p' | head -1
}

Q_THRPT='sum(rate(k6_http_reqs_total[15s]))'
Q_AVG='histogram_avg(rate(k6_http_req_duration_seconds[15s])) * 1000'
Q_P95='histogram_quantile(0.95, rate(k6_http_req_duration_seconds[15s])) * 1000'
Q_P99='histogram_quantile(0.99, rate(k6_http_req_duration_seconds[15s])) * 1000'
Q_CPU='sum(process_cpu_usage{job="spring-services"}) * 100'
Q_HEAP='sum(jvm_memory_used_bytes{job="spring-services",area="heap"}) / 1024 / 1024'

printf "%-10s %12s %10s %10s %10s %12s %12s\n" \
    "time" "thrpt(rps)" "avg(ms)" "p95(ms)" "p99(ms)" "cpu_sum%" "heap_sum(MB)"
printf -- "-------------------------------------------------------------------------------\n"

END=$(($(date +%s) + DURATION))
while [ $(date +%s) -lt $END ]; do
    ts=$(date +%H:%M:%S)
    tp=$(q "$Q_THRPT")
    avg=$(q "$Q_AVG")
    p95=$(q "$Q_P95")
    p99=$(q "$Q_P99")
    cpu=$(q "$Q_CPU")
    heap=$(q "$Q_HEAP")
    # Skip rijen zonder k6 load (warm-up/cool-down window)
    tp_int=$(printf "%.0f" "${tp:-0}")
    if [ "$tp_int" -gt 0 ]; then
        printf "%-10s %12.1f %10.2f %10.2f %10.2f %12.1f %12.1f\n" \
            "$ts" "${tp:-0}" "${avg:-0}" "${p95:-0}" "${p99:-0}" "${cpu:-0}" "${heap:-0}"
    fi
    sleep $INTERVAL
done
