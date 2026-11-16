# Minecraft Plugin: Herobrine's Chasing Shadows

Herobrine's Chasing Shadows is a Paper Minecraft plugin for rare, atmospheric Herobrine encounters, combining stalking effects, scary structures, environmental sounds, particles, potion effects, and administrator controls to create a suspenseful survival experience.

## Install

1. Install a Paper Minecraft server compatible with Minecraft 1.21 and Java 21.
2. Build the plugin with Maven:
   ```bash
   mvn package
   ```
3. Copy `target/plugin.jar` into the server's `plugins` directory.
4. Start or restart the server.
5. Review and adjust `plugins/HerobrineAwakening/config.yml` if needed.

The plugin registers the `/herobrine` command for operators with the `herobrine.admin` permission.

## Use

Herobrine encounters are checked automatically at the configured interval. A player may be selected when:

- It is nighttime in the player's world.
- The player is alive.
- The player is not in The End.
- The configured random encounter chance succeeds.

When an encounter begins, the plugin finds a location

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/efc57daf-8d9f-453b-be34-702a6869b31f?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=efc57daf-8d9f-453b-be34-702a6869b31f
