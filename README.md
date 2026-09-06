# Mobees

An IMDb-style Android app built with **Jetpack Compose** for browsing movies and TV series.
Every series gets a **Series Graph**: a season-by-episode ratings heatmap plus a rating
trend line, so you can see at a glance where a show peaks and where it dips.

## Features

- **Home** – trending movies, popular series and a top-rated mix in horizontal carousels.
- **Search** – instant search with All / Movies / Series filters.
- **Saved** – a local watchlist persisted with DataStore.
- **Movie details** – backdrop hero, poster, meta chips, genres, overview, cast, and a
  **franchise graph** (ratings across the collection in release order) or, for standalone
  films, a bar chart comparing the film against similar titles.
- **Series details** – the same hero, plus:
  - **Series graph heatmap** – rows are seasons, columns are episodes, cells are coloured
    red → amber → green by rating. Tap a cell to open the episode sheet.
  - **Rating trend** – every episode in broadcast order, coloured per season, with the
    series average as a dashed line. Tap a season chip to focus it.
  - **Episode sheet** – name, air date, rating, votes, season average and synopsis.
- Light and dark Material 3 themes with a content-first, generously spaced layout.

## Data: live or demo

Data comes from [TMDB](https://www.themoviedb.org/) when an API key is configured. Without
a key the app runs entirely on a bundled sample dataset (11 movies, 7 series, 400+ rated
episodes) and shows a "demo data" banner, so it works out of the box and in CI.

Provide the key in **either** of these places (checked in this order):

1. `local.properties` (git-ignored):
   ```properties
   sdk.dir=/path/to/Android/sdk
   TMDB_API_KEY=your_v3_api_key_or_v4_read_token
   ```
2. An environment variable named `TMDB_API_KEY`.

Both TMDB v3 API keys and v4 read access tokens are accepted.

## Build & run

Requirements: JDK 17+, Android SDK with platform 35 and build-tools 35.0.0.

```bash
./gradlew assembleDebug            # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest        # JVM unit tests
./gradlew lintDebug                # Android Lint
./gradlew assembleRelease          # minified release APK
```

Open the project in Android Studio (Ladybug or newer) to run on an emulator or device.

## Project structure

```
app/src/main/java/com/mobees/app/
  data/            TitleRepository + TMDB (Retrofit) and demo (assets JSON) implementations
  data/model/      Domain models: TitleSummary, MovieDetail, TvDetail, Season, Episode…
  domain/          RatingScale (colour scale) and SeriesStats (averages, best/worst)
  di/              AppContainer – picks the repository based on the API key
  ui/theme/        Material 3 colour schemes, typography, shapes
  ui/components/   Poster cards, rating pill, chips, cast row, states
  ui/charts/       SeriesHeatmap, EpisodeTrendChart, RatingLineChart, RatingBarChart
  ui/home|search|saved|detail/   Screens and ViewModels
  ui/navigation/   NavHost and bottom navigation
app/src/main/assets/demo_titles.json   Bundled sample dataset
app/src/test/                          JVM unit tests
```

## Continuous integration & releases

Two GitHub Actions workflows live in `.github/workflows/`:

| Workflow | Trigger | What it does |
| --- | --- | --- |
| `ci.yml` | pull requests, pushes to non-`main` branches | Unit tests, lint, debug APK (uploaded as an artifact) |
| `release.yml` | every push to `main` (and manual dispatch) | Unit tests, minified release APK, **GitHub Release** tagged `v<versionName>-<run number>` with the APK attached |

### Release signing

The release workflow signs with a real keystore when these repository secrets exist:

| Secret | Description |
| --- | --- |
| `KEYSTORE_BASE64` | The `.jks` keystore, base64-encoded (`base64 -w0 release.jks`) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `TMDB_API_KEY` | Optional. Bakes the TMDB key into the build so releases use live data |

If the keystore secrets are absent the release APK is signed with the debug keystore so the
pipeline always produces an installable build; the release notes state which was used.
Locally, the same variables can be set in `local.properties` (`KEYSTORE_PATH` pointing to
the `.jks` file) or as environment variables.

## Tech stack

Kotlin 2.2 · Jetpack Compose (Material 3) · Navigation Compose · Lifecycle ViewModel ·
Retrofit + OkHttp + kotlinx.serialization · Coil 3 · DataStore Preferences · JUnit 4 ·
Android Gradle Plugin 8.11 · Gradle 8.14

## License

MIT – see [LICENSE](LICENSE).
