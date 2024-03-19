# Technical Assessment

Rod Bailey
Thursday 8 February 2024

# Summary

This is an Android technical exercise centered on the presentation of COVID data from an online source at `https://covid-api.com/`. The following libraries are used:

- **Jetpack Compose** for UI
- **Room** for caching of loaded data (country list only)
- **Retrofit** for network communications
- **GSON** for parsing of JSON
- **Espresso** for instrumented testing

# Build

This Github repository contains a single Android Studio project that is ready to build and install.
It has been built with `Android Studio Hedgehog | 2023.1.1 Patch 2`

# Architecture

The app has a layered architecture. The UI layer uses the MVVM pattern with the UI in `Compose` and the `ViewModel` class from the Android Architecture Components. The data layer exposes a `Repository` that fetches data either from the network or the local database.

![Architecture](/doc/app_architecture.png)

# UI Design

The UI conforms to the Material Design 3 guidelines.

# Screen

The app has only a single screen. A search field is used to filter the country list below it. The circular icon at the right of the search field provides a way to access "Global" statistics. The card at the bottom of the screen displays the covid statistics for the currently selected country (or Global). Tapping the card hides it.

The country list is cached in a local database and the only way to clear the database is to uninstall the application.

![Portrait](/doc/sample_screenshot_portrait.png)

![Landscape](/doc/sample_screenshot_landscape.png)

# Video

The following video demonstrates the application in use. You may need to download the video file to your own machine in order to watch it. It is stored within this repo at `/doc/sample_video.mp4`

![Video](/doc/sample_video.mp4)

# Tests

Currently there is a single suite of instrumented tests that perform very basic testing.

![Tests](/doc/main_instrumented_tests.png)

# Addendum

If time permitted, the following would be nice to do: 

- Extend local data caching to covid stats for individual countries
- Use a surrogate key for regions instead of the ISO 3 character code. The data has two entries for `TWN`. At the moment I silently ignore one.
- Add units of measurements for the stats to the UI
- Better UI for accessing Global stats. Current method = icon at right of search field.
- Improve tests

