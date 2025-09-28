<h1 align="center">
  SkyOcean
</h1>

<div align="center">

[![Discord](https://img.shields.io/discord/1296157888343179264?color=8c03fc&label=Discord&logo=discord&logoColor=white)](https://discord.gg/FsRc2GUwZR)
[![Modrinth](https://img.shields.io/modrinth/dt/dIczrQAR?style=flat&logo=modrinth)](https://modrinth.com/mod/skyocean)

</div>


SkyOcean is a mod that aims to improve the playing experience while staying true to the skyblock style, meaning that we aim to integrate with the base game in a
seamless way.

## Installation Guide

- Prerequisites: [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
- Download into your favourite launcher or into your mods folder directly

## Features

<details>
<summary>Screens</summary>

- Item Value Screen
    - When hovering over an item in any inventory, you can press `J` to open the item value screen.
    - This screen shows a breakdown of item value sources by category, such as item stars, recombs, and more.
- Item Search Screen
    - Press `o` or run `/skyocean search` to open the item search screen.
    - Allowing you to search through the following:
        - Island Chests
        - Storage (Backpack/Enderchest)
        - Wardrobe
        - Sacks
        - Accessory Bag
        - Forge
        - Inventory (overworld)
        - Vault
        - Museum
        - Rift (Inventory/Enderchest)
        - Drill & Rod upgrades (upgrade parts that are applied)
        - and way more...
  - Using Right Click on any sack item will open an input to get that item from the sack.
- Item Customisation Screen
    - Run `/skyocean customize` while having an item selected to either customize it with subcommands or a gui
    - Allows you to customize the following attributes:
        - Name (with custom colors and gradients)
        - Item Model
        - Item Color (static or gradients)
        - Armor Trim
        - Enchantment Glint
        - Skins (static and animated)

</details>

<details>
<summary>Commands</summary>

- /skyocean calc \<equation> - performs a mathematical equation and prints the result
- /skyocean sendcoords - sends the current coordinates in all chat
- /skyocean search - opens the item search screen
- /skyocean recipe \<recipe> [\<amount>] - Sets the selected recipe as the active Craft Helper Item.
    - /skyocean recipe amount \<amount> -  Sets the amount of items to craft for the Craft Helper.
    - /skyocean recipe skyshards - Sets the [SkyShards](https://skyshards.com/) Tree as the active Craft Helper Tree from your clipboard.
    - /skyocean recipe clear

</details>


<details>
<summary>General config</summary>

- Option to disable text shadows for all SkyOcean messages.
- Prefix gradient
- SkyOcean Item Modify Indicator
</details>

<details>
<summary>Chat Config</summary>

- Profile In Chat
    - Adds a bingo/ironman/stranded icon next to a player's chat message.
- Piggy Bank Repair Helper
    - When you die and the piggy bank breaks, you can click on the chat message to get 8 Enchanted Pork from your sacks.
- Sack Notification
    - Specify for which sack items you want to receive a notification when you gain them, e.g. `Glossy Gemstone`.
- White non mesages
    - Makes messages from players without rank white.

</details>

<details>
<summary>Slayer Config</summary>

- Highlight own slayer boss
- Highlight mini bosses
    - Extra option to change color for high tier minis
- Highlight blaze in attunement color

</details>

<details>
<summary>Fishing Config</summary>

- Trophy Fish Numbers
    - Shows the number of Trophy Fish you have caught in the chat message of the specific fish you caught.
    - Best used with the [SkyBlock Profile Viewer](https://modrinth.com/mod/skyblock-profile-viewer) mod to update your data when out of sync.
- Hook Text Scale
    - Change the scale of the text that appears when you're about to hook a fish.
- Lava Replacement
    - Visually replace lava with water in the Crimson Isle.
- Bobber Fix
    - Fixes fishing bobbers rubberbanding when fishing.
- Bobber Timer
    - Shows a timer above your bobber to indicate how long it has been in the liquid.
- Hide other Bobbers
    - Hides other players' fishing bobbers, so you can see your own bobber more clearly.
- Hotspot Highlight
    - Highlights hotspots with a color fitting to their type.
    - Renders a circle around the hotspot and fills it with the color.

</details>

<details>
<summary>Foraging Config</summary>

- HOTF
    - Show level as amount
    - Show total progress
  - Show shift-click cost
    - Show total amount left
- Galatea
    - Mute the Phantoms
    - Shulker Overwrite
        - Allows you to overwrite the Shulker's color with a custom one.

</details>

<details>
<summary>Garden Config</summary>

- Pest Bait Type
    - While in a Trap inventory, view what bait type attracts what pests.
- Desk Pest Highlight
    - In the "Configure Plots" screen, highlight what plots have pests on them.

</details>

<details>
<summary>Inventory Config</summary>

- Sack Value
    - View what item is worth how much in each sack.
    - Choose between Bazaar or NPC prices.
- Inventory Buttons
    - Customizable buttons at the top and bottom of your inventory, like the creative inventory tabs.

</details>

<details>
<summary>Lore Modifier Config</summary>

- Drill Lore
    - Removes the Abilities of each drill component, keeping just the component's name.
- Dungeon Quality
    - Adds the Dungeon Quality to the lore of dungeon items.
- Compact Level Bar
    - Compacts the bars in the skyblock levels menu into one line, making them fit on smaller screens.
- Storage Preview
    - Previews the contents of your backpacks and ender chest in the tooltip.

</details>

<details>
<summary>Mining Config</summary>

- Modify Commissions
    - Modifies commission items to show the progress you've made.
- HOTM
    - Show level as amount
    - Show total progress
  - Show shift-click cost
    - Show total amount left
- Mineshaft
    - Announce mineshaft enter
    - Corpse waypoints
    - Corpse key information
- Retexture
    - Retexture carpets
    - Retexture Glacite Tunnel blocks
    - Retexture mist
    - Retexture gemstones
    - Retexture Crystal Hollows blocks
- Crystal Hollows Area Walls
- Puzzler Solver
- Metal detector solver

</details>

<details>
<summary>Dungeon Config</summary>

- CSGO Gambling Screen
  - Plays the CSGO Gambling animation when opening any Obsidian and Bedrock Chest.
  - Also works in Croesus.

</details>

<details>
<summary>Misc Config</summary>

- Craft Helper
    - Creates a Tree of the item you want to craft, showing the progress of each sub item.
    - Also accessible through `/skyocean recipe <recipe> [<amount>]`
- Mob Icons
    - Replaces the default hypixel mob icons with a custom texture, also has texturepack support.
- Minister in Calendar
- Previous server notifications
- Anvil helper
- Hide lightning bolts and flashes
- Full Text Shadow
- Show Hidden Pet Candy
    - Readds pet candy that Hypixel hides for pets that are level 100.
- Transparent armour
    - On yourself and others, configurable transparency.
- Hide entity fire
    - Removes the fire effect from all entities
- Cloud hider for specific islands

</details>
