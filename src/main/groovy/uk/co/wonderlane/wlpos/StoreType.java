package uk.co.wonderlane.wlpos;

public enum StoreType {
    STORE("STORE"),
    CAFE("CAFE"),
    CANTEEN("CANTEEN");


    private final String value;

    StoreType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
