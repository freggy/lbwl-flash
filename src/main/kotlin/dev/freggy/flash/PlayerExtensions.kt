package dev.freggy.flash

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionEffect

private val gameData = WeakHashMap<Player, GameData>()

fun Player.initGameData(speed: Int, spawn: Location) {
    gameData[player] = GameData(mutableSetOf(), speed, spawn)
}

fun Player.isIngame() = this.gameMode != GameMode.SPECTATOR

fun Player.getCurrentCheckPointLocation() = gameData[this]?.checkpoints?.last()?.location

fun Player.getCurrentCheckPointIndex() = gameData[this]?.checkpoints?.size ?: 0

fun Player.setCurrentCheckpoint(checkpoint: Checkpoint) {
    gameData[this]?.checkpoints?.add(checkpoint)
    println(gameData[this]?.checkpoints?.size)
}

fun Player.respawn() {
    this.applyEffects()

    println("lol")

    if (gameData[player]!!.checkpoints.isEmpty()) {
        this.teleport(gameData[player]?.spawn)
    } else {
        this.teleport(this.getCurrentCheckPointLocation())
    }

    this.playSound(this.location, Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F)

    this.health = 20.0
    this.fireTicks = 0
    this.fallDistance = 0.0F
}

fun Player.applyEffects() {
    this.activePotionEffects.forEach { this.removePotionEffect(it.type) }
    this.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 3))
    this.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, gameData[player]!!.mapSpeed))
}
