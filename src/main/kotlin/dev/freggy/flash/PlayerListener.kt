package dev.freggy.flash

import dev.freggy.flash.event.PlayerCheckpointEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PlayerListener : Listener {

    @EventHandler
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.player.gameMode == GameMode.SPECTATOR) return
        if (event.action != Action.PHYSICAL) return
        if (event.clickedBlock?.type != Material.STONE_PLATE) return

        // Legacy checkpoint format
        val player = event.player
        val furnace = event.clickedBlock!!.location.subtract(0.0, 2.0, 0.0).block.state as Furnace
        val number = furnace.inventory.name!!.toInt()

        if (number == player.getCurrentCheckPointIndex() + 1) {
            Bukkit.getPluginManager().callEvent(
                PlayerCheckpointEvent(player, Checkpoint(player.location.clone(), System.currentTimeMillis()))
            )
        }
    }
}