package model;

public enum CarType {
    SEDAN(1, "Sedan"),
    SUV  (2, "SUV"),
    TRUCK(3, "Truck");

    private final int menuNumber;
    private final String displayName;

    CarType(int menuNumber, String displayName) {
        this.menuNumber  = menuNumber;
        this.displayName = displayName;
    }

    public int getMenuNumber()     { return menuNumber; }
    public String getDisplayName() { return displayName; }

    public static CarType fromMenuNumber(int n) {
        for (CarType t : values()) {
            if (t.menuNumber == n) return t;
        }
        throw new IllegalArgumentException("유효하지 않은 차량 타입 번호: " + n);
    }
}
