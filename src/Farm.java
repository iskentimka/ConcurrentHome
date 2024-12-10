import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Farm {
    private static final int DEFAULT_DIM = 14;
    private final int length;
    private final int width;
    private final LockCell[][] grid;
    private volatile boolean simRunning = true;
    private final List<int[]> gates = new ArrayList<>();
    private final List<Thread> animalThreads = new ArrayList<>();


    public Farm() {
        this(DEFAULT_DIM, DEFAULT_DIM, 10, 5);
    }

    public Farm(int length, int width, int numSheep, int numDogs) {
        if (length % 3 != 2 || width % 3 != 2) {
            throw new IllegalArgumentException("Length and width must be multiples of three plus two.");
        }
        this.length = length;
        this.width = width;
        this.grid = new LockCell[length][width];
        initFarm();
        addGates();
        addAnimal(Sheep.class, numSheep);
        addAnimal(Dog.class, numDogs);
    }

    private void initFarm() {
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = new LockCell((i == 0 || i == length - 1 || j == 0 || j == width - 1) ? new Wall() : new EmptyCell());
            }
        }
    }

    private void addAnimal(Object type, int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int x, y;
            do {
                x = random.nextInt(length - 6) + 3;
                y = random.nextInt(width - 6) + 3;
            } while (!(grid[x][y].getContent() instanceof EmptyCell));
            if (type == Sheep.class) {
                // Adding sheep with name
                Sheep sheep = new Sheep(x, y, this, (char) ('A' + i));
                grid[x][y] = new LockCell(sheep);
                Thread sheepThread = new Thread(sheep);
                animalThreads.add(sheepThread);
                sheepThread.start();
            } else if (type == Dog.class) {
                // Adding dog with num
                Dog dog = new Dog(x, y, this, i + 1);
                grid[x][y] = new LockCell(dog);
                Thread dogThread = new Thread(dog);
                animalThreads.add(dogThread);
                dogThread.start();
            }
        }
    }

    private void addGates() {
        Random random = new Random();
        for (int i = 0; i < 2; i++) {
            int row = (i == 0) ? 0 : length - 1;
            int col = random.nextInt(width - 2) + 1;
            grid[row][col] = new LockCell(new Gate());
            gates.add(new int[]{row, col});
        }
        for (int i = 0; i < 2; i++) {
            int col = (i == 0) ? 0 : width - 1;
            int row = random.nextInt(length - 2) + 1;
            grid[row][col] = new LockCell(new Gate());
            gates.add(new int[]{row, col});
        }
    }

    public synchronized void printFarm() {
        // Clear the screen
        System.out.print("\033[H\033[2J");
        System.out.print("\u001B[0;0H");

        // Print column headers
        System.out.print("    ");
        for (int col = 0; col < width; col++) {
            System.out.printf("%2d ", col);
        }
        System.out.println();

        for (int row = 0; row < length; row++) {
            System.out.printf("%2d |", row); // Print row number with a separator
            for (LockCell cell : grid[row]) {
                System.out.printf(" %s ", cell.getContent());
            }
            System.out.println();
        }
    }

    public void startRun() {
        new Thread(() -> {
            while (simRunning) {
                printFarm();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public synchronized boolean moveAnimal(int x, int y, int newX, int newY, CellType animal) {
        // Check if newX and newY are within bounds. It is possible only in case of the sheep.
        if (newX < 0 || newX >= grid.length || newY < 0 || newY >= grid[0].length) {
            return false;
        }

        // If the animal didn't move
        if (newX == x && newY == y) {
            return true;
        }

        LockCell currCell = grid[x][y];
        LockCell nextCell = grid[newX][newY];

        // If the target cell is not EmptyCell or Gate
        if (!(nextCell.getContent() instanceof EmptyCell) && !(nextCell.getContent() instanceof Gate)) {
            return false;
        }

        // Determine lock order to avoid deadlocks
        // First lock cell would be such cell, which would be met firstly in grid.
        LockCell firstLock = (x < newX || (x == newX && y < newY)) ? currCell : nextCell;
        LockCell secLock = (firstLock == currCell) ? nextCell : currCell;

        firstLock.getLock().lock(); // The first lock is acquired. At this point, only this thread can modify the resource firstLock.
        try {
            secLock.getLock().lock(); // Once the first lock is held, the thread attempts to acquire the second lock (secLock).
            try {
                // If the target cell is a Gate and the animal is a Sheep
                if (nextCell.getContent() instanceof Gate && animal instanceof Sheep) {
                    simRunning = false; // stop simulation
                    System.out.println("Sheep " + animal + " escaped through gate at (" + newX + ", " + newY + ")");
                    return false;
                }

                // Move the animal
                nextCell.setContent(animal);
                currCell.setContent(new EmptyCell());
                return true;
            } finally {
                secLock.getLock().unlock();
            }
        } finally {
            firstLock.getLock().unlock();
        }
    }

    public synchronized LockCell[][] getGrid() {
        return grid;
    }

    public boolean isSimRunning() {
        return simRunning;
    }

    public boolean isInMidZone(int x, int y) {
        int mxs = length / 3, mxe = 2 * length / 3;
        int mys= width / 3,  mye = 2 * width / 3;
        return x >= mxs && x < mxe && y >= mys && y < mye;
    }

    public boolean isDogNear(int x, int y) {
        // If the position itself is out of bounds, return false
        if (x < 0 || y < 0 || x >= length || y >= width) {
            return false;
        }

        int[][] directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
        for (int[] dir : directions) {
            int nx = x + dir[0], ny = y + dir[1];
            if (nx >= 0 && ny >= 0 && nx < length && ny < width) {
                LockCell neighbor = grid[nx][ny];
                neighbor.getLock().lock();
                try {
                    if (neighbor.getContent() instanceof Dog) {
                        return true;
                    }
                } finally {
                    neighbor.getLock().unlock();
                }
            }
        }
        return false;
    }
}
