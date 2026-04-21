# Wordle

Android group project for TDT4240. This repository contains a Wordle-inspired app built with Kotlin, Jetpack Compose, and Firebase-backed services for authentication and shared data.

## Project Structure

The repository is organized as a single Android application module with its source code, tests, and build configuration:

- `app/` is the main Android application module.
- `app/build.gradle.kts` defines the app module's Android configuration, Compose setup, Firebase integration, SDK levels, and dependencies.
- `app/google-services.json` contains the Firebase Android app configuration used by the Google Services Gradle plugin.
- `app/src/main/` contains the production application code and Android resources.
- `app/src/main/AndroidManifest.xml` declares the app entry point, permissions, and application metadata.
- `app/src/main/java/com/example/wordle/` is the Kotlin source root for the app.
- `app/src/main/java/com/example/wordle/MainActivity.kt` is the application entry point and composition root that wires screens, repositories, and view models together.
- `app/src/main/java/com/example/wordle/data/` contains the app's data-access layer:
  - `auth/` contains authentication abstractions and the Firebase-backed repository implementation.
  - `daily/` contains daily-play persistence logic, used to track whether the daily game has already been played.
  - `leaderboard/` contains leaderboard models and repository code for reading ranked player data.
  - `stats/` contains user statistics models, local storage, and remote synchronization helpers.
  - `WordProvider.kt` contains word loading and validation logic used by the gameplay flow.
- `app/src/main/java/com/example/wordle/ui/` contains the app's Compose UI layer:
  - `auth/` contains the authentication flow screens, UI state, and view model.
  - `game/` contains the core gameplay implementation. This package includes the game screen (`GameScreen`), the game state model (`GameUiState`), configuration for different game modes (`GameConfig`), the main gameplay controller (`GameViewModel` and `GameViewModelFactory`), and helper logic for evaluating guesses and updating keyboard state (`WordEvaluator` and `KeyboardStateReducer`).
  - `leaderboard/` contains the leaderboard screen and its view model.
  - `menu/` contains the main menu, custom game setup flow, and how-to-play dialog.
  - `settings/` contains the settings screen and state management for user preferences.
  - `stats/` contains the statistics dialog shown from gameplay-related flows.
  - `theme/` contains Compose theme definitions such as colors, typography, and Material theme setup.
  - `user/` contains the user profile screen and profile-related view model logic.
  - `Keyboard.kt` contains the reusable on-screen keyboard UI used during gameplay.
- `app/src/main/res/` contains Android resources such as strings, icons, drawables, launcher assets, themes, and XML configuration files.
- `app/src/test/` contains local JVM unit tests. These currently focus on isolated gameplay logic such as word evaluation and keyboard state reduction.
- `app/src/androidTest/` contains instrumented Android tests that run on a device or emulator.
- `gradle/` contains Gradle version catalog and wrapper files used to build the project consistently across machines.
- `build.gradle.kts` is the top-level Gradle build configuration for shared plugin setup.
- `settings.gradle.kts` declares the Gradle project structure and repositories.
- `gradlew` and `gradlew.bat` are the Gradle wrapper scripts for Unix-like systems and Windows.
- `gradle.properties` contains project-wide Gradle settings.
- `firestore.rules` contains Firestore security rules for the Firebase backend.
- `local.properties` stores local Android SDK path information for a developer machine and is not typically shared as team documentation.

## High-Level Architecture

The app follows a simple feature-oriented Android architecture centered around a single app module. At a high level, the project is split into an application entry point, a UI layer, and a data layer.

- `MainActivity` acts as the application entry point and composition root. It is responsible for starting the app and connecting the main screens, repositories, and state holders used throughout the UI.
- The `ui` layer contains the Compose-based presentation logic. This includes screens, dialogs, reusable UI elements, view models, and theme definitions.
- The `data` layer contains repositories, providers, and persistence helpers that supply words, manage authentication, track daily play restrictions, load and store statistics, and communicate with Firebase-backed services.

The UI layer depends on the data layer rather than accessing platform or Firebase APIs directly. This keeps feature logic grouped around screens and view models, while data retrieval and persistence stay inside dedicated repository or provider classes.

The most central feature area is the `game` package, which implements the playable Wordle flow. It coordinates:

- game setup for daily, random, and custom modes
- creation and updating of the board state shown on screen
- keyboard input handling and on-screen keyboard feedback
- guess validation and evaluation
- hard-mode rule enforcement
- end-of-game handling, including statistics updates and daily-play tracking

Other UI feature packages support the rest of the application experience:

- `auth` handles login and signup flows
- `menu` handles the main menu, how-to-play dialog, and custom game setup
- `leaderboard` handles leaderboard presentation and retrieval
- `settings` handles user-configurable preferences
- `user` handles profile-related actions
- `stats` contains statistics presentation components

On the data side, the app combines local and remote storage responsibilities:

- local persistence is used for things like daily play status and locally stored player statistics
- Firebase services support authentication, shared data access, remote stats synchronization, and leaderboard-related features
- word retrieval and validation are isolated in provider-style classes so gameplay code can request words without owning the backend details

This structure keeps the codebase organized by responsibility: UI packages define what the user can see and do, while data packages define how the app loads, stores, validates, and synchronizes information.

## Firebase Setup

This project uses Firebase services. The separate `google-services.json` file is only needed if the project is being built from source.

To compile the project source code with Firebase enabled:

1. Obtain the `google-services.json` file from the separate project delivery material.
2. Place the file at:

```text
app/google-services.json
```

The file is delivered separately from the repository for submission purposes. The app will not build correctly with Firebase features enabled unless `google-services.json` is present in that location before compiling the project.

## Build From Source

### Prerequisites

This is the primary way to set up and run the project. Before building from source, make sure you have:

- Android Studio installed
- JDK 11 available on your machine
- An Android SDK installed that supports the project's current compile and target SDK setup
- The separately delivered `google-services.json` file placed at `app/google-services.json`
- An Android emulator or a physical Android device if you want to run the app after building

### Android Studio

1. Open the repository root in Android Studio.
2. Let Android Studio sync the Gradle project.
3. Select an emulator or connected Android device.
4. Run the `app` configuration.

### Command Line

Build a debug APK from the project root:

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
./gradlew assembleDebug
```

## Optional APK Installation

A prebuilt APK may also be provided with the project delivery material.

### On an Android Phone

1. Transfer the provided APK file to the phone.
2. Open the APK file on the device.
3. Allow installation from the chosen source if Android asks for permission.
4. Complete the installation and open the app from the launcher.

### On an Android Emulator

1. Start an Android emulator from Android Studio or another Android emulator setup.
2. Drag and drop the APK file into the running emulator, or install it using Android tooling such as `adb install`.
3. Open the app from the emulator's app list after installation.

## Testing

Run local unit tests:

On Windows:

```powershell
.\gradlew.bat test
```

On macOS or Linux:

```bash
./gradlew test
```

Run instrumented Android tests with a connected device or running emulator:

On Windows:

```powershell
.\gradlew.bat connectedAndroidTest
```

On macOS or Linux:

```bash
./gradlew connectedAndroidTest
```
