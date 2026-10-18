# VersatiaExample-Tasks-Conditions

A `Condition` holds the last result of a check, evaluated again only when one of its events fires, never by polling: here a teleport that waits for the player to stand still for five seconds, and a background task that runs only while someone is online.

Part of the [Versatia examples](https://github.com/VersatiaFramework/VersatiaExamples); documentation: [Conditions](https://versatiaframework.github.io/VersatiaDocs/docs/tasks#conditions).

**Start with** [`Teleports.kt`](src/main/kotlin/com/github/marcoral/versatia/example/conditions/Teleports.kt), then [`SpawnBeaconTask.kt`](src/main/kotlin/com/github/marcoral/versatia/example/conditions/SpawnBeaconTask.kt).

## What the log shows

```
(a player joins the empty server) [VersatiaExample-Tasks-Conditions] Beacon on
(the last player leaves) [VersatiaExample-Tasks-Conditions] Beacon off
```

While the beacon is on, green sparks appear above the spawn of the main world. A join is checked at
once (`immediately = true`), because `PlayerJoinEvent` fires after the player is added; a quit is
checked at the next tick, the default, because the leaving player is still online while
`PlayerQuitEvent` runs. Right-clicking with a compass shows `Teleporting in 5 s, stand still` above
the hotbar, counting down to the teleport.

## Things to try

- Right-click with a compass and stand still: the countdown runs from 5 to 1 and you land at the
  world spawn. Turning your head fires `PlayerMoveEvent` too, but the check compares positions only.
- Walk away during the countdown: `Teleport cancelled: you moved` as soon as you are more than 1.5
  blocks from where you started; `runOnceWhile` cancelled the countdown and returned false.
- Leave the server mid-countdown: the request ends without a message, and the listeners of its
  condition go with its coroutine.
- Right-click again during a countdown: it starts over from 5, because the new request replaced the
  old one in `KeyedJobs`.
- Leave the server empty and rejoin: `Beacon off`, then `Beacon on`. `whenever` cancelled the
  particle loop, running its `finally`, and started it again when the condition turned true.
- In `SpawnBeaconTask`, write `recheckOn<PlayerQuitEvent>(immediately = true)` and leave the server empty:
  the beacon stays on, since the check ran while you were still online.

## Building

Requires JDK 21 and VersatiaStarter with what it exports (VersatiaAPI, the module APIs) and
VersatiaCatalog, which supplies every version, in your local Maven repository (`./gradlew publishToMavenLocal` in each checkout; see
[Building the framework](https://versatiaframework.github.io/VersatiaDocs/docs/contributing/building));
running it requires a Paper 1.21.4 server with VersatiaCore installed.

```bash
./gradlew build
```

To have the JAR copied to your server after every build, copy `local.properties.example` to
`local.properties` and set `pluginsDir`. The generated descriptor is in
`build/generated/ksp/main/kotlin`; open it to see exactly what the runtime executes.

The Versatia version is `versatiaVersion` in `gradle.properties`; `settings.gradle.kts` imports
the matching [VersatiaCatalog](https://versatiaframework.github.io/VersatiaDocs/docs/getting-started/installation#versatia-catalog),
so the build script writes `libs.versatia.starter` and `alias(libs.plugins.ksp)` instead of version
numbers.
