package cloud.luxor.lbwl.flash

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


data class MapConfig(
    val name: String = "null",
    val checkpoints: Int,
    val builder: String = "",
    val time: Int,
    val mode: String = "easy",
    val speedLevel: Int = 19,
    val item: Material = Material.STONE,
    val spawnString: String = "0,0,0,0"
) {
    companion object {
        fun read(file: File): MapConfig {
            val yaml = YamlConfiguration.loadConfiguration(file)
            return MapConfig(
                yaml.getString("name") ?: "null",
                yaml.getInt("checkpoints"),
                yaml.getString("author").orEmpty(),
                yaml.getInt("time"),
                yaml.getString("mode") ?: "easy",
                yaml.getInt("speedLevel"),
                Material.valueOf(yaml.getString("item")?.uppercase() ?: "STONE"),
                yaml.getString("spawn") ?: "0,0,0,0"
            )
        }

        // stupid legacy location format
        fun locFromString(rawLocation: String, world: World): Location? {
            val pos = rawLocation
                .split(",")
                .map { it.toDouble() }
            if (pos.size < 3)
                return null
            val location = Location(world, pos[0], pos[1], pos[2])
            location.yaw = pos
                .getOrElse(3) { 0 }
                .toFloat() * 90
            return location
        }
    }
}
