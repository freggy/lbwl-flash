package dev.freggy.flash

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin


/**
 * @author Yannic Rieger
 */
class MapVoting(private val maps: List<MapConfig>) : Listener {

    private val votes = HashMap<String, Int>()
    private val playerVotes = HashMap<Player, String>()
    private val inventory: Inventory = Bukkit.createInventory(null, 5 * 9, "Maps")

    private fun updateInventory() {
        for (i in this.maps.indices) {
            val map = this.maps[i]
            val votes = votes[map.name] ?: 0
            val stack = ItemStack(map.item, votes, 0)
            val meta = stack.itemMeta
            meta.displayName = if (map.mode == "easy") "§a${map.name}" else "§c${map.name}"
            meta.lore = listOf("", "§eErbauer: §b${map.builder}")
            stack.itemMeta = meta
            this.inventory.setItem(i, stack)
        }
    }

    init {
        this.updateInventory()
    }

    fun vote(player: Player, name: String) {
        if (this.playerVotes.containsKey(player)) {
            this.removeVote(player)
        }

        this.playerVotes[player] = name

        if (!this.votes.containsKey(name)) this.votes[name] = 0

        this.votes[name] = this.votes[name]!!.plus(1)

        this.updateInventory()
    }

    fun removeVote(player: Player) {
        if (!this.playerVotes.containsKey(player)) return
        val map = this.playerVotes[player]!!
        this.votes[map] = this.votes[map]!!.minus(1)
        this.updateInventory()
    }

    fun open(player: Player) {
        player.openInventory(this.inventory)
    }

    fun determineMap(): MapConfig {
        val name = this.votes.toList().maxBy { (_, value) -> value }!!
        return this.maps.find { config -> name.first == config.name }!!
    }

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory.name != "Maps") return
        if (event.currentItem == null) return
        if (event.currentItem.itemMeta == null) return

        event.isCancelled = true
        val name = ChatColor.stripColor(event.currentItem.itemMeta.displayName)
        this.vote(event.whoClicked as Player, name)
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        this.removeVote(event.player)
    }

    fun registerListeners(plugin: Plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun end() {
        this.votes.clear()
        this.playerVotes.clear()
        InventoryClickEvent.getHandlerList().unregister(this)
    }
}
