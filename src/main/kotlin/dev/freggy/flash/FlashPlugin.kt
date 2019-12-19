package dev.freggy.flash

import dev.freggy.flash.event.PlayerCheckpointEvent
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.FileUtil
import java.io.File
import java.nio.file.Files

class FlashPlugin() : JavaPlugin(), Listener {

    private var playerCheckTask: BukkitTask? = null
    private var timerTask: BukkitTask? = null
    private val maxPlayers = 10
    private val minPlayers = 2
    private var time = 20

    fun startWaitingPhase() {
        this.playerCheckTask = Bukkit.getScheduler().runTaskTimer(this, Runnable {
            val currentPlayers = Bukkit.getOnlinePlayers().size
            val needed = minPlayers - currentPlayers

            Bukkit.broadcastMessage("Noch Benoetigte Spieler: $needed")

            if (currentPlayers < this.minPlayers) {
                this.time = 20
                timerTask?.cancel()
            }

            if (currentPlayers >= this.minPlayers) {
                this.timerTask = Bukkit.getScheduler().runTaskTimer(this, Runnable {
                    this.time--
                    Bukkit.getOnlinePlayers().forEach { player -> player.level = this.time }
                    if (this.time >= 0) {
                        this.startGame()
                    }
                }, 0, 20)
            }

            if (currentPlayers >= this.maxPlayers) {
                this.time = 10
                Bukkit.broadcastMessage("Zeit wird auf $time Sekunden verkuerzt.")
            }
        }, 20L, 20L)
    }

    override fun onEnable() {
        //this.startWaitingPhase()
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getPluginManager().registerEvents(PlayerListener(), this)
        val maps = this.loadMaps()

        maps.forEach {
            println(it.name)
            println(it.builder)
            println(it.checkpoints)
            println(it.spawnString)
        }

    }


    override fun onCommand(
        sender: CommandSender?,
        command: Command?,
        label: String?,
        args: Array<out String>?
    ): Boolean {
        val maps = this.loadMaps()
        if (label.equals("lol")) {
            FileUtils.copyDirectory(File("${this.dataFolder}\\maps\\${maps[0].name}"), File(maps[0].name))
           val w = WorldCreator(maps[0].name)
               .generateStructures(false)
               .environment(World.Environment.NORMAL)
                .createWorld()

            (sender as Player).teleport(Location(w, 0.0, 100.0, 0.0))
        }

        return true
    }

    private fun startGame() {
        // TODO: teleport players and stuff
    }

    private fun loadMaps(): List<MapConfig> {
        val file = File(this.dataFolder, "maps")
        return file.listFiles()
            .filter { it.isDirectory }
            .map { File(it.absolutePath, "mapconfig.yml") }
            .map { MapConfig.read(it) }
            .toList()
    }

    @EventHandler
    private fun onPlayerReachCheckpoint(event: PlayerCheckpointEvent) {
        event.player.setCurrentCheckpoint(event.checkpoint)
        // TODO: Update scoreboard
    }

    @EventHandler
    private fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = false
    }

    @EventHandler
    private fun onPlayerDamage(event: EntityDamageEvent) {
        event.isCancelled = false
    }
}