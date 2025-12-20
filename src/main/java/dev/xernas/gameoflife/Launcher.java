package dev.xernas.gameoflife;

import dev.xernas.photon.Library;
import dev.xernas.photon.PhotonAPI;
import dev.xernas.photon.api.window.Window;
import dev.xernas.photon.exceptions.PhotonException;

public class Launcher {

    public static void main(String[] args) throws PhotonException {
        PhotonAPI.init(Library.OPENGL_4_6, "GameOfLife", "1.0.0", false);

        Window window = new Window("Game Of Life", 800, 600);
        Grid grid = new Grid(1000, 1000, 0.02f);

        GameOfLife app = new GameOfLife(window, grid);
        app.run();
    }

}