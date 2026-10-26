package com.drawfirework.draft;

public class DraftCorruptedException extends DraftStorageException {
    public DraftCorruptedException(String message) {
        super(message);
    }

    public DraftCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
