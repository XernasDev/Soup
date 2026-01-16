# Soup
A classic implementation of the Game Of Life.
Except for the fact that all librairies with the exception of LWJGL are mine

## Important commands
- Left Click to make cells alive
- Right Click to make cells dead
- Middle click to move around
- Scroll to zoom in/out
- Space/Enter to pause/resume the simulation
- Arrow Up/Down to increase/decrease simulation speed
- Arrow Left/Right to adjust cell spacing
- S to step one generation when paused. If not paused, it will pause first
- Hold R to kill all cells

## Download
Just download the last release and run it with Java 21, it should be fine and please don't hesitate to report any bugs !

## Build
You can simply build the project using Maven :
```bash
mvn clean package
```
This will generate a runnable jar in the target folder (using Java 21) !
