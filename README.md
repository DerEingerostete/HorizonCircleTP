# Horizon Circle TP

A simple Folia plugin to teleport all players around the center in a circle.

### Requirements

This plugin was made for [Folia](https://papermc.io/software/folia) and more specifically [ShreddedPaper](https://github.com/MultiPaper/ShreddedPaper).
The plugin was tested with:

* Java 21
* Folia / ShreddedPaper
* Minecraft Version 1.20.6

### Build the plugin

Clone the repo and run

```shell
mvn clean package
```

## Usage

The plugin is basically just one command ``/circle-tp`` (requires the permission ``event.circle-tp``)

```html
> /circle-tp <circleX> <circleZ>
```

Where:
- **circleX** is the X coordinate of the center
- **circleZ** is the Z coordinate of the center

After executing the command all players (excluding players with the permission ``event.circle-tp.ignored``) will be teleported in a circle around the center coordinates.
The radius is calculated based on the amount of players with a minimum defined by ``TPCommand#MIN_RADIUS``.

Additionally, the plugin will generate random locations around the center (in a circle) that players will respawn on. This does not happen if the player already set their own spawnpoint.

## Additional Documentation and Acknowledgments

* Thank you to [PureGero](https://github.com/PureGero) for explaining the Folia async processing to me.
