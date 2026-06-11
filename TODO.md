# TODO / Roadmap

## High
- [ ] **Pin a stable debug signing key.** CI currently signs each build with an ephemeral debug
  keystore (regenerated per runner), so a new APK has a *different signature* and Android refuses to
  install it over the previously-installed app (signature mismatch) — you must uninstall first. Fix:
  commit a fixed `debug.keystore` (or a dedicated upload keystore via repo secrets) and add a
  `signingConfigs { debug { storeFile = ... } }` to `app/build.gradle.kts` so every build is signed
  with the same key and installs as a normal update. (Keystore secrets are non-sensitive for a debug
  key, but prefer a CI secret over committing it if going beyond personal use.)

## Features not yet in the app
- [ ] Contact **photo** display + upload (needs an image loader, e.g. Coil, using the authed OkHttp
  client; + a file/camera picker for upload).
- [ ] **CSV import / export** (file picker + share sheet).
- [ ] **Bulk** multi-select actions (delete / favourite / tag).
- [ ] **Admin** screens — user management + audit log (admin-only).
- [ ] Move tokens to **EncryptedSharedPreferences** (currently plain prefs — fine for a personal demo).
- [ ] **HTTPS**: when the server is actually deployed behind TLS, drop the cleartext
  network-security-config. (The backend is already TLS-*ready* as of v1.0.0-beta.3 — CD-027 done;
  what's pending is the real deployment, backend CD-025.)

## Nice-to-have
- [ ] Pull-to-refresh on the contacts list.
- [ ] Remember the last server URL list / quick-switch.
- [ ] Dark-theme polish (currently default MaterialTheme).
