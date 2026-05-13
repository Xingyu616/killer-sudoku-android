# Roadmap

`killer-sudoku-android` is currently at version 1.1.0. The app is playable and now includes unique-solution generation checks, saved notes, and a clearer killer sudoku grid. The following areas should be improved in future versions.

## Puzzle Quality

- Add stronger solver-based difficulty grading.
- Improve cage generation to avoid overly trivial or awkward cages.
- Add a curated puzzle library for stable daily challenges.

## Gameplay

- Add timer, statistics, and completion history.
- Add mistake limits and optional relaxed mode.
- Add pencil mark auto-cleanup.
- Add smarter hints that explain reasoning steps.
- Add pause and resume UX.

## UI and UX

- Further refine cage borders and labels across screen densities.
- Add landscape and tablet layouts.
- Add accessibility labels and larger text support.
- Add haptic feedback and sound toggles.
- Improve dark mode contrast.

## Engineering

- Add more unit tests for repository and ViewModel behavior.
- Add Compose UI tests for main workflows.
- Add release signing configuration.
- Add CI checks for build and tests.
- Replace ad hoc grid serialization with a structured serializer if the data model grows.

## Distribution

- Prepare a signed release APK or AAB.
- Add app icon variants and store screenshots.
- Add privacy policy and release notes.
