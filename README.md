# IFPatcher [![Build](https://github.com/eve0415/IFPatcher/actions/workflows/build.yml/badge.svg)](https://github.com/eve0415/IFPatcher/actions/workflows/build.yml) [![Release](https://img.shields.io/github/v/release/eve0415/IFPatcher)](https://github.com/eve0415/IFPatcher/releases/latest)

As Industrial Foregoing for 1.12.2 is EOL, this is a project to patch some known issues with Industrial Foregoing for
1.12.2:

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/industrial-foregoing)
- [modrinth](https://modrinth.com/mod/industrial-foregoing)

*It will not work on any other versions.*

Please use the latest version (`1.12.13-237`) to use this:

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/industrial-foregoing/files/2745321)
- [modrinth](https://modrinth.com/mod/industrial-foregoing/version/1.12.13-237)

## Features

This mod will patch some bugs or implement some features:

- Insertion Conveyor Upgrade
    - Will not duplicate items when using multiple insertion upgrade on one conveyor belt.
- Fluid Pump
    - Make pump work properly on any conditions.
    - Can now fill fluid to buckets directly from GUI.
- Laser Base
    - Stop crashing Minecraft when using negative lens.
- Potion Brewer
    - Can now brew potion that the ingredients does not start from `minecraft:nether_wart`. (Ex. Potion of Weakness)
- Plant Sower
    - Till any blocks if possible. (You no longer have to use `minecraft:dirt` or `minecraft:grass`)
- Mob Imprisonment Tool
    - Implement blacklist for mobs that can't be imprisoned.
        - Support wildcard `*`. (Ex. `minecraft:*`)
    - Add option to enable/disable of imprisoning boss mobs. (`blacklistBosses`)
        - Even if `blacklistBosses` is set to `false`, you can't imprison boss mobs if it is in the blacklist.
- Latex Processing Unit
    - Will not eat `Latex Bucket` when tank is already full enough to fill.

## Installation

You can download the latest version from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/ifpatcher)
or [modrinth](https://modrinth.com/mod/ifpatcher).

## Other

If you find any other issues related to IF, please open an issue or pull request on this repository.

I will try to find some time to patch some other issues if requested.
