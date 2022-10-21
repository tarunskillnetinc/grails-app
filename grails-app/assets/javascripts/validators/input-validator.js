function numericOnly(event) {
    if (["-", "+", "e", "E", "."].includes(event.key)) {
        return false;
    }
}