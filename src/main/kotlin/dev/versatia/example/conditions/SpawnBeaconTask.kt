package dev.versatia.example.conditions

import dev.versatia.VersatiaEngine
import dev.versatia.onlinePlayers
import dev.versatia.task.BackgroundTask
import dev.versatia.task.condition
import dev.versatia.task.everyTicks
import dev.versatia.task.recheckOn
import dev.versatia.task.whenever
import dev.versatia.util.world.spawnParticle
import java.util.logging.Logger
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
class SpawnBeaconTask(private val logger: Logger) : BackgroundTask {
    override suspend fun run() {
        val anyoneOnline = condition(recheckOn<PlayerJoinEvent>(immediately = true), recheckOn<PlayerQuitEvent>()) {
            VersatiaEngine.onlinePlayers.isNotEmpty()
        }
        whenever(anyoneOnline) {
            logger.info("Beacon on")
            try {
                everyTicks(10, delayTicks = 0) {
                    val spawn = Bukkit.getWorlds().first().spawnLocation.add(0.0, 1.0, 0.0)
                    spawn.spawnParticle(Particle.HAPPY_VILLAGER, count = 10, offsetX = 0.3, offsetY = 1.0, offsetZ = 0.3)
                }
            } finally {
                logger.info("Beacon off")
            }
        }
    }
}
