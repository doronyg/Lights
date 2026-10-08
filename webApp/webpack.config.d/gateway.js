// Development only: the webpack dev server plays the role of the gateway.
//
//   HA_URL=http://homeassistant.local:8123 HA_TOKEN=<long-lived token> \
//     ./gradlew :webApp:wasmJsBrowserDevelopmentRun
//
// /api/... requests from the page are forwarded to HA_URL with the token added here, on the
// developer machine. The browser never receives the token (same as with the real gateway).
// Without HA_URL there's no proxy; use http://localhost:8080/?demo instead.
if (config.devServer && process.env.HA_URL) {
    const target = process.env.HA_URL.replace(/\/+$/, '');
    const token = process.env.HA_TOKEN || '';
    config.devServer.proxy = [
        {
            context: ['/api'],
            target: target,
            changeOrigin: true,
            ws: true, // HA WebSocket API, if the app ever uses it
            onProxyReq: (proxyReq) => {
                proxyReq.removeHeader('cookie');
                proxyReq.setHeader('Authorization', 'Bearer ' + token);
            },
        },
    ];
    console.log('[gateway] proxying /api to ' + target + (token ? ' (with token)' : ' (NO HA_TOKEN set)'));
}

// The real gateway (gateway/ module) answers these; the dev server mimics them.
if (config.devServer) {
    const previous = config.devServer.setupMiddlewares;
    config.devServer.setupMiddlewares = (middlewares, devServer) => {
        devServer.app.get('/gateway/status', (req, res) =>
            res.type('text/plain').send(process.env.HA_URL ? 'configured' : 'unconfigured'));
        devServer.app.post('/gateway/setup', (req, res) =>
            res.status(501).type('text/plain').send(
                'Dev server: set HA_URL and HA_TOKEN before running Gradle (or use ?demo).'));
        return previous ? previous(middlewares, devServer) : middlewares;
    };
}

