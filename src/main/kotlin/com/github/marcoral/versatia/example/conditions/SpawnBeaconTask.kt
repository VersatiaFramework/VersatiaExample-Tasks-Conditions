package com.github.marcoral.versatia.example.conditions

import com.github.marcoral.versatia.VersatiaEngine
import com.github.marcoral.versatia.onlinePlayers
import com.github.marcoral.versatia.task.BackgroundTask
import com.github.marcoral.versatia.task.condition
import com.github.marcoral.versatia.task.everyTicks
import com.github.marcoral.versatia.task.recheckOn
import com.github.marcoral.versatia.task.whenever
import com.github.marcoral.versatia.util.world.spawnParticle
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Particles above the spawn of the main world while anyone is online: `whenever` starts the
 * loop when the condition turns true and cancels it when it turns false. A join is checked at
 * once, a quit at the next tick, when the leaving player is no longer online. The condition
 * belongs to the run, so its listeners go when a reload or the stop cancels it.
 */
class SpawnBeaconTask(private val plugin: ConditionsPlugin) : BackgroundTask {
    override suspend fun run() {
        val anyoneOnline = condition(recheckOn<PlayerJoinEvent>(immediately = true), recheckOn<PlayerQuitEvent>()) {
            VersatiaEngine.onlinePlayers.isNotEmpty()
        }
        whenever(anyoneOnline) {
            plugin.logger.info("Beacon on")
            try {
                everyTicks(10, delayTicks = 0) {
                    val spawn = Bukkit.getWorlds().first().spawnLocation.add(0.0, 1.0, 0.0)
                    spawn.spawnParticle(Particle.HAPPY_VILLAGER, count = 10, offsetX = 0.3, offsetY = 1.0, offsetZ = 0.3)
                }
            } finally {
                plugin.logger.info("Beacon off")
            }
        }
    }
}
