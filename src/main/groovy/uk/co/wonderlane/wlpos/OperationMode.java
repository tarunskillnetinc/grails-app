package uk.co.wonderlane.wlpos;

public enum OperationMode {
    ADD(1),
    EDIT(2);

    public final int value;

    OperationMode(int value){
        this.value = value;
    }
}
