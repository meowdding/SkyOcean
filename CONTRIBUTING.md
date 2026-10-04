# Contributing Guidelines

> [!WARNING]
> Pull requests suspected of being created, documented, or aided with AI/LLM usage will be closed without further discussion.

If you have questions, join our [Discord](https://meowdd.ing/discord).

## Prerequisites

Basic Java/Kotlin knowledge is required.

- **IDE**: Use [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
- **JDK**: Install [JDK 25](https://jdk.java.net/archive/)
- **Optional IntelliJ Plugins:**
    - [Minecraft Development](https://plugins.jetbrains.com/plugin/8327-minecraft-development)
    - [Stonecutter Dev](https://plugins.jetbrains.com/plugin/25044-stonecutter-dev)

## Setting Up

1. Clone the repository
2. Open the project in IntelliJ
3. Set up JDK 25 in Project Settings

## Writing Code & Booting up the Game

To support multiple Minecraft versions at once, we use [Stonecutter](https://stonecutter.kikugie.dev/), a lib for Minecraft Multiversion.
Code shared between the Minecraft versions is in src/main,
version specific code in src/\<version> but can also be in main with preprocessed comments.

To learn how to use Stonecutter, please reference the [Docs](https://stonecutter.kikugie.dev/wiki/v2/reference/) or the existing code.

## Dependencies

Meowdding mainly uses these custom libraries:

### [SkyBlockAPI](https://github.com/SkyBlockAPI/SkyblockAPI)
Handles SkyBlock features:
- API's like BazaarAPI, CurrencyAPI, SlayerAPI, etc.
- Other SkyBlock utilities

### [MeowddingLib](https://github.com/meowdding/meowdding-lib)
Provides:
- Rendering systems (Displays, Layouts)
- Common utilities

## Adding Features

If an API feature is missing:

1. Implement it temporarily in your feature mod
2. Test if it works
3. Open a PR to the appropriate library
