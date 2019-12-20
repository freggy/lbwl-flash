package dev.freggy.flash.listener

import dev.freggy.flash.*
import dev.freggy.flash.event.PlayerCheckpointEvent
import dev.freggy.flash.event.PlayerFinishedEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Furnace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin

class PlayerListener(private val plugin: Plugin) : Listener {


    @EventHandler
    private fun onFinishTriggered(event: PlayerInteractEvent) {
        if (event.player.gameMode == GameMode.SPECTATOR) return
        if (event.action != Action.PHYSICAL) return
        if (event.clickedBlock?.type != Material.WOOD_PLATE) return
        val type = event.clickedBlock.location.subtract(0.0, 1.0, 0.0).block.type
        if (type != Material.WOOL) return
        Bukkit.getPluginManager().callEvent(PlayerFinishedEvent(event.player, System.currentTimeMillis()))
    }

    @EventHandler
    private fun onCheckpointTriggered(event: PlayerInteractEvent) {
        if (event.player.gameMode == GameMode.SPECTATOR) return
        if (event.action != Action.PHYSICAL) return
        if (event.clickedBlock?.type != Material.STONE_PLATE) return

        // Legacy checkpoint format
        val player = event.player
        val state = event.clickedBlock!!.location.subtract(0.0, 1.0, 0.0).block.state

        if (state !is Furnace) return

        val number = state.inventory.name!!.toInt()

        if (number == player.getCurrentCheckPointIndex() + 1) {
            Bukkit.getPluginManager().callEvent(
                PlayerCheckpointEvent(player, Checkpoint(player.location.clone(), System.currentTimeMillis()))
            )
            return
        }

        if (number > player.getCurrentCheckPointIndex() + 1) {
            player.sendMessage("$PREFIX §cDu hast ein Checkpoint übersprungen! Du wurdest zurück teleportiert!")
            player.respawn()
            player.playSound(player.location, Sound.ENDERMAN_DEATH, 1.0F, 1.0F)
            return
        }
    }

    @EventHandler
    private fun onItemInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item == null) return

        val type = event.item.type
        val player = event.player

        if (type == Material.INK_SACK) {
            player.respawn()
            return
        }

        if (type == Material.BLAZE_ROD || type == Material.STICK) {
            event.player.toggleVisibility()
            return
        }
    }

    @EventHandler
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        event.keepInventory = true
        event.entity.player.spigot().respawn()
        Bukkit.getScheduler().runTaskLater(plugin, { event.entity.respawn() }, 1)
    }

    @EventHandler
    private fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        if (event.cause != EntityDamageEvent.DamageCause.VOID) return
        (event.entity as Player).respawn()
    }
}
