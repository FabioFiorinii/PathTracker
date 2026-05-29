# PathTracker — AGENTS.md

## Commands

```powershell
./gradlew assembleDebug                          # build debug APK
./gradlew test                                   # unit tests
./gradlew connectedAndroidTest                   # instrumented tests on emulator/device
./gradlew lint                                   # lint
./gradlew :app:kspKotlinDebug                    # force Room/KSP codegen (run after DAO/entity changes)
```

## Architecture

- **Single module** `:app`, package `com.fabiofiorini.pathtracker`
- **Navigation Compose**: 4 routes — `"start"` (StartScreen), `"map"` (MapScreen), `"history"` (HistoryScreen), `"routeMap/{routeId}"` (RouteMapScreen)
- **State**: `TrackingManager` singleton (`tracking/`) holds mutable Compose state (`points`, `elapsedSeconds`, etc.). This is the global tracking state holder used directly by ViewModel and MapScreen.
- **DI**: No framework. `DatabaseProvider` is a manual singleton for Room.
- **ViewModel**: `TrackingViewModel(Application)` (AndroidViewModel), not `ViewModelProvider.Factory` based — use `viewModel()` directly in composables.
- **Maps**: osmdroid (not Google Maps). Must call `Configuration.getInstance().load()` before any MapView. Map screens set `userAgentValue` from package name.
- **Database**: Room v2, tables `routes` and `route_points`, version 2, `exportSchema=false`. Run `kspKotlinDebug` if DAO/entity changes don't compile.
- **Background GPS**: `TrackingService` is a foreground service with `foregroundServiceType="location"`. Started via `startForegroundService()`. Requests location every 2s (HIGH_ACCURACY). GPS smoothing: rolling average of 5 samples, 3m jitter filter.

## Key conventions

- UI strings are in Italian (e.g. "Registra nuovo percorso", "Storico percorsi")
- No `exportSchema=true` — Room schema JSON is not tracked in git
- `fallbackToDestructiveMigration(false)` — Room migration must be explicit
- ProGuard/minification enabled on release builds (see `proguard-rules.pro`)
- ABI split: only `arm64-v8a` is built; universal APK disabled
- Tests are sparse: 1 example unit test, 1 example instrumented test
- Compose BOM manages all `androidx.compose.*` versions; `material-icons-extended` is added separately
