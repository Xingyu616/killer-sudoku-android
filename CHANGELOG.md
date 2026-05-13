# Changelog

## 1.1.0 - 2026-05-13

Puzzle quality, persistence, and killer sudoku interaction update.

### Added

- Added a killer sudoku backtracking solver with solution counting.
- Added unique-solution validation during puzzle generation.
- Added persistent note storage with a Room database migration to version 2.
- Added cage-combination hints based on selected cage sum and cage size.
- Added per-cage combination exclusion state and per-cell number exclusion state.
- Added tests for solver behavior, unique generated puzzles, and note mapping.

### Changed

- Updated app version to `1.1.0` with `versionCode` 2.
- Redesigned the game grid to use white cells and dashed cage outlines instead of colored cage backgrounds.
- Moved cage sum labels above cage outlines with a white backing for readability.
- Reworked the bottom input panel to keep the number pad position stable.
- Changed number input so all numbers can be entered; checking the board remains responsible for marking wrong entries.
- Improved pencil-mark rendering for cells with multiple small numbers.
- Made game saves coalesce to the latest pending state to avoid stale writes after rapid input.
- Replaced answer reveal with solver-backed solving, falling back to the stored solution if needed.

### Fixed

- Fixed Hilt constructor conflicts caused by injected constructors with default parameters.
- Fixed cage outline gaps around bends by drawing cage borders above grid lines and reinforcing segment endpoints.
- Fixed combination hint state so all cells in the same cage share the same excluded combinations.

## 1.0.0 - 2026-05-12

Initial playable Android version.

### Added

- Android project setup with Kotlin, Compose, Material 3, Hilt, and Room.
- Home screen, difficulty selection screen, and game screen.
- Random killer sudoku puzzle generation.
- Local persistence for continuing the latest game.
- Move validation for sudoku and cage constraints.
- Notes, hints, check board, erase, undo, redo, and solve actions.
- Domain unit tests for puzzle generation and move validation.
- Debug APK build output.
