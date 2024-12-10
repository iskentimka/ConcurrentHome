import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Sheep implements Runnable, CellType {
    private final char name;
    private int x, y;
    private final Farm farm;
    private final Random random = new Random();

    public Sheep(int x, int y, Farm farm, char name) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.farm = farm;
    }

    @Override
    public void run() {
        while (farm.isSimRunning()) {
            int[] newPos = getNextMove();
            if (!farm.moveAnimal(x, y, newPos[0], newPos[1], this)) {
                if (!farm.isSimRunning())
                    break;
                else
                    continue;
            }
            x = newPos[0];
            y = newPos[1];
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private int[] getNextMove() {
        int[][] directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
        List<int[]> safeMoves = new ArrayList<>();
        List<int[]> allValidMoves = new ArrayList<>();

        // Collect moves that are not adjacent to dogs
        for (int[] dir : directions) {
            int nx = x + dir[0], ny = y + dir[1];
            if (isValidMove(nx, ny)) {
                if (!farm.isDogNear(nx, ny)) {
                    safeMoves.add(new int[]{nx, ny});
                }
                allValidMoves.add(new int[]{nx, ny});
            }
        }

        // Prefer moves that are safe (not near dogs)
        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        // If no safe moves, pick any valid move
        if (!allValidMoves.isEmpty()) {
            return allValidMoves.get(random.nextInt(allValidMoves.size()));
        }

        // If no valid moves, stay in same place
        return new int[]{x, y};
    }

    private boolean isValidMove(int newX, int newY) {
        if (newX < 0 || newX >= farm.getGrid().length || newY < 0 || newY >= farm.getGrid()[0].length) {
            return false;
        }
        LockCell cell = farm.getGrid()[newX][newY];
        return cell.getContent() instanceof EmptyCell || cell.getContent() instanceof Gate;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}