Lights - web app for your NAS (Synology Container Manager)
==========================================================

INSTALL (once)
1. File Station: upload this "lights" folder to the "docker" shared folder  ->  /docker/lights
   (or upload lights.zip there and right-click > Extract).
2. Container Manager > Project > Create
     Project name: lights
     Path: /docker/lights
     Source: "Use existing docker-compose.yml"
   Next > Done. It builds the image (a minute or two) and starts it.
3. Open http://<your-NAS>:8321 and enter the Home Assistant URL and a long-lived token
   (Home Assistant > your profile > Security > Long-lived access tokens).
   If Home Assistant runs on the NAS, use the NAS's LAN IP (e.g. http://192.168.1.10:8123),
   not localhost.

UPDATE
1. File Station: upload the new "lights" folder over /docker/lights (overwrite).
   The data folder keeps your Home Assistant settings.
2. Container Manager > Project > lights > Action > Build.

CHANGE THE HOME ASSISTANT URL / TOKEN
Delete /docker/lights/data/ha.properties, restart the project, open the page again.

PORT
Container Manager > Project > lights > edit the YAML: "8321:8321" -> "9000:8321", then Build.

LOGS
Container Manager > Container > lights > Log.

