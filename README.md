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
- Hotkeys
    - Add multiple hotkeys dependent on different contexts, like the SkyBlock Island or your selected Dungeon Class.
    - Nest contexts based on operations like "or", "and", etc.
    - Share hotkeys with other profiles.
    - Download predefined hotkey profiles from the community.
    - Define hotkeys based on keypress order (e.g. pressing `1` then `2` to trigger a different hotkey than pressing `2` then `1`).
</details>

<details>
<summary>Commands</summary>

- `/skyocean calc <equation>` - performs a mathematical equation and prints the result
- `/skyocean sendcoords` - sends the current coordinates in all chat
- `/skyocean search` - opens the item search screen
- `/skyocean hotkeys` - opens the hotkey configuration screen
- `/skyocean recipe <recipe> [<amount>]` - Sets the selected recipe as the active Craft Helper Item.
    - `/skyocean recipe amount <amount>` -  Sets the amount of items to craft for the Craft Helper.
    - `/skyocean recipe skyshards` - Sets the [SkyShards](https://skyshards.com/) Tree as the active Craft Helper Tree from your clipboard.
    - `/skyocean recipe clear`

</details>


<details>
<summary>General Config</summary>

- Disable text shadows for all SkyOcean messages
- Prefix gradient customization
- Clickable chat message prefix (opens the config)
- SkyOcean Item Modify Indicator (Prefix, Suffix, Lore, or None)

</details>

<details>
<summary>Chat Config</summary>

- Profile In Chat
    - Adds a bingo/ironman/stranded icon next to a player's chat message.
- Consistent Player Message Color
    - Makes messages from players without rank white instead of gray.
- Piggy Bank Repair Helper
    - When you die and the piggy bank breaks, you can click on the chat message to get 8 Enchanted Pork from your sacks.
- Sack Notification
    - Specify for which sack items you want to receive a notification when you gain them, e.g. `Glossy Gemstone`.
- Reply Boop
    - Adds a `/rboop` command that runs `/boop` on the last person that messaged you.

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
- Hotspot Highlight Features
    - Chat warning when a hotspot you recently fished at disappears.
    - Render a solid surface circle or solid outline around the hotspot.
    - Adjustable transparencies for surface and outline highlights.

</details>

<details>
<summary>Foraging Config</summary>

- Heart of the Forest (HOTF)
    - Show level as amount (stack size)
    - Show total progress
    - Show shift-click cost
    - Show total amount left
    - Configurable reminders (and titles) for when you have enough powder.
- Galatea
    - Mute the Phantoms
    - Moonglade Beacon Color (Red when inactive, Green when active)
    - Shulker Overwrite (recolors shulkers to make them more visible)

</details>

<details>
<summary>Garden Config</summary>

- Pest Bait Type
    - While in a Trap inventory, view what bait type attracts what pests.
- Desk Pest Highlight
    - In the "Configure Plots" screen, highlight what plots have pests on them.
- Pest Warning
    - Warns you (with an adjustable delay) when you reach 4 or more pests while farming.

</details>

<details>
<summary>Inventory Config</summary>

- Sack Value
    - View what item is worth how much in each sack.
    - Choose between Bazaar or NPC prices.
    - Toggle showing in the Sack of Sacks, hiding items with no value, and displaying the total value.
- Inventory Buttons
    - Customizable buttons at the top and bottom of your inventory, like the creative inventory tabs.
    - Features a dedicated in-game UI to configure button commands, titles, and tooltips.

</details>

<details>
<summary>Lore Modifier Config</summary>

- Drill Modifications
    - Compacts drill upgrade parts (engine, fuel tank, omelette) into single lines.
- Dungeon Quality
    - Adds the Dungeon Quality out of 50 to the lore of dungeon items.
- Compact Level Bars
    - Compacts the bars in the SkyBlock levels menu into one line, making them fit on smaller screens.
- Prehistoric Egg Blocks Walked
    - Shows how many blocks you have walked so far on a prehistoric egg.
- Storage Preview
    - Previews the contents of your backpacks and ender chest in the tooltip.
- Museum Donation
    - Shows what items haven't been donated to the museum in the tooltip.
- Dye Hex Display
    - Shows the hex code of the dye on armors, with configurable left/middle/right positioning.
- Midas Bid Breakdown
    - Shows the original bid and added coins on Midas Weapons.

</details>

<details>
<summary>Mining Config</summary>

- Modify Commissions
    - Modifies commission items to show the progress you've made.
- Heart of the Mountain (HOTM)
    - Show level as amount, total progress, shift-click cost, and total amount left.
    - Powder reminders and titles.
- Forge Reminder
    - Sends a reminder for unclaimed expired forge items with a configurable delay.
    - Customizable click action (Warp to Forge, Call Fred, or both).
- Mineshaft
    - Mineshaft enter announcements (Chat/Party) and title alerts.
    - Pity message upon finding a mineshaft.
    - Corpse waypoints for all possible locations.
    - Chat message announcing how many corpse keys you currently have in your sacks.
- Scathas
    - Worm type announcer and spawn cooldown notifier.
    - Pet drop message modifiers (includes rarity and drops title alerts).
- Metal Detector Solver
    - Helps find treasures in the Mines of Divan with optional Ding sounds and titles upon finding.
- Retexture carpets to red.
- Puzzler Solver.

</details>

<details>
<summary>Gambling Config</summary>

- Dungeon Chest Gambling
    - Plays a CSGO Case Opening animation when opening Obsidian and Bedrock Chests.
    - Can optionally be enabled in the Croesus menu as well.
- Vanguard Gambling
    - Shows a slot machine animation when looting a Vanguard.
    - Option to hide chat while the gambling animation plays to avoid spoilers.

</details>

<details>
<summary>Misc Config</summary>

- Craft Helper
    - Creates a Tree of the item you want to craft, showing the progress of each sub item.
    - Also accessible through `/skyocean recipe <recipe> [<amount>]`
    - Optional HUD overlay with background, custom positioning, and margins.
    - Choose between Tree and Raw formatters.
    - "Quick Set" items directly from menus (forge, recipe, visitors).
    - Ignore specific items, disable root item tracking, or configure done messages.
- Mob Icons
    - Replaces the default Hypixel mob icons with spelled-out versions.
    - Configurable display types and individual custom colors for every mob icon.
- Fun: Player Animals
    - Render players in the world as animals (Cats, Foxes, Wolves, Axolotls, Pandas, and many more).
    - Configure who is affected (Nobody, Self, Players, Everyone).
    - Customize individual appearance traits (variants, collars, baby status, sheared states).
- Item Search
    - Customize highlight colors, highlight modes, and highlight durations.
    - Preserve your last search queries.
    - REI Search Bar integration.
    - Museum integration (highlights items you have on your profile vs what you don't).
    - Auto-warp to island upon clicking.
- Minister in Calendar.
- Previous server notifications (with adjustable cache time).
- Anvil helper (highlights matching books).
- Queue estimation based on speed.
- Item Star Stacksize modifier.
- Revert Master Stars to their stacked colored look.
- Animate dyes and skins in inventories (Vanilla Customization Integration).
- Show Hidden Pet Candy (re-adds hidden candy lines for level 100 pets).
- Hide lightning bolts, sky flashes, and entity fire.
- Transparent armor (configurable for yourself and others).
- Rat Hitboxes (renders extra hitboxes for hub rats).
- Island Cloud Hider (disables clouds in places like Dwarven Mines or Crystal Hollows).
- Full Text Shadow modifier.
- Show museum armor pieces (displays all pieces required to finish a set in the tooltip).

</details>
