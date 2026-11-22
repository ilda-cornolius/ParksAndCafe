# Parks & Cafe

One-tap **Park** / **Cafe** location saver for Android — **home-screen widget** and **Maps quick-save** — plus a web app to manage the same places. Shared backend: **Firebase Auth + Firestore** (Spark free tier).

```
ParksAndCafe/
├── android/               # Kotlin: widget + Maps accessibility → QuickSaveActivity
├── web/                   # Vite + React + TypeScript manager
├── firestore.rules
├── firestore.indexes.json
└── README.md
```

## What you get

| Client | Role |
|--------|------|
| **Android widget** | Two big home-screen buttons. Tap → GPS → save as park or cafe |
| **Maps quick-save** | When Google Maps opens, a translucent Park / Cafe sheet (debounced) |
| **Web** | Sign in, filter parks/cafes, rename, notes, delete, open in Maps |

Brand: moss green + espresso — glanceable while driving, not generic Material purple.

---

## 1. Firebase project (already wired: `parksandcafe`)

Project **parksandcafe** is configured for this repo:

- Google Sign-In enabled (Auth)
- Firestore `(default)` created; rules + indexes deployed from this repo
- Android app `com.parksandcafe.app` + Web app registered
- Local configs (gitignored): `android/app/google-services.json`, `web/.env`  
  Templates: `android/app/google-services.json.example`, `web/.env.example`

### Still required on your machine (Android Google Sign-In)

Add **your** debug keystore SHA-1 to the Android app (the cloud agent’s SHA is not yours):

```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android
```

Then: [Firebase Console → parksandcafe → Project settings → Your apps → Android](https://console.firebase.google.com/project/parksandcafe/settings/general) → add the SHA-1 fingerprint → re-download `google-services.json` into `android/app/` if prompted.

Authorized domains already include `localhost` for web Google Sign-In via CLI config.


---

## 2. Run Android

1. Open the `android/` folder in **Android Studio**.
2. Confirm `app/google-services.json` is present, then Sync Gradle.
3. Run on a device or emulator **with Google Play**.
4. Sign in with Google; allow location.
5. **Maps quick-save:** Settings → Accessibility → enable **Parks & Cafe** (or use **Open Accessibility settings** in the app). Open Google Maps → Park / Cafe sheet should appear.
6. Home screen → Widgets → **Parks & Cafe**.

More detail: [`android/README.md`](./android/README.md).

---

## 3. Run web

```bash
cd web
cp .env.example .env   # then edit with Firebase web config
npm install
npm run dev
```

Open the URL Vite prints (usually http://localhost:5173). Sign in with the same Google account used on the phone.

---

## Permissions & Play Store (read before release)

### Runtime / user-facing

| Permission / setting | Required for v1? | Purpose |
|----------------------|------------------|---------|
| **Fine / coarse location** | Yes | One-shot GPS when saving Park or Cafe |
| **Internet** | Yes | Firebase Auth + Firestore |
| **Notifications** (Android 13+) | Recommended | Short “saved” confirmation |
| **Accessibility — Parks & Cafe** | Yes for Maps quick-save | Detect only that **Google Maps** is in the foreground |
| **Display over other apps** (`SYSTEM_ALERT_WINDOW`) | **No for v1** | Reserved if you later add a persistent bubble; v1 uses a translucent activity instead |

### Play Console declarations

1. **Accessibility (sensitive / restricted)**  
   Declare the accessibility service. Justification example:  
   *“Parks & Cafe uses Accessibility solely to detect when Google Maps is open, then shows a Park/Cafe save UI. It does not read screen content, passwords, or control other apps. Scoped to package `com.google.android.apps.maps` and window-state events only.”*

2. **Privacy policy**  
   Must cover: location, Google account sign-in, Firebase, Accessibility for Maps detection. Host a public URL and link it on the listing and in-app.

3. **Data safety form**  
   Precise location; account email/ID; app functionality; data processed via Firebase/Google.

4. **Do not broaden** the accessibility service (no content reading, no extra packages) without a fresh policy review — misuse is a common rejection reason.

5. **Optional later:** if you ship `SYSTEM_ALERT_WINDOW`, declare “Display over other apps” and explain the floating bubble.

---

## Firestore schema (`places`)

| Field | Type | Notes |
|-------|------|--------|
| `lat` | number | Latitude |
| `lng` | number | Longitude |
| `type` | string | `"park"` \| `"cafe"` |
| `createdAt` | timestamp | When saved |
| `userId` | string | Firebase Auth UID |
| `name` | string? | Optional |
| `notes` | string? | Optional |

---

## Secrets

Do **not** commit:

- `android/app/google-services.json`
- `web/.env`
- Keystores

Placeholders and examples are safe to commit.

---

## Product plan

Project store: `/cursor/stores/self/docs/product-plan.md` (also mirrored in coordination docs).
