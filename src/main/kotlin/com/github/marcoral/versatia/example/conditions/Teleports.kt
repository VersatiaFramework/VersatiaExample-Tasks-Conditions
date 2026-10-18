package com.github.marcoral.versatia.example.conditions

import com.github.marcoral.versatia.di.LocalBean
import com.github.marcoral.versatia.task.PluginScope
import com.github.marcoral.versatia.task.condition
import com.github.marcoral.versatia.task.delayTicks
import com.github.marcoral.versatia.task.keyedJobs
import com.github.marcoral.versatia.task.recheckOn
import com.github.marcoral.versatia.task.runOnceWhile
import com.github.marcoral.versatia.util.world.isWithin
import java.util.UUID
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Right-clicking with a compass teleports to the world spawn after five seconds of standing
 * still. The condition is checked again only when the player moves or quits, never by polling,
 * and its listeners go when the request ends.
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
            val stays = condition(recheckOn<PlayerMoveEvent>(player), recheckOn<PlayerQuitEvent>(player)) {
                player.isOnline && player.location.isWithin(1.5, start)
            }
            val stayed = runOnceWhile(stays) {
                for (left in 5 downTo 1) {
                    player.sendActionBar(text("Teleporting in $left s, stand still"))
                    delayTicks(20)
                }
            }
            when {
                stayed -> player.teleport(player.world.spawnLocation)
                player.isOnline -> player.sendMessage(text("Teleport cancelled: you moved"))
            }
        }
    }
}
