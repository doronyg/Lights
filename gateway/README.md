# Lights gateway (`:gateway`)

Runs the web app on a NAS (Docker). A small Ktor server that:

| Path | |
|---|---|
| `/` | the web app (`:webApp` release build) |
| `GET /gateway/status` | `configured` / `unconfigured` |
| `POST /gateway/setup` | first-run setup (form `haUrl`, `haToken`): checked against HA (`GET /api/`), then stored. Only while unconfigured (409 afterwards) |
| `GET /api/states`, `POST /api/services/{light,switch}/{turn_on,turn_off}` | forwarded to HA with the stored token |
| any other `/api/...` | 403: the token is an HA admin token, so nothing else is exposed |

The browser never receives the token. It's stored in `data/ha.properties` (mode 600) on the NAS.

## Build

```zsh
./gradlew :gateway:bundle   # -> gateway/build/bundle/lights/ and gateway/build/bundle/lights.zip
./gradlew :gateway:run      # local try-out on http://localhost:8321 (data in gateway/build/data)
./gradlew :gateway:test
```

The bundle contains the Dockerfile, docker-compose.yml, the server (`lights-gateway.jar`), the web
app (`site/`), an empty `data/`, and `README.txt`. That README has the step-by-step install for
Synology Container Manager. No SSH and no Docker on your computer: the NAS builds the image.

## Install / update on Synology (summary, details in docker/README.txt)

1. File Station: upload `lights/` to `/docker/lights`.
2. Container Manager → Project → Create → path `/docker/lights`, use the existing docker-compose.yml.
3. Open `http://<nas>:8321`, enter the HA URL (the NAS LAN IP if HA runs on the NAS) and a long-lived token.

Update: upload the new folder over the old one, then Project → Build. `data/` (the HA settings) is kept.
Reset: delete `data/ha.properties` and restart the project.

## Notes

- Anyone on the LAN who can open the page can switch lights and read HA states (`/api/states`).
  Until setup is done, the first person to open the page sets it up.
- `/api/states` returns all HA entities, not only lights (HA tokens can't be limited to some entities).
- Dev server (`:webApp:wasmJsBrowserDevelopmentRun`) mimics the gateway with `HA_URL`/`HA_TOKEN` env
  vars instead (webApp/webpack.config.d/gateway.js).

