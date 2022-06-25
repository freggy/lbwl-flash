package cloud.luxor.lbwl.flash

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class MapConfig(
    val name: String,
    val checkpoints: Int,
    val builder: String,
    val time: Int,
    val mode: String,
    val speedLevel: Int,
    val item: Material,
    val spawnString: String
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
        fun locFromString(s: String, world: World): Location? {
            val split = s.split(",")
            val values = DoubleArray(split.size)
            for (i in split.indices) values[i] = split[i].toDouble()
            if (values.size >= 3) {
                val location =
                    Location(world, values[0], values[1], values[2])
                if (values.size >= 4) {
                    location.yaw = values[3].toFloat() * 90.0f
                }
                return location
            }
            return null
        }
    }
}
