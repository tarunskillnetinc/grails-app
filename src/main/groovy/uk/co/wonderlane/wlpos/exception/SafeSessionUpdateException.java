package uk.co.wonderlane.wlpos.exception;

import uk.co.wonderlane.wlpos.entities.cash.SafeSession;

public class SafeSessionUpdateException extends Exception {
    private SafeSession safeSession;

    public SafeSessionUpdateException(SafeSession safeSession, String message) {
        super(message);
        this.safeSession = safeSession;
    }

    public SafeSession getSafeSession() {
        return safeSession;
    }
}
