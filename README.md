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

## 1. Create Firebase (Ilda — do this first)

1. Open [Firebase Console](https://console.firebase.google.com/) → **Add project** (e.g. `parks-and-cafe`).
2. **Authentication** → Sign-in method → enable **Google**.
3. **Firestore Database** → Create database (start in test mode if you like, then lock rules).
4. **Rules** tab → paste contents of [`firestore.rules`](./firestore.rules) → Publish.
5. **Indexes**: when the web app first queries, Firebase may prompt for a composite index on `userId` + `createdAt`. You can also deploy [`firestore.indexes.json`](./firestore.indexes.json) with the Firebase CLI.

### Add Android app

1. Project settings → **Add app** → Android.
2. Package name: `com.parksandcafe.app`.
3. Download `google-services.json` → place at `android/app/google-services.json`  
   (never commit the real file; see `.gitignore`. Shape reference: `android/app/google-services.json.example`).
4. Add your **debug SHA-1** (required for Google Sign-In):
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore \
     -alias androiddebugkey -storepass android -keypass android
   ```
5. Copy the **Web client ID** (OAuth 2.0 client, type Web) into  
   `android/app/src/main/res/values/strings.xml` → `default_web_client_id_placeholder`.

### Add Web app

1. Project settings → **Add app** → Web.
2. Copy config values into `web/.env` (from `web/.env.example`).
3. Under Authentication → Settings → **Authorized domains**, ensure `localhost` is listed for local dev.

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
