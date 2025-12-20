package dev.xernas.gameoflife;

import dev.xernas.microscope.helper.PathHelper;
import dev.xernas.photon.PhotonAPI;
import dev.xernas.photon.api.IRenderer;
import dev.xernas.photon.api.Transform;
import dev.xernas.photon.api.framebuffer.IFramebuffer;
import dev.xernas.photon.api.model.IMesh;
import dev.xernas.photon.api.model.Model;
import dev.xernas.photon.api.shader.IShader;
import dev.xernas.photon.api.shader.Shader;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.api.window.Window;
import dev.xernas.photon.api.window.cursor.CursorShape;
import dev.xernas.photon.api.window.input.Key;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.utils.MatrixUtils;
import dev.xernas.photon.utils.Models;
import dev.xernas.photon.utils.ShaderResource;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class GameOfLife {

    private final Window window;
    private final Grid grid;
    private final IRenderer<IFramebuffer, IShader, IMesh, ITexture> renderer;
    private final Transform.CameraTransform camera;

    private IShader shader;
    private IMesh cellMesh;
    private Vector2f cameraStartPos;
    private Vector2f cameraDeltaDir;

    private boolean running;
    private boolean paused;
    private float fps;
    private int simulationSpeed = AppConstants.DEFAULT_SIMULATION_SPEED;

    public GameOfLife(Window window, Grid grid) {
        this.window = window;
        this.grid = grid;
        this.renderer = PhotonAPI.getRenderer(window, false);
        this.camera = new Transform.CameraTransform();
        this.cameraStartPos = new Vector2f();
        this.cameraDeltaDir = new Vector2f();
    }

    public void run() throws PhotonException {
        start();

        float deltaTime;
        float accumulator = 0f;
        int frameCount = 0;
        float fpsTimer = 0f;
        while (running) {
            long startTime = System.nanoTime();

            // Window
            window.update(renderer);
            if (!window.isOpen()) running = false;
            window.setTitle(window.getDefaultTitle() + " | FPS: " + Math.round(fps) + " | Simulation speed: " + simulationSpeed + (paused ? " | (Paused)" : ""));

            // Input
            input();

            // Camera movement
            camera.move(new Vector3f(-cameraDeltaDir.x, -cameraDeltaDir.y, 0));
            cameraDeltaDir.set(0);

            // Rendering
            renderer.clear(Color.DARK_GRAY);
            List<Grid.Cell> cells = grid.getAllCells();
            for (Grid.Cell cell : cells) {
                renderer.render(shader, cellMesh, (m, s) -> {
                    s.setUniform("projectionMatrix", MatrixUtils.createOrthoMatrix(window));
                    Vector3f position = new Vector3f(cell.getX() * (grid.getCellSize() + AppConstants.CELL_SPACING), cell.getY() * (grid.getCellSize() + AppConstants.CELL_SPACING), 0);
                    s.setUniform("modelMatrix", MatrixUtils.createTransformationMatrix(new Transform(position).scale(grid.getCellSize())));
                    s.setUniform("viewMatrix", MatrixUtils.create2DViewMatrix(camera));
                    s.setUniform("isAlive", cell.isAlive());
                });
            }

            // Timers
            if (accumulator >= (float) 1 / simulationSpeed) {
                if (!paused) grid.updateCells();
                accumulator = 0f;
            }
            if (fpsTimer >= 1f) {
                fps = frameCount / fpsTimer;
                fpsTimer = 0f;
                frameCount = 0;
            }

            // Time measurements
            frameCount++;
            long endTime = System.nanoTime();
            long frameTime = endTime - startTime;
            deltaTime = frameTime / (float) AppConstants.SECOND_IN_NANOS;
            accumulator += deltaTime;
            fpsTimer += deltaTime;
        }

        clean();
    }

    private void input() throws PhotonException {
        // Managing camera movement with middle mouse button
        if (window.getInput().isPressing(Key.MOUSE_MIDDLE)) {
            window.setCursorShape(CursorShape.HAND);
            if (cameraStartPos == null) {
                cameraStartPos = window.getInput().getMousePosition().toWorldSpace(window);
            } else {
                Vector2f currentMousePos = window.getInput().getMousePosition().toWorldSpace(window);
                cameraDeltaDir = new Vector2f(currentMousePos).sub(cameraStartPos);
                cameraStartPos.set(currentMousePos);
            }
        } else {
            window.setCursorShape(CursorShape.ARROW);
            cameraStartPos = null;
        }
        // Managing cell birth with left mouse button
        if (window.getInput().isPressing(Key.MOUSE_LEFT)) {
            paused = true;
            Vector2f mouseWorldPos = getMouseCameraPos();
            int cellX = (int) (mouseWorldPos.x / (grid.getCellSize() + AppConstants.CELL_SPACING));
            int cellY = (int) (mouseWorldPos.y / (grid.getCellSize() + AppConstants.CELL_SPACING));
            grid.setCellState(cellX, cellY, true);
        }
        // Managing cell death with right mouse button
        if (window.getInput().isPressing(Key.MOUSE_RIGHT)) {
            paused = true;
            Vector2f mouseWorldPos = getMouseCameraPos();
            int cellX = (int) (mouseWorldPos.x / (grid.getCellSize() + AppConstants.CELL_SPACING));
            int cellY = (int) (mouseWorldPos.y / (grid.getCellSize() + AppConstants.CELL_SPACING));
            grid.setCellState(cellX, cellY, false);
        }
        // Toggling pause with space key
        if (window.getInput().hasPressed(Key.KEY_SPACE) || window.getInput().hasPressed(Key.KEY_ENTER)) {
            paused = !paused;
        }
        // Accelerating simulation speed
        if (window.getInput().hasPressed(Key.KEY_ARROW_UP) || window.getInput().hasHold(Key.KEY_ARROW_UP)) {
            paused = false;
            simulationSpeed++;
        }
        // Decelerating simulation speed
        if (window.getInput().hasPressed(Key.KEY_ARROW_DOWN) || window.getInput().hasHold(Key.KEY_ARROW_DOWN)) {
            paused = false;
            simulationSpeed = Math.max(1, simulationSpeed - 1);
        }
        // Resetting simulation
        if (window.getInput().hasHold(Key.KEY_R)) {
            paused = true;
            grid.resetGrid();
        }
    }

    private void start() throws PhotonException {
        window.start();
        renderer.start();

        Shader appShader = getShaderFromResources();
        Model cellModel = Models.createQuad();
        shader = renderer.loadShader(appShader);
        cellMesh = renderer.loadMesh(cellModel);

        window.show();
        running = true;
    }

    private void clean() throws PhotonException {
        renderer.dispose();
        window.dispose();
    }

    private Vector2f getMouseCameraPos() {
        // Get mouse position in world space CONSIDERING CAMERA POSITION
        Vector2f mousePos = window.getInput().getMousePosition().toWorldSpace(window);
        mousePos.add(new Vector2f(camera.getPosition().x, camera.getPosition().y));
        return mousePos;
    }

    private Shader getShaderFromResources() throws PhotonException {
        try {
            Path vertexShaderPath = PathHelper.getResourcePath(AppConstants.VERTEX_SHADER_PATH);
            Path fragmentShaderPath = PathHelper.getResourcePath(AppConstants.FRAGMENT_SHADER_PATH);
            String vertexSource = PathHelper.getStringOf(vertexShaderPath);
            String fragmentSource = PathHelper.getStringOf(fragmentShaderPath);
            ShaderResource vertexResource = new ShaderResource(vertexShaderPath.getFileName().toString(), vertexSource);
            ShaderResource fragmentResource = new ShaderResource(fragmentShaderPath.getFileName().toString(), fragmentSource);
            return new Shader(vertexResource, fragmentResource);
        } catch (IOException | URISyntaxException e) {
            throw new PhotonException("Failed to load shader files from resources", e);
        }
    }

}
