# Project Guidelines

## Git Commits

- Never mention Claude or add Co-Authored-By lines in commit messages
- Follow the existing commit message style: `chore:`, `fix:`, etc.
- Version compatibility commits use: `chore: neoforge {version} compatibility`

## Releasing a New Minecraft Version

### 1. Find the right versions

Check the official NeoForge MDK for the target version:
`https://github.com/NeoForgeMDKs/MDK-{version}-ModDevGradle/blob/main/gradle.properties`

Key properties to update in `gradle.properties`:
- `minecraft_version`, `minecraft_version_range`
- `neo_version`, `neo_version_range`
- `parchment_minecraft_version`, `parchment_mappings_version`
- `loader_version_range`
- `mod_version`

Also check the MDK's `build.gradle` (ModDevGradle plugin version) and `gradle/wrapper/gradle-wrapper.properties` (Gradle version).

### 2. Check for breaking changes

Migration primers are at: `https://docs.neoforged.net/primer/docs/{version}/`

Known breaking changes:
- **1.21.10+**: `@EventBusSubscriber` no longer has `bus` parameter. Remove `bus = EventBusSubscriber.Bus.MOD`. Events are auto-routed to the correct bus.
- **1.21.10+**: Requires Gradle 9.x and ModDevGradle 2.x (was Gradle 8.x and ModDevGradle 1.x)
- **1.21.1**: Uses `loader_version_range=[1,)` instead of `[4,)`

### 3. GitHub Release

Format matches previous releases:
- Tag and title: version number (e.g. `1.21.10`)
- Body: `**Full Changelog**: https://github.com/jblemee/BrokenTool/commits/{version}`
- Attach the JAR file

```bash
gh release create {version} build/libs/brokentoolmod-{version}.jar --title "{version}" --notes "**Full Changelog**: https://github.com/jblemee/BrokenTool/commits/{version}"
```
