# Web app: development

The web app (`:webApp`, Kotlin/Wasm) runs the shared UI in the browser. It never holds the HA
token: it calls `/api/...` on the server that served the page, and that server adds the token.
In development that server is the webpack dev server; in production it's the
[gateway on the NAS](nas.md).

Requires: JDK 17+. Gradle downloads Node.js and the other web tooling on the first build.
Browsers: current Chrome, Edge, Firefox or Safari (WebAssembly GC support).

## Demo (no Home Assistant)

```zsh
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Open <http://localhost:8080/?demo>. Removing `?demo` from the URL leaves demo mode.

## Against your Home Assistant

The dev server forwards `/api/...` to HA and adds the token from your shell
(`webApp/webpack.config.d/gateway.js`):

```zsh
export HA_URL=http://192.168.1.10:8123
read -rs "HA_TOKEN?HA token: "; export HA_TOKEN; echo
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Open <http://localhost:8080/>. The Gradle output shows `[gateway] proxying /api to ...`.

Windows (PowerShell):
```powershell
$env:HA_URL = "http://192.168.1.10:8123"
$env:HA_TOKEN = Read-Host "HA token"
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```

Add `--continuous` to rebuild automatically when code changes. Stop with Ctrl+C.

## Run the real gateway locally

To try exactly what runs on the NAS (including the first-run setup screen):

```zsh
./gradlew :gateway:run
```

Open <http://localhost:8321/> and enter the HA URL + token. They're stored in
`gateway/build/data/ha.properties`; delete it to see the setup screen again.

## Release build only

```zsh
./gradlew :webApp:wasmJsBrowserDistribution
# -> webApp/build/dist/wasmJs/productionExecutable/
```

These are static files, but they need a server that also provides `/api` and `/gateway`, so use
the gateway ([NAS guide](nas.md)) rather than a plain web server.

## Tips

- Settings are kept in the browser's `localStorage` (per site). `?debug` shows debug options.
- Errors are logged to the browser console (DevTools).
- Not available on the web yet: voice control, QR scanning, translation, browser back button.

