public interface CellType {
    @Override
    String toString();
}

class Wall implements CellType {
    @Override
    public String toString() {
        return "#";
    }
}

class Gate implements CellType {
    @Override
    public String toString() {
        return "G";
    }
}

class EmptyCell implements CellType {
    @Override
    public String toString() {
        return " ";
    }
}
