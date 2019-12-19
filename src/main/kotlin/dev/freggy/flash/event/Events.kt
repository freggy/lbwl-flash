package dev.freggy.flash.event

import dev.freggy.flash.Checkpoint
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent


class PlayerCheckpointEvent(player: Player, val checkpoint: Checkpoint) : PlayerEvent(player) {

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers() = handlerList
}

class PlayerFinishedEvent(player: Player, val finished: Long) : PlayerEvent(player){
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers() = handlerList
}
