# GitHub Copilot Instructions

This file provides persistent guidance for GitHub Copilot (both chat agent mode and coding agent)
when working in the `replicant1/Covid` repository.

---

## Project Summary

Android COVID-19 data app (technical exercise) built with Kotlin and Jetpack Compose.
Displays regional stats from `https://covid-api.com/`.
Single-screen app, single `:app` module.

- **Min SDK**: 24 · **Target SDK**: 34
- **Build**: Android Gradle Plugin 8.2.2 · Kotlin 1.9.25

---

## Architecture

Layered MVVM:

```
Presentation  →  Compose UI + MainViewModel
Domain        →  Region, ReportData, RegionStats models
Data          →  Repository pattern
                 ├── Network: Retrofit (CovidAPI)
                 └── Local:   Room (AppDatabase)
```

- DI: **Hilt** — modules in `core/di/` (NetworkModule, DatabaseModule, RepositoryModule, UseCaseModule)
- State: sealed `UIState` / `DataPanelUIState`, intent-driven via `processIntent()`
- Errors: delivered via `Channel` (not `StateFlow`) to avoid duplication on config change
- Region list: **hot flow** (cached in Room)
- Region stats: **cold flow** (one emission per request, network-first then cached)

---

## Key Files

| Purpose | Path |
|---|---|
| App entry point | `app/src/main/java/com/rodbailey/covid/core/app/CovidApplication.kt` |
| Single Activity | `presentation/core/MainActivity.kt` |
| ViewModel | `presentation/MainViewModel.kt` |
| Main screen | `presentation/main/MainScreen.kt` |
| Repository interface | `data/repo/CovidRepository.kt` |
| Repository impl | `data/repo/DefaultCovidRepository.kt` |
| Retrofit API | `data/net/CovidAPI.kt` |
| Room database | `data/db/AppDatabase.kt` |

---

## Build & Test Commands

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
```

---

## Coding Conventions

- **Prefer the smallest safe change** — avoid unnecessary refactoring in the same PR
- **All correctness fixes must have tests**
- **Instrumented tests** go in `app/src/androidTest/`
- **Unit tests** go in `app/src/test/`
- Keep ViewModel state mutations **intent-driven** through `processIntent()`
- Do not expose internal ViewModel methods as `public` unless required by the UI
- Use `data class` for sealed class leaves that carry value payloads
- Prefer `first()` over `collectLatest` for one-shot cold flows

---

## PR Conventions

- Branch names: `fix/`, `feature/`, `refactor/`, `chore/` prefixes
- PR title: use conventional commits style (`fix:`, `feat:`, `refactor:`, `chore:`)
- PR body: include **Summary**, **Problem**, **Fix**, and **Test plan** sections
- Keep PRs small and focused — one concern per PR

---

## Testing Infrastructure

- `FakeCovidRepository` — in-memory fake for ViewModel tests
- `FakeCovidAPI` — fake for repository integration tests
- `CoroutinesTestRule` — JUnit rule for coroutine dispatcher control
- `Turbine` — Flow assertions
- `CustomTestRunner` — Hilt integration test runner
