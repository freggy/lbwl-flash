package dev.freggy.flash.listener

import dev.freggy.flash.Checkpoint
import dev.freggy.flash.FlashPlugin
import dev.freggy.flash.event.PlayerCheckpointEvent
import dev.freggy.flash.getCurrentCheckPointIndex
import dev.freggy.flash.respawn
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin

class PlayerListener(val plugin: Plugin) : Listener {

    @EventHandler
    private fun onPlayerInteract(event: PlayerInteractEvent) {
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
        }
    }

    @EventHandler
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        println("xdd")
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
