import java.util.Random;

public class Dog implements Runnable, CellType {
    private final int id;
    private int x, y;
    private final Random random = new Random();
    private final Farm farm;

    public Dog(int x, int y, Farm farm, int id) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.farm = farm;
    }

    @Override
    public void run() {
        while (farm.isSimRunning()) {
            int[] newPos = getNextMove();
            if (farm.moveAnimal(x, y, newPos[0], newPos[1], this)) {
                x = newPos[0];
                y = newPos[1];
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private int[] getNextMove() {
        int[][] directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
        int[] dir = directions[random.nextInt(directions.length)];
        int newX = x + dir[0], newY = y + dir[1];
        // Check valid moves
        if (isValidMove(newX, newY)) {
            return new int[]{newX, newY};
        }
        // If no valid move found, stay in place
        return new int[]{x, y};
    }

    private boolean isValidMove(int newX, int newY) {
        if (newX < 0 || newX >= farm.getGrid().length || newY < 0 || newY >= farm.getGrid()[0].length) {
            return false;
        }
        if (farm.isInMidZone(newX, newY)) {
            return false;
        }
        LockCell cell = farm.getGrid()[newX][newY];
        return cell.getContent() instanceof EmptyCell;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}