package dev.freggy.flash

import org.bukkit.Location

data class Checkpoint(val location: Location, val timeReached: Long)

data class GameData(val checkpoints: MutableList<Checkpoint>)
