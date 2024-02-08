# Technical Assessment

Rod Bailey
Thursday 8 February 2024

# Summary

This is an Android technical exercise centered on the presentation of COVID data from an online source at `https://covid-api.com/`. The following libraries are used:

- **Jetpack Compose** for UI
- **Room** for caching of loaded data
- **Retrofit** for network
- **GSON** for parsing of JSON

# Screen

The app has only a single screen. A search field is used to filter the country list below it. The circular icon at the right of the search field provides a way to access "Global" statistics. The card at the bottom of the screen displays the covid statistics for the currently selected country (or Global). Tapping the card hides it.

The country list is cached in a local database and the only way to clear the database is to uninstall the application.

![Screen](/doc/sample_screenshot.png)

# Video

The following video demonstrates the application in use. You may need to download the video file to your own machine in order to watch it. It is stored within this repo at `/doc/sample_video.mp4`

![Video](/doc/sample_video.mp4)

