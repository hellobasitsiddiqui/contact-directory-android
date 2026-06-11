# Maestro UI tests

[Maestro](https://maestro.mobile.dev/) is the "Playwright for mobile" — declarative YAML flows that
drive the real app and take screenshots. The same flows work on Android and iOS, so they carry over
to a future iOS build.

## Run locally
1. Install Maestro: `curl -Ls https://get.maestro.mobile.dev | bash`
2. Start the **backend** (reachable from the emulator at `10.0.2.2`):
   `cd ../Fahad && ./mvnw spring-boot:run`
3. Start an **emulator** and install the app APK (`adb install …/app-debug.apk`).
4. Run a flow (screenshots are written to the working directory):
   ```bash
   maestro test maestro/smoke.yaml
   ```
   Or explore interactively: `maestro studio`.

## In CI
`.github/workflows/maestro.yml` (manual `workflow_dispatch`) boots the backend, starts an emulator,
installs the APK, runs `smoke.yaml`, and uploads the screenshots as a `maestro-screenshots` artifact —
mirroring the web Playwright e2e pipeline. The emulator + backend orchestration is the fiddly part; if
selectors or timing need tweaking on the first real run, adjust `smoke.yaml` (text matchers) or the
workflow waits.

## Notes
- Selectors match **visible text** (e.g. "Sign in", "Username", "Contacts"). If a label changes, update
  the matcher. For rock-solid selectors, add Compose `Modifier.testTag(...)` and match by id.
- The login screen pre-fills the Server URL to `http://10.0.2.2:8080`, so the flow doesn't type it.
