package dev.versatia.example.conditions

import dev.versatia.di.LocalBean
import dev.versatia.task.PluginScope
import dev.versatia.task.condition
import dev.versatia.task.delayTicks
import dev.versatia.task.keyedJobs
import dev.versatia.task.recheckOn
import dev.versatia.task.runOnceWhile
import dev.versatia.util.world.isWithin
import java.util.UUID
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * Right-clicking with a compass teleports to the world spawn after five seconds of standing
 * still. The condition is checked again only when the player moves, is teleported or quits, never
 * by polling, and its listeners go when the request ends.
 */
@LocalBean
class Teleports(scope: PluginScope) : Listener {
    private val pending = scope.keyedJobs<UUID>()

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (!event.action.isRightClick || event.item?.type != Material.COMPASS) return
        val player = event.player
        val start = player.location
        pending.launch(player.uniqueId) {
            val stays = condition(
                recheckOn<PlayerMoveEvent>(player),
                recheckOn<PlayerTeleportEvent>(player),
                recheckOn<PlayerQuitEvent>(player),
            ) {
                player.isOnline && player.location.isWithin(1.5, start)
            }
            val stayed = runOnceWhile(stays) {
                for (left in 5 downTo 1) {
                    player.sendActionBar(text("Teleporting in ").append(text("$left s", NamedTextColor.AQUA)).append(text(", stand still")))
                    delayTicks(20)
                }
            }
            when {
                stayed -> player.teleport(player.world.spawnLocation)
                player.isOnline -> player.sendMessage(text("Teleport cancelled: you moved", NamedTextColor.RED))
            }
        }
    }
}
