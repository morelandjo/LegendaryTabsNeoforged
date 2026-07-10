# Mod Tabs

A Minecraft mod that adds a tab menu to the inventory screen for quick navigation between different mod interfaces.

Originally developed by Sfiomn as "Legendary Tabs", this mod is maintained across several Minecraft versions and loaders from one Stonecutter workspace.

## Supported targets

| Stonecutter project | Minecraft | Loader | Java |
|---|---:|---|---:|
| `1.21.1-neoforge` | 1.21.1 | NeoForge | 21 |
| `1.20.1-forge` | 1.20.1 | Forge | 17 |
| `1.20.1-fabric` | 1.20.1 | Fabric | 17 |

Each former Git branch is preserved under `versions/<minecraft>-<loader>/src`. This keeps the
loader-specific integrations intact while Stonecutter provides one project graph and one set of
top-level commands.

## Building

```sh
./gradlew projects
./gradlew :1.21.1-neoforge:build
./gradlew :1.20.1-forge:build
./gradlew :1.20.1-fabric:build
./gradlew chiseledBuild
./gradlew integrationTest
```

The active development target is recorded in `.sc_active_version`. Use Stonecutter's generated
`Set active project to ...` tasks to switch it.

Compatibility integrations are guarded by bytecode contract tests and Minecraft-backed resolver
tests. When adding an integration, update its target's `integration-contracts.txt`; the test suite
fails if `ModIntegration` and the manifest differ. See `test-support/README.md` for the required
workflow.

## Features

- Access various mod screens directly from your inventory
- Press the inventory key (default e) to return to the inventory from a clicked tab
- Compatible with the following mods:
  - FTB Quests
  - FTB Teams
  - Xaero's World Map
  - Journeymap
  - Travelers Backpack
  - Sophisticated Backpacks
  - Cobblemon
  - Pufferfish Skills
  - Map Atlases
  - Cosmetic Armor Reworked
  - L2 mods (hostility / artifacts) etc
  - Modular Golems
  - Ars Elixirium
  - Reskillable Reimagined
