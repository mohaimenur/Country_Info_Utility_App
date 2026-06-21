# Country Info Utility App

A simple Android utility app built with Kotlin and Jetpack Compose. It lets you pick any country and instantly see its flag, capital city, population, currency, and ISO codes — all in one card.

## Features

- **Search and select** any country from a searchable list
- **Flag, capital, population, currency, and ISO codes** shown at a glance
- **Real-time data** fetched from free public APIs using Retrofit
- **Rotation support** — selected country and data stay even when you rotate the phone
- **Multi-language settings** — change the app's display language, saved permanently with DataStore
- **Error handling** — shows a clear error message and a Retry button if the network fails

## Built With

- **Kotlin** — main programming language
- **Jetpack Compose** — modern declarative UI toolkit
- **Material Design 3** — UI components and theming
- **ViewModel + StateFlow** — manages app data and survives screen rotation
- **Retrofit + Gson** — networking and JSON parsing
- **Coroutines** — for running API calls in parallel, off the main thread
- **DataStore** — saves the selected language permanently
- **Coil** — loads flag images from the internet

## APIs Used

| API | Purpose |
|---|---|
| [countriesnow.space](https://countriesnow.space) | Country names, capitals, population, currency, ISO codes |
| [flagcdn.com](https://flagcdn.com) | Flag images (PNG, by ISO2 code) |

## App Structure

app/

├── api/            → CountryInfoApi

├── viewmodels/      → CountryViewModel, LanguageViewModel

├── screens/         → UtilityScreen, SettingsScreen

├── preferences/      → LanguagePreference (DataStore)

└── MainActivity.kt   → App entry point, bottom navigation

## How It Works

1. App opens and `CountryViewModel` fetches data from 5 different API endpoints in parallel (flags, capitals, population, currency, ISO codes).
2. The results are merged into one combined list using the country name as the key.
3. User taps the dropdown, searches and selects a country from the list.
4. Tapping **Explore** shows the selected country's full details in a card.
5. In Settings, the user can pick a language — saved using DataStore so it's remembered next time the app opens.

## Getting Started

1. Clone this repository
2. Open the project in **Android Studio**
3. Let Gradle sync and download dependencies
4. Run the app on an emulator or physical device (internet connection required)

## Limitations

- Currently uses direct API calls inside the ViewModel rather than a separate Repository layer
- Settings (besides language) are not persistent — by design, as per assignment requirements
- Free public APIs may occasionally be slow or rate-limited
