# killer-sudoku-android

Version: 1.3.0

Killer Sudoku is a single-player Android app built with Kotlin and Jetpack Compose. It generates playable killer sudoku puzzles, supports local progress saving, and provides helper tools such as notes, hints, validation, undo, redo, and answer reveal.

## Features

- Kotlin + Jetpack Compose + Material 3
- MVVM + Repository architecture
- Hilt dependency injection
- Room local persistence for the latest game
- Unique-solution puzzle generation
- Difficulty-based cell removal with solver validation
- Killer cage generation with sum constraints
- Row, column, box, cage, and duplicate validation
- Persistent notes, smart hints, check board, erase, undo, and redo actions
- Timer, pause/resume, completion history, and best-time statistics
- Coins, daily check-in, first-win bonus, and time-tier completion rewards
- Smart hint action that fills one useful cell instead of revealing the full answer
- White killer sudoku grid with dashed cage outlines and cage-combination helper panel
- Compact rules and solving-method guide available from the home screen and in-game menu
- Home, difficulty selection, and game screens
- Debug APK build support

## Version 1.3.0 Status

This release starts with a lightweight gameplay guide. The app now includes concise killer sudoku rules and starter solving methods from both the home screen and the in-game menu.

## Build

Open the project root in Android Studio and run the `app` module.

Command-line debug build:

```bash
./gradlew :app:assembleDebug
```

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```text
app/src/main/kotlin/com/example/killersudoku/
  data/         Room database, DAO, repository implementation, mappers
  domain/       Models, repository interfaces, puzzle and game use cases
  ui/           Compose app, screens, reusable UI components, theme
  viewmodel/    Game state and interaction logic
```

## Roadmap

See [ROADMAP.md](ROADMAP.md).
