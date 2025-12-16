# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ANVIL (Advanced Nexus of Variable Implement Leverage) is a NeoForge mod for Minecraft 1.21.1 focused on PvE tool customization. The mod aims to provide a deep tool crafting and leveling system inspired by Tinkers' Construct and Path of Exile.

**Target Platform**: NeoForge 1.21.1
**Java Version**: 21
**Mod ID**: `anvil`
**Package**: `io.github.nazottix.anvil`

## Build Commands

```bash
# Build the mod
./gradlew build

# Run Minecraft client with mod loaded
./gradlew runClient

# Run dedicated server
./gradlew runServer

# Run data generation
./gradlew runData

# Run game tests
./gradlew runGameTestServer

# Refresh dependencies (if missing libraries)
./gradlew --refresh-dependencies

# Reset project (does not affect code)
./gradlew clean
```

## Architecture

### Entry Points

- `ANVIL.java` - Main mod class, handles common setup and registry registration
- `ANVILClient.java` - Client-side only code (annotated with `@Mod(dist = Dist.CLIENT)`)
- `Config.java` - Mod configuration using NeoForge's `ModConfigSpec`

### Registry Pattern

The mod uses NeoForge's `DeferredRegister` pattern for registering game objects:
- `BLOCKS` - Block registry
- `ITEMS` - Item registry
- `CREATIVE_MODE_TABS` - Creative mode tab registry

Registries are initialized in the main mod constructor and registered to the mod event bus.

### Event Handling

- Mod lifecycle events use `@SubscribeEvent` on the mod event bus
- Game events (like `ServerStartingEvent`) use `NeoForge.EVENT_BUS`
- Client-side events use `@EventBusSubscriber(value = Dist.CLIENT)`

## Key Configuration Files

- `gradle.properties` - Mod metadata and version configuration
- `src/main/templates/META-INF/neoforge.mods.toml` - Mod descriptor template (variables substituted at build)
- `src/main/resources/assets/anvil/lang/en_us.json` - Localization

## Design Specification

See `docs/Minecraftツール作成mod 仕様書.md` for the complete design document (in Japanese) covering:
- Parts system (heads, handles, bindings, etc.)
- Leveling system (up to level 10,000)
- Skill tree system
- Grid-based module placement
- Capacity/weight system
- Rarity system
- Addon API design

### レポジトリの概要
---
- ANVIL (Advanced Nexus of Variable Implement Leverage)は、Minecraft 1.21.1向けのNeoForge modで、PvEツールのカスタマイズに焦点を当てています。このmodは、Tinkers' ConstructやPath of Exile等に触発された深いツールクラフティングとレベリングシステムを提供することを目的としています。

### 参考にする資料
---
- NeoForge公式ドキュメント: https://docs.neoforged.net/
- プロジェクトフォルダ内のdocsの中身

### 義務事項
---
- 日本語で結果を返すこと
- コードを編集する際は、必ず変更箇所にコメントで説明を追加すること
- 変更を加える前に、必ず変更内容の提案を行い、 質問がある場合は質問を行い、 変更内容に関するドキュメント更新が必要かどうかを確認すること
- 途中結果を返すこと
- 最終結果を返すこと
- わからない情報がある場合、わからない内容を質問し、指示を待つこと
- 続けて作業を行えるように、途中結果や最終結果をdocsに出力すること

### 禁止事項
---
- 参考にする資料以外のことを指示がない状態で検索すること、ただし、検索の許可をする指示を得たら検索しても良いこととする。
- logを日本語で書かずに英語で書くこと