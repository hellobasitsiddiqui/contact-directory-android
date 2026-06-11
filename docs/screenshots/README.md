# Screenshots

Captured automatically on a CI emulator by the [Maestro](../../maestro) flows
(`maestro/smoke.yaml` and `maestro/webview-smoke.yaml`) — no manual capture.

## Native app (`:app` → `contact-directory.apk`)

| Login | Contacts | Search |
|---|---|---|
| ![Login](native-01-login.png) | ![Contacts](native-02-contacts.png) | ![Search](native-03-search.png) |

| Profile | New contact |
|---|---|
| ![Profile](native-04-profile.png) | ![New contact](native-05-new-contact.png) |

## WebView wrapper (`:webview` → `contact-directory-webview.apk`)

The thin native shell with an address bar, loading the web UI verbatim.

| Address bar | Web login loaded in the WebView |
|---|---|
| ![Address bar](webview-01-address-bar.png) | ![Web login](webview-02-web-login.png) |
