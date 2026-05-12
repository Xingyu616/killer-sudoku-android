# Killer Sudoku

Version: 1.0.0

Killer Sudoku is a single-player Android app built with Kotlin and Jetpack Compose. It generates playable killer sudoku puzzles, supports local progress saving, and provides helper tools such as notes, hints, validation, undo, redo, and answer reveal.

## Features

- Kotlin + Jetpack Compose + Material 3
- MVVM + Repository architecture
- Hilt dependency injection
- Room local persistence for the latest game
- Random solved sudoku generation
- Difficulty-based cell removal
- Killer cage generation with sum constraints
- Row, column, box, cage, and duplicate validation
- Notes, hints, check board, erase, undo, redo, and solve actions
- Home, difficulty selection, and game screens
- Debug APK build support

## Version 1.0.0 Status

This is the first playable project version. It is ready for device testing, but there are still many areas to improve, especially puzzle quality, UI polish, accessibility, release packaging, and test coverage.

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
