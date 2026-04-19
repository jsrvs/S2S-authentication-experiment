#!/bin/sh
set -e

OUT=/certs/out
mkdir -p "$OUT"

# ── CA ──────────────────────────────────────────────────────────────
openssl genrsa -out "$OUT/ca.key" 2048
openssl req -new -x509 -key "$OUT/ca.key" -out "$OUT/ca.crt" \
  -days 3650 -subj "/CN=s2s-experiment-ca"

# ── Per-service cert signed by CA ───────────────────────────────────
for SERVICE in gatewayservice ordercreationservice menuservice orderservice; do
  openssl genrsa -out "$OUT/$SERVICE.key" 2048

  openssl req -new -key "$OUT/$SERVICE.key" -out "$OUT/$SERVICE.csr" \
    -subj "/CN=$SERVICE"

  # Write SAN extension to temp file (sh-compatible, no process substitution)
  printf "subjectAltName=DNS:%s,DNS:localhost" "$SERVICE" > "$OUT/_ext.cnf"

  openssl x509 -req -in "$OUT/$SERVICE.csr" \
    -CA "$OUT/ca.crt" -CAkey "$OUT/ca.key" -CAcreateserial \
    -out "$OUT/$SERVICE.crt" -days 365 \
    -extfile "$OUT/_ext.cnf"

  rm -f "$OUT/$SERVICE.csr" "$OUT/_ext.cnf"
done

# ── PKCS12 keystore for gateway (Spring Cloud Gateway needs keystore format) ─
openssl pkcs12 -export \
  -in "$OUT/gatewayservice.crt" -inkey "$OUT/gatewayservice.key" \
  -out "$OUT/gatewayservice.p12" -password pass:changeit

rm -f "$OUT/ca.srl"
echo "Certificates generated in $OUT"
