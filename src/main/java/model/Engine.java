package model;

public enum Engine {
    GM    (1, "GM"),
    TOYOTA(2, "TOYOTA"),
    WIA   (3, "WIA"),
    BROKEN(4, "고장난 엔진");

    private final int menuNumber;
    private final String displayName;

    Engine(int menuNumber, String displayName) {
        this.menuNumber  = menuNumber;
        this.displayName = displayName;
    }

    public int getMenuNumber()     { return menuNumber; }
    public String getDisplayName() { return displayName; }
    public boolean isBroken()      { return this == BROKEN; }

    public static Engine fromMenuNumber(int n) {
        for (Engine e : values()) {
            if (e.menuNumber == n) return e;
        }
        throw new IllegalArgumentException("유효하지 않은 엔진 번호: " + n);
    }
}
