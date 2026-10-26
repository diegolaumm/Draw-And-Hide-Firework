package com.drawfirework.draft;

public class DraftVersionException extends DraftStorageException {
    private final int version;

    public DraftVersionException(int version, String message) {
        super(message);
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
