package dev.freggy.flash.listener

import dev.freggy.flash.FlashPlugin
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.EntityDamageEvent
import dev.freggy.flash.GameState
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.weather.WeatherChangeEvent



/**
 * @author Yannic Rieger
 */
class CancelListener(val flash: FlashPlugin) : Listener {
    @EventHandler
    fun on(e: WeatherChangeEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerInteractEvent) {
        if (e.action != Action.PHYSICAL) e.isCancelled = true
    }

    @EventHandler
    fun on(e: BlockPlaceEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: BlockBreakEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: EntityDamageEvent) {
        if (this.flash.state == GameState.WAITING
            || this.flash.state == GameState.FINISHING
            || e.entityType != EntityType.PLAYER) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: FoodLevelChangeEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: BlockFormEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: BlockFromToEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: CreatureSpawnEvent) {
        if (e.spawnReason != SpawnReason.CUSTOM) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: InventoryClickEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerDropItemEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerPickupItemEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerInteractEntityEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: PlayerInteractAtEntityEvent) {
        e.isCancelled = true
    }
}
