package model;

public enum BrakeSystem {
    MANDO      (1, "Mando"),
    CONTINENTAL(2, "Continental"),
    BOSCH      (3, "Bosch");

    private final int menuNumber;
    private final String displayName;

    BrakeSystem(int menuNumber, String displayName) {
        this.menuNumber  = menuNumber;
        this.displayName = displayName;
    }

    public int getMenuNumber()     { return menuNumber; }
    public String getDisplayName() { return displayName; }

    public static BrakeSystem fromMenuNumber(int n) {
        for (BrakeSystem b : values()) {
            if (b.menuNumber == n) return b;
        }
        throw new IllegalArgumentException("유효하지 않은 제동장치 번호: " + n);
    }
}
