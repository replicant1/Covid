# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android COVID-19 data app (technical exercise) that displays regional stats from `https://covid-api.com/`. Single-screen app built with Kotlin and Jetpack Compose.

- **Min SDK**: 24, **Target SDK**: 34
- **Build**: Android Gradle Plugin 8.2.2, Kotlin 1.9.25

## Build & Run Commands

All Gradle commands from the project root (`~/StudioProjects/Covid`):

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (JVM)
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single instrumented test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.rodbailey.covid.presentation.main.MainViewModelTest

# Lint
./gradlew lint
```

## Architecture

Layered MVVM with a single `:app` module:

```
Presentation  →  Compose UI + MainViewModel
Domain        →  Region, ReportData, RegionStats models
Data          →  Repository pattern
                 ├── Network: Retrofit (CovidAPI)
                 └── Local:   Room (AppDatabase)
```

**Key data flow**: `MainViewModel` consumes `Flow`s from `CovidRepository`. The region list is a **hot flow** (cached in Room, persists across sessions). Region stats are a **cold flow** (one emission per request, cache-first: reads from Room; if empty, fetches from network then caches).

**DI**: Hilt throughout. Modules live in `core/di/` — `NetworkModule`, `DatabaseModule`, `RepositoryModule`, `UseCaseModule`.

**State**: `MainViewModel` uses sealed `UIState`/`DataPanelUIState` and intent-driven updates. Errors are delivered via `Channel` (not `StateFlow`) to prevent duplication on config change.

## Key Files

| Purpose              | Path                                                                        |
|----------------------|-----------------------------------------------------------------------------|
| App entry point      | `app/src/main/java/com/rodbailey/covid/core/app/CovidApplication.kt`        |
| Single Activity      | `app/src/main/java/com/rodbailey/covid/presentation/core/MainActivity.kt`   |
| ViewModel            | `app/src/main/java/com/rodbailey/covid/presentation/MainViewModel.kt`       |
| Main screen          | `app/src/main/java/com/rodbailey/covid/presentation/main/MainScreen.kt`     |
| Repository interface | `app/src/main/java/com/rodbailey/covid/data/repo/CovidRepository.kt`        |
| Repository impl      | `app/src/main/java/com/rodbailey/covid/data/repo/DefaultCovidRepository.kt` |
| Retrofit API         | `app/src/main/java/com/rodbailey/covid/data/net/CovidAPI.kt`                |
| Room database        | `app/src/main/java/com/rodbailey/covid/data/db/AppDatabase.kt`              |

## Testing

**Instrumented tests** (`app/src/androidTest/`) — require a device/emulator:
- `MainViewModelTest` — ViewModel with `FakeCovidRepository`
- `MainUITest` — Espresso UI tests
- `RegionDbTest`, `RegionStatsDbTest` — Room DAO tests
- `RepositoryTest` — Repository integration tests

**Unit tests** (`app/src/test/`):
- `TransformUtilsTest` — Utility functions

**Test infrastructure**: `FakeCovidAPI`, `FakeCovidRepository`, `CoroutinesTestRule` (JUnit rule for coroutine dispatchers), `Turbine` (Flow assertions), `CustomTestRunner` (Hilt integration).

## API

Two endpoints via `CovidAPI` (Retrofit):
- `GET /api/regions` — all regions
- `GET /api/reports/total?iso={code}` — stats for a region (`code = null` → global stats)

`RegionCode` is an ISO3 code value type; `GlobalCode` is the subclass used for global stats.