package dev.freggy.flash

import dev.freggy.flash.event.PlayerCheckpointEvent
import dev.freggy.flash.event.PlayerFinishedEvent
import dev.freggy.flash.listener.CancelListener
import dev.freggy.flash.listener.PlayerListener
import org.apache.commons.io.FileUtils
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.io.File
import kotlin.math.max


class FlashPlugin : JavaPlugin(), Listener {

    private var playerCheckTask: BukkitTask? = null
    private var lobbyTimerTask: BukkitTask? = null
    private var gamerTimerTask: BukkitTask? = null
    private var lobbyTime = 20
    private var roundTime = 3600
    private var startTime = 0L
    private var mapConfig: MapConfig? = null

    private val scoreboard by lazy { FlashScoreboard(this) }
    private val maxPlayers = 10
    private val minPlayers = 1

    val mapVoting by lazy { MapVoting(this.loadMapInfo()) }

    var state = GameState.INIT
        set(value) {
            // TODO: update kubernetes
            field = value
        }

    private fun startWaitingPhase() {
        this.playerCheckTask = Bukkit.getScheduler().runTaskTimer(this, {
            val currentPlayers = Bukkit.getOnlinePlayers().size
            val needed = max(minPlayers - currentPlayers, 0)

            Bukkit.broadcastMessage("Noch Benötigte Spieler: $needed")

            if (currentPlayers < this.minPlayers) {
                this.lobbyTime = 20
                lobbyTimerTask?.cancel()
                lobbyTimerTask = null
            }

            if (currentPlayers >= this.minPlayers) {
                if (this.lobbyTimerTask != null) return@runTaskTimer
                this.lobbyTimerTask = Bukkit.getScheduler().runTaskTimer(this, {
                    this.lobbyTime--
                    Bukkit.getOnlinePlayers().forEach { player -> player.level = this.lobbyTime }
                    if (this.lobbyTime <= 0) {
                        this.lobbyTimerTask!!.cancel()
                        this.playerCheckTask!!.cancel()
                        this.startGame()
                    }
                }, 0, 20)
            }

            if (currentPlayers >= this.maxPlayers) {
                this.lobbyTime = 10
                Bukkit.broadcastMessage("Zeit wird auf $lobbyTime Sekunden verkürzt.")
            }
        }, 20L, 20L)
    }

    override fun onEnable() {
        this.state = GameState.INIT
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
        this.mapConfig = map
        this.mapVoting.end()

        val world = this.loadMap(map)
        this.scoreboard.startDisplay()

        Bukkit.getOnlinePlayers().forEach {
            val location = MapConfig.locFromString(map.spawnString, world)
            this.scoreboard.show(it)
            it.initGameData(map.speedLevel, location!!)
            it.gameMode = GameMode.ADVENTURE
            it.isFlying = false
            it.allowFlight = false
            it.applyEffects()
            it.giveItems()
            it.teleport(location)
        }

        this.startTime = System.currentTimeMillis()

        this.state = GameState.RUNNING
        this.gamerTimerTask = Bukkit.getScheduler().runTaskTimer(this, {
            roundTime--
            val format = String.format("%02d:%02d", roundTime / 60, roundTime % 60)
            this.scoreboard.updateTitle(format)

            if (roundTime <= 0) {
                this.stop()
                this.gamerTimerTask!!.cancel()
                return@runTaskTimer
            }

            if (Bukkit.getOnlinePlayers().count { it.isIngame() } <= 0) {
                this.stop()
                this.gamerTimerTask!!.cancel()
                return@runTaskTimer
            }
        }, 0, 20)
    }

    private fun stop() {
        Bukkit.broadcastMessage("Der Server stoppt in 20 Sekunden.")
        this.state = GameState.FINISHING
        Bukkit.getScheduler().runTaskLater(this, {
            Bukkit.getServer().shutdown()
        }, 20 * 20)
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
        player.applyEffects()

        val index = player.getCurrentCheckPointIndex()
        player.playSound(player.location, Sound.LEVEL_UP, 1.0F, 1.0F)
        player.sendMessage("$PREFIX §7Du hast einen Checkpoint erreicht! §b[${index}/${mapConfig?.checkpoints}]")

        Bukkit.getOnlinePlayers()
            .filter { it != player }
            .forEach { it.sendMessage("$PREFIX §7Der Spieler §a${player.name} §7hat den §b${index}. §7Checkpoint erreicht.") }

        spawnRandomFirework(this, player.location.clone())
    }

    @EventHandler
    private fun onPlayerFinished(event: PlayerFinishedEvent) {
        val player = event.player
        player.gameMode = GameMode.SPECTATOR

        val needed = event.finished - startTime
        val minutes = needed / (1000 * 60)
        val seconds = needed / 1000 % 60
        val millis = needed % 1000

        val formatted = String.format("%02d:%02d.%03d", minutes, seconds, millis)
        player.sendMessage("$PREFIX §7Du hast insgesamt §b$formatted §7benötigt.")

        Bukkit.broadcastMessage("$PREFIX §a${event.player.name} §bhat das Ziel erreicht.")
        Bukkit.getOnlinePlayers().forEach { it.playSound(it.location, Sound.ENDERDRAGON_GROWL, 1f, 1f) }
    }

    @EventHandler
    private fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = false
    }
}

