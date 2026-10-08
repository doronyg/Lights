# Web app on a NAS (Synology)

The web app runs in one Docker container on your NAS, next to Home Assistant. Any phone or computer
on your home network opens it at `http://<NAS-IP>:8321`.

The container (the **gateway**, module `:gateway`):

- serves the web app;
- forwards only the calls the app needs (read states, turn lights/switches on and off) to Home
  Assistant, adding the token; everything else is refused;
- keeps the HA URL + token **on the NAS** (`data/ha.properties`, readable by the container only).
  Browsers never receive the token.

No SSH and no Docker on your computer are needed: you upload a folder in DSM and the NAS builds it.

Requires: DSM 7.2+ with **Container Manager** installed (Package Center), and a computer with the
repository and JDK 17+.

## 1. Build the bundle (on your computer)

```zsh
./gradlew :gateway:bundle          # Windows: .\gradlew.bat :gateway:bundle
```

Output: `gateway/build/bundle/lights.zip` (about 14 MB). It contains the `lights` folder, with
`Dockerfile`, `docker-compose.yml`, `lights-gateway.jar`, `site/` (the web app), `data/` and
`README.txt`.

## 2. Create the Home Assistant token (the secret)

1. Open Home Assistant, e.g. `http://<NAS-IP>:8123`.
2. Your profile (bottom left) → **Security** → **Long-lived access tokens** → **Create token**,
   name it e.g. "Lights web".
3. Copy it: it's shown only once. Keep it until step 5, then delete your copy.

## 3. Upload to the NAS

1. Open DSM (`http://<NAS-IP>:5000`) → **File Station** → the **docker** shared folder
   (created by Container Manager).
2. **Upload** `lights.zip`, then right-click it → **Extract** → **Extract here**.
3. You now have `/docker/lights/`. The zip can be deleted.

## 4. Create the project

1. **Container Manager → Project → Create**
   - Project name: `lights`
   - Path: `/docker/lights`
   - Source: **Use existing docker-compose.yml**
2. **Next** through the wizard (skip the Web Station portal), then **Done**.
3. The NAS downloads the Java base image and builds (1-3 minutes the first time). The project then
   shows **Running**.
4. Optional: **Container → lights → Log** shows `Lights gateway on port 8321` and `Not set up yet`.

## 5. First-run setup (enter the secret)

1. Open `http://<NAS-IP>:8321` → **Enter details manually**.
2. **Home Assistant URL**: `http://<NAS-IP>:8123`. If HA runs on the same NAS, use the NAS's LAN
   IP, **not** `localhost` or `homeassistant.local` (inside the container those don't point to HA).
3. **Access Token**: paste the token.
4. **Save configuration**. The gateway tests the connection to HA, stores the details, and the
   lights appear.

Setup is accepted only once. From then on everyone just sees the lights.

## Update to a new version

1. Build the bundle again (step 1).
2. File Station: extract the new `lights.zip` into `/docker` and overwrite the files.
   `data/` keeps your HA settings.
3. **Container Manager → Project → lights → Action → Build**.

## Change the HA URL or token

1. File Station: delete `/docker/lights/data/ha.properties`.
2. **Container Manager → Project → lights → Restart**.
3. Open the page: the setup screen is back.

## Change the port

**Project → lights → edit the YAML**: change the first number of `"8321:8321"`, e.g.
`"9000:8321"`, then **Build**.

## Troubleshooting

| You see | Fix |
|---|---|
| "Home Assistant rejected the token" | The token was copied incompletely: create a new one |
| "Can't reach Home Assistant at ..." | Use `http://<NAS-IP>:8123` and check it opens in a browser |
| The page doesn't load | Project running? Check the container log. DSM firewall on? Allow port 8321 |
| "Already set up" | Setup was done before: see "Change the HA URL or token" |
| Build fails in Container Manager | Check the NAS has internet access (it downloads `eclipse-temurin:21-jre-alpine`) |

## Security notes

- Only reachable on your home network: don't forward port 8321 on your router or put it behind
  QuickConnect / a reverse proxy.
- Anyone on the network who can open the page can switch lights, and the app's state list shows
  the state of all HA entities (HA tokens can't be limited to some entities).
- Until setup is done, the first person to open the page sets it up.

Developer details (endpoints, tests): [gateway/README.md](../gateway/README.md).

