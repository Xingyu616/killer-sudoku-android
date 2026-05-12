# Roadmap

`killer-sudoku-android` is currently at version 1.0.0. The first release is playable, but the following areas should be improved in future versions.

## Puzzle Quality

- Guarantee unique solutions for generated puzzles.
- Add a stronger killer sudoku solver for difficulty grading.
- Improve cage generation to avoid overly trivial or awkward cages.
- Add a curated puzzle library for stable daily challenges.

## Gameplay

- Add timer, statistics, and completion history.
- Add mistake limits and optional relaxed mode.
- Add pencil mark auto-cleanup.
- Add smarter hints that explain reasoning steps.
- Add pause and resume UX.

## UI and UX

- Refine cage borders and labels.
- Add landscape and tablet layouts.
- Add accessibility labels and larger text support.
- Add haptic feedback and sound toggles.
- Improve dark mode contrast.

## Engineering

- Add more unit tests for solver, generator, repository, and ViewModel.
- Add Compose UI tests for main workflows.
- Add release signing configuration.
- Add CI checks for build and tests.
- Replace ad hoc grid serialization with a structured serializer if the data model grows.

## Distribution

- Prepare a signed release APK or AAB.
- Add app icon variants and store screenshots.
- Add privacy policy and release notes.
