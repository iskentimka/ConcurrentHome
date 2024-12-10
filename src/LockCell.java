import java.util.concurrent.locks.ReentrantLock;

public class LockCell {
    private Object content;
    private final ReentrantLock lock = new ReentrantLock();

    public LockCell(Object content) {
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
