package server;

public enum Type {
    set, get, delete, exit;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}

