import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 20,
    duration: '2m',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

const GATEWAY_URL = __ENV.GATEWAY_URL || 'http://gatewayservice:8080';

export default function () {
    const res = http.post(
        `${GATEWAY_URL}/api/orders`,
        JSON.stringify({}),
        { headers: { 'Content-Type': 'application/json' } }
    );
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}
