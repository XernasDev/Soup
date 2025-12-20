package dev.xernas.gameoflife;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    private final Cell[][] grid;
    private final List<Cell> allCells = new ArrayList<>();
    private final float cellSize;

    public Grid(int width, int height, float cellSize) {
        grid = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Cell(x, y); // Initialize each cell
                allCells.add(grid[x][y]); // Populate allCells list
            }
        }
        this.cellSize = cellSize;
    }
    public Cell getCell(int x, int y) {
        if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length) {
            return null;
        }
        return grid[x][y];
    }

    public boolean isCellAlive(int x, int y) {
        Cell cell = getCell(x, y);
        return cell != null && cell.isAlive();
    }

    public void setCellState(int x, int y, boolean isAlive) {
        Cell cell = getCell(x, y);
        if (cell != null) cell.setAlive(isAlive);
    }

    public int getNumberOfAliveNeighbors(Cell cell) {
        int aliveCount = 0;
        // Loop through the 3x3 grid centered on the cell
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip the cell itself
                if (isCellAlive(cell.x + dx, cell.y + dy)) {
                    aliveCount++;
                }
            }
        }
        return aliveCount;
    }

    public void updateCells() {
        int w = grid.length;
        int h = grid[0].length;
        boolean[][] nextStates = new boolean[w][h];

        // Computing rules on a separate array to avoid interference
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Cell currentCell = grid[x][y];
                int aliveNeighbors = getNumberOfAliveNeighbors(currentCell);
                if (currentCell.isAlive()) nextStates[x][y] = (aliveNeighbors == 2 || aliveNeighbors == 3);
                else nextStates[x][y] = (aliveNeighbors == 3);
            }
        }

        // Apply the computed states
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                grid[x][y].setAlive(nextStates[x][y]);
            }
        }
    }

    public void resetGrid() {
        for (Cell[] cells : grid) for (Cell cell : cells) cell.setAlive(false);
    }

    public float getCellSize() {
        return cellSize;
    }

    public List<Cell> getAliveCells() {
        allCells.clear();
        for (Cell[] cells : grid) for (Cell cell : cells) if (cell.isAlive()) allCells.add(cell);
        return allCells;
    }

    public float getWorldWidth() {
        return grid.length * (cellSize + AppConstants.CELL_SPACING);
    }

    public float getWorldHeight() {
        return grid[0].length * (cellSize + AppConstants.CELL_SPACING);
    }

    public static class Cell {

        private final int x;
        private final int y;
        private boolean isAlive;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
            this.isAlive = false;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isAlive() {
            return isAlive;
        }

        public void setAlive(boolean alive) {
            isAlive = alive;
        }

    }

}
