package dev.freggy.flash

import dev.freggy.flash.event.PlayerCheckpointEvent
import dev.freggy.flash.listener.CancelListener
import dev.freggy.flash.listener.PlayerListener
import org.apache.commons.io.FileUtils
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.io.File

class FlashPlugin : JavaPlugin(), Listener {

    private var playerCheckTask: BukkitTask? = null
    private var timerTask: BukkitTask? = null
    private val mapVoting by lazy {
        MapVoting(this.loadMapInfo())
    }
    private var time = 5
    private val maxPlayers = 10
    private val minPlayers = 1

    var state = GameState.INIT
        set(value) {
            // TODO: update kubernetes
            field = value
        }

    fun startWaitingPhase() {
        this.playerCheckTask = Bukkit.getScheduler().runTaskTimer(this, {
            val currentPlayers = Bukkit.getOnlinePlayers().size
            val needed = minPlayers - currentPlayers

            Bukkit.broadcastMessage("Noch Benötigte Spieler: $needed")

            if (currentPlayers < this.minPlayers) {
                this.time = 5
                timerTask?.cancel()
                timerTask = null
            }

            if (currentPlayers >= this.minPlayers) {
                if (this.timerTask != null) return@runTaskTimer
                this.timerTask = Bukkit.getScheduler().runTaskTimer(this, {
                    this.time--
                    Bukkit.getOnlinePlayers().forEach { player -> player.level = this.time }
                    if (this.time <= 0) {
                        this.timerTask!!.cancel()
                        this.playerCheckTask!!.cancel()
                        this.startGame()
                    }
                }, 0, 20)
            }

            if (currentPlayers >= this.maxPlayers) {
                this.time = 10
                Bukkit.broadcastMessage("Zeit wird auf $time Sekunden verkürzt.")
            }
        }, 20L, 20L)
    }

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getPluginManager().registerEvents(PlayerListener(this), this)
        Bukkit.getPluginManager().registerEvents(CancelListener(this), this)

        this.mapVoting.registerListeners(this)
        this.startWaitingPhase()
        this.state = GameState.WAITING
    }


    override fun onCommand(
        sender: CommandSender?,
        command: Command?,
        label: String?,
        args: Array<out String>?
    ): Boolean {
        if (label.equals("lol")) {

            if (args!!.isNotEmpty()) {
                (sender as Player).respawn()
                return false
            }
            this.mapVoting.open(sender as Player)
        }
        return true
    }

    private fun startGame() {
        val map = this.mapVoting.determineMap()
        this.mapVoting.end()
        val world = this.loadMap(map)

        Bukkit.getOnlinePlayers().forEach {
            val location = MapConfig.locFromString(map.spawnString, world)
            it.initGameData(map.speedLevel, location!!)
            it.gameMode = GameMode.ADVENTURE
            it.isFlying = false
            it.allowFlight = false
            it.applyEffects()
            it.teleport(location)
        }

        this.state = GameState.RUNNING
    }

    private fun loadMapInfo(): List<MapConfig> {
        val file = File(this.dataFolder, "maps")
        return file.listFiles()
            .filter { it.isDirectory }
            .map { File(it.absolutePath, "mapconfig.yml") }
            .map { MapConfig.read(it) }
            .toList()
    }

    private fun loadMap(config: MapConfig): World {
        FileUtils.copyDirectory(File("${this.dataFolder}/maps/${config.name}"), File(config.name))
        val world = WorldCreator(config.name)
            .generateStructures(false)
            .environment(World.Environment.NORMAL)
            .createWorld()

        world.setGameRuleValue("doFireTick", "false")
        world.setGameRuleValue("mobGriefing", "false")
        world.setGameRuleValue("doDaylightCycle", "false")

        return world
    }

    @EventHandler
    private fun onPlayerReachCheckpoint(event: PlayerCheckpointEvent) {
        val player = event.player
        player.setCurrentCheckpoint(event.checkpoint)
        player.playSound(player.location, Sound.LEVEL_UP, 1.0F, 1.0F)

        // TODO: display message
        // TODO: spawn firework
        // TODO: Update scoreboard
    }

    @EventHandler
    private fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = false
    }
}

