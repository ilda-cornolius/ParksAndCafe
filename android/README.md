# Parks & Cafe — Android

Kotlin app with:

1. **Home-screen widget** — Park / Cafe one-tap GPS save  
2. **Maps quick-save** — when Google Maps is foreground, show a translucent Park / Cafe sheet  

Both paths write the same Firestore `places` documents.

## Open in Cursor Desktop (Mac)

1. Clone or pull branch `cursor/scaffold-parks-and-cafe-4165` from  
   https://github.com/ilda-cornolius/ParksAndCafe  
2. In Cursor: **File → Open Folder** → the `ParksAndCafe` folder  
3. In the terminal (Cursor → Terminal):
   ```bash
   cp android/app/google-services.json.example android/app/google-services.json
   cp web/.env.example web/.env
   ```
4. You should now see `android/app/google-services.json` in the sidebar  
5. Open the **`android`** folder in **Android Studio** to Run on a device/emulator  

(`google-services.json` stays gitignored; the `.example` file is what Git ships.)

## Setup (Firebase already uses project `parksandcafe`)

1. If you skipped the copy step above:  
   `cp android/app/google-services.json.example android/app/google-services.json`
2. Web client ID is already in  
   `app/src/main/res/values/strings.xml` → `default_web_client_id_placeholder`.
3. Add **your Mac’s** debug SHA-1 for Google Sign-In if sign-in fails:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
4. Open this `android/` folder in Android Studio, sync, run on a device/emulator with Google Play.


## Enable Maps quick-save

1. Launch **Parks & Cafe** → sign in → allow location.  
2. Tap **Open Accessibility settings** → enable **Parks & Cafe**.  
3. Open **Google Maps**. After a short debounce, the quick-save sheet should appear.  
4. Use **Maps save is on / paused** in the app to stop prompts without disabling Accessibility.  
5. **Preview quick-save UI** exercises the sheet without Maps.

### How it works

- `MapsDetectAccessibilityService` — `TYPE_WINDOW_STATE_CHANGED` for `com.google.android.apps.maps` only; does **not** retrieve window content.  
- `QuickSaveActivity` — translucent activity (not a persistent overlay window).  
- `SavePlaceService` — shared GPS → Firestore path used by widget and Maps UI.

## Permissions

| Item | Notes |
|------|--------|
| Location | Requested in MainActivity; required to save |
| Accessibility | User must enable; Play-restricted — see root README |
| `SYSTEM_ALERT_WINDOW` | **Not used in v1** (commented in manifest). Activity overlay instead |

## Widget

Long-press home screen → Widgets → **Parks & Cafe**.

## Secrets

Do not commit real `google-services.json` (gitignored).
