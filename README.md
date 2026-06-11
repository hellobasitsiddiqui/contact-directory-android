# Contact Directory — Android

A native Android client (Kotlin + Jetpack Compose) for the
[Contact Directory](https://github.com/hellobasitsiddiqui/contact-directory) API. No server changes
required.

**Features:** login (with an editable server URL) · contacts list with **search** + pagination ·
**favourite** toggle · contact **detail** (tap-to-call / tap-to-email) · **create / edit / delete** ·
**Trash** (restore / delete forever) · **profile** + **change password** · **logout**. Sessions use the
short-lived access token with **silent refresh** on `401` (mirrors the web app).

Not included yet: photo upload/display, CSV import/export, bulk multi-select, and admin
(user-management / audit) screens.

## Two apps in this repo
- **`:app`** — the **native** Kotlin/Compose client (above) → `contact-directory.apk`.
- **`:webview`** — a thin **WebView wrapper**: the same web UI loaded in a `WebView`, with an address
  bar to point it at your server (handles the changing LAN IP) → `contact-directory-webview.apk`.
  Lowest-effort cross-surface option; reuses the web app verbatim so it's always in sync.

Both APKs are built by CI and attached to the **`latest`** release.

## UI testing (Maestro)
`maestro/` holds [Maestro](https://maestro.mobile.dev/) flows (the "Playwright for mobile") that drive
the real app and take screenshots. Run locally with `maestro test maestro/smoke.yaml`, or via the
manual **Maestro UI tests** GitHub Action (boots the backend + an emulator, runs the flow, uploads
screenshots). See [`maestro/README.md`](maestro/README.md).

## Get the APK (no Android tooling needed)
GitHub Actions builds the APK for you. After this repo is pushed:

1. Open the repo's **Actions** tab → the **Build APK** run → it publishes a **`latest`** pre-release.
2. On your phone, open the **Releases** page and download **`contact-directory.apk`**.
3. Tap it to install (allow *“install from unknown sources”* when prompted).

You can re-trigger a build anytime via **Actions → Build APK → Run workflow**.

## Run the server so the phone can reach it
The phone talks to the Spring Boot app on your laptop over Wi-Fi.

1. Start the app **bound to all interfaces** (not the loopback-only Docker stack):
   ```bash
   cd ../Fahad           # the contact-directory backend
   ./mvnw spring-boot:run
   ```
   This binds `0.0.0.0:8080`, so it's reachable on the LAN. (The `docker compose`
   stack binds `127.0.0.1` for security and is **not** LAN-reachable as-is.)
2. Find your laptop's LAN IP: `ipconfig getifaddr en0` (macOS Wi-Fi).
3. Allow the macOS firewall prompt for `java` on first launch.
4. Phone + laptop must be on the **same Wi-Fi**.

## Use the app
- **Server URL:** `http://<your-laptop-LAN-IP>:8080` (e.g. `http://10.255.198.231:8080`).
  - On the **Android emulator**, use `http://10.0.2.2:8080` (the default) — that's the emulator's alias for your laptop.
- **Username / password:** any account on the server (the seeded `admin` / `admin123`, or `alice` / `alice123`).
- On success you land on **“Welcome, <username>”**.

## Notes
- The app permits **cleartext HTTP** (`network_security_config.xml`) because the server runs over HTTP
  on your LAN. For a real/public deployment, use HTTPS and remove that.
- The access token is kept in plain `SharedPreferences` — fine for a personal demo; a production app
  would use `EncryptedSharedPreferences` and add refresh-token handling.
- The server URL is saved on-device, so you only type it once (re-enter it if your laptop's IP changes).

## Build locally instead (optional)
The Gradle **wrapper jar is not committed** (CI provisions Gradle itself), so generate it once if you
want a local/IDE build:
```bash
gradle wrapper --gradle-version 8.7   # one-off: needs Gradle installed; creates ./gradlew
./gradlew :app:assembleDebug          # APK at app/build/outputs/apk/debug/
```
Or just open the folder in **Android Studio** (it sets up Gradle + the SDK on import) and
**Build → Build Bundle(s)/APK(s) → Build APK(s)**.
