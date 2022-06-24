package cloud.luxor.lbwl.flash

import net.kyori.adventure.text.Component
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
@Suppress("unused")
class MapVoting(private val maps: List<MapConfig>) : Listener {

    private val votes = HashMap<String, Int>()
    private val playerVotes = HashMap<Player, String>()
    private val inventory: Inventory = Bukkit.createInventory(null, 5 * 9, Component.text("Maps"))

    private fun updateInventory() {
        // Probably pretty inefficient for sorting by difficulty, but it does the job and 
        // efficiency doesn't really matter in this context.
        // Maps with easy difficulty should be listed first ('e' before 'h') 
        val sortedMaps = this.maps.sortedWith(compareBy { it.mode.first() })
        sortedMaps.forEachIndexed { index, mapConfig ->
            val stack = ItemStack(mapConfig.item, votes[mapConfig.name] ?: 1)   //0 is forbidden in 1.19
            stack.itemMeta.displayName(Component.text(mapConfig.name))  //this does not work properly
            //stack.itemMeta.displayName(Component.text(if (mapConfig.mode == "easy") "§a${mapConfig.name}" else "§c${mapConfig.name}"))
            stack.itemMeta.lore(listOf(Component.text("§eErbauer: §b${mapConfig.builder}")))
            inventory.setItem(index, stack)
            println("[Flash] Debug: Set ${stack.type.name} on index $index")
            println("[Flash] Debug was considered item: ${mapConfig.item.isItem}")
        }
        println("[Flash] Debug: " + inventory.contents[0]?.type?.name + " is empty? " + inventory.isEmpty)
    }

    init {
        this.updateInventory()
    }

    private fun vote(player: Player, name: String) {
        if (this.playerVotes.containsKey(player)) {
            this.removeVote(player)
        }

        this.playerVotes[player] = name

        if (!this.votes.containsKey(name)) this.votes[name] = 1

        this.votes[name] = this.votes[name]!!.plus(1)

        this.updateInventory()
    }

    private fun removeVote(player: Player) {
        if (!this.playerVotes.containsKey(player)) return
        val map = this.playerVotes[player]!!
        this.votes[map] = this.votes[map]!!.minus(1)
        this.updateInventory()
    }

    fun open(player: Player) {
        player.openInventory(this.inventory)
    }

    fun determineMap(): MapConfig {
        // choose random map if no-one has voted
        if (this.votes.values.sum() == 0) return this.maps.shuffled()[0]

        // find the map with the highest votes
        val name = this.votes.toList().maxBy { (_, value) -> value }
        return this.maps.find { config -> name.first == config.name }!!
    }

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory == null) return
        //if((event.clickedInventory?.holder as Container).name != "Text")
        if (event.currentItem == null) return
        if (event.currentItem!!.itemMeta == null) return

        event.isCancelled = true
        val mapName = ChatColor.stripColor(
            Component.text().append(event.currentItem?.itemMeta?.displayName() ?: Component.text("")).content()
        )
        mapName?.let { this.vote(event.whoClicked as Player, it) }
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
