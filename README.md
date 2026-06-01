# PathTracker

Traccia i tuoi percorsi outdoor con precisione GPS.

## Funzionalità

- **Registrazione GPS** in tempo reale (2s, alta precisione) con smoothing e filtro jitter
- **Contapassi** tramite sensore `TYPE_STEP_COUNTER` — delta rilevato ogni 2s, nessuna dipendenza esterna
- **Statistiche** — distanza, durata, velocità media, passi durante il percorso
- **Mappa interattiva** con percorso colorato per velocità (verde → giallo → rosso) e legenda
- **Marker personalizzati** per posizione corrente, inizio e fine percorso
- **Storico percorsi** con ricerca e dettaglio
- **Finestra informativa** con `ModalBottomSheet`: header rosso, griglia di statistiche (passi, calorie stimate, tempo in movimento, andatura)
- **Esportazione GPX** per condividere i tracciati
- **Semplificazione Douglas-Peucker** (tolleranza 5 m) per risparmiare spazio

## Esportazione GPX

I file GPX vengono salvati nella cartella `Downloads/PathTracker/` (Android 10+) oppure nella directory privata dell'app su versioni precedenti.

## Stack

- **Kotlin** + Jetpack Compose + Material 3
- **osmdroid** — mappe offline/online
- **Room** — database locale v4 (`fallbackToDestructiveMigration()`)
- **Google Play Services Location** — tracciamento GPS ad alta precisione
- **KSP** — annotation processing per Room
- **Coroutines + Flow** — reattività e concorrenza

## Build & Test

```bash
# build debug APK
./gradlew assembleDebug

# unit test
./gradlew test

# instrumented test (emulator/dispositivo)
./gradlew connectedAndroidTest

# lint
./gradlew lint

# rigenera Room/KSP dopo modifiche a DAO/entity
./gradlew :app:kspKotlinDebug
```

Per tutti i comandi di sviluppo, vedi [AGENTS.md](AGENTS.md).

## Screenshot

Work in progress

## Licenza

MIT — vedi [LICENSE](LICENSE).

## Architettura

- **Single module** `:app`, package `com.fabiofiorini.pathtracker`
- **Navigation Compose** — 4 route: start, map, history, route detail
- **Manual DI** — `DatabaseProvider` singleton + `TrackingManager` singleton
- **ViewModel** — `TrackingViewModel(Application)` con tracking state condiviso
- **TrackingService** — foreground service con `foregroundServiceType="location"`, registra sensore `TYPE_STEP_COUNTER` per il conteggio passi
- **Permessi runtime** — `ACCESS_FINE_LOCATION` e `ACTIVITY_RECOGNITION` richiesti via `RequestMultiplePermissions`
