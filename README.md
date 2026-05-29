# PathTracker

Traccia i tuoi percorsi outdoor con precisione GPS.

## Funzionalità

- **Registrazione GPS** in tempo reale con smoothing e filtro jitter
- **Statistiche** — distanza, durata, velocità media durante il percorso
- **Mappa interattiva** con percorso colorato per velocità (verde → giallo → rosso)
- **Storico percorsi** con ricerca e dettaglio
- **Esportazione GPX** per condividere i tracciati
- **Semplificazione Douglas-Peucker** per risparmiare spazio di archiviazione

## Screenshot

<!-- Aggiungi qui gli screenshot dell'app -->

## Stack

- **Kotlin** + Jetpack Compose + Material 3
- **osmdroid** — mappe offline/online
- **Room** — database locale con migrazioni
- **Google Play Services Location** — tracciamento GPS ad alta precisione
- **KSP** — annotation processing per Room
- **Coroutines + Flow** — reattività e concorrenza

## Build

```bash
git clone https://github.com/tuo/path-tracker
cd PathTracker
./gradlew assembleDebug          # APK debug
./gradlew assembleRelease        # APK release (firmato)
```

## Test

```bash
./gradlew test                   # unit test
./gradlew connectedAndroidTest   # instrumented test
```

## Architettura

- **Single module** `:app`, package `com.fabiofiorini.pathtracker`
- **Navigation Compose** — 4 route: start, map, history, route detail
- **Manual DI** — `DatabaseProvider` singleton + `TrackingManager` lazy singleton
- **ViewModel** — `TrackingViewModel(Application)` con tracking state condiviso

## Licenza

MIT
