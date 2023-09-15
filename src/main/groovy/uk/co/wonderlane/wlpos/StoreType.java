package uk.co.wonderlane.wlpos;

public enum StoreType {

    HEAD_OFFICE("HEAD_OFFICE"),
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
