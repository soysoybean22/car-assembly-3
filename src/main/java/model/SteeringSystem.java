package model;

public enum SteeringSystem {
    BOSCH(1, "Bosch"),
    MOBIS(2, "Mobis");

    private final int menuNumber;
    private final String displayName;

    SteeringSystem(int menuNumber, String displayName) {
        this.menuNumber  = menuNumber;
        this.displayName = displayName;
    }

    public int getMenuNumber()     { return menuNumber; }
    public String getDisplayName() { return displayName; }

    public static SteeringSystem fromMenuNumber(int n) {
        for (SteeringSystem s : values()) {
            if (s.menuNumber == n) return s;
        }
        throw new IllegalArgumentException("유효하지 않은 조향장치 번호: " + n);
    }
}
