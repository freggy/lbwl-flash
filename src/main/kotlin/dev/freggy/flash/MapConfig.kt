package dev.freggy.flash

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
    val speedLevel: Int,
    val item: Material,
    val spawnString: String
) {
    companion object {
        fun read(file: File): MapConfig {
            val yaml = YamlConfiguration.loadConfiguration(file)
            return MapConfig(
                yaml.getString("name"),
                yaml.getInt("checkpoints"),
                yaml.getString("author").orEmpty(),
                yaml.getInt("time"),
                yaml.getInt("speedLevel"),
                Material.valueOf(yaml.getString("item")?.toUpperCase() ?: "STONE"),
                yaml.getString("spawn")
            )
        }

        // stupid legacy location format
        fun locFromString(s: String, world: World): Location? {
            val split = s.split(",")
            val values = IntArray(split.size)
            for (i in split.indices) values[i] = Integer.valueOf(split[i])
            if (values.size >= 3) {
                val location =
                    Location(world, values[0].toDouble(), values[1].toDouble(), values[2].toDouble())
                if (values.size >= 4) {
                    location.yaw = values[3] * 90.0f
                }
                return location
            }
            return null
        }
    }
}
