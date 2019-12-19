package dev.freggy.flash

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

private val gameData = WeakHashMap<Player, GameData>()

fun Player.isIngame() = this.gameMode != GameMode.SPECTATOR

fun Player.getCurrentCheckPointLocation() = gameData[this]?.checkpoints?.last()?.location

fun Player.getCurrentCheckPointIndex() = gameData[this]?.checkpoints?.size ?: 0

fun Player.setCurrentCheckpoint(checkpoint: Checkpoint) {
    gameData[this]?.checkpoints?.add(checkpoint)
}
