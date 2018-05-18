package com.ztdx.eams.basic.exception;

import com.ztdx.eams.basic.exception.ApplicationException;

public class EntryValueConverException extends ApplicationException {

    public EntryValueConverException(String message) {
        super(400, message, null);
    }
}
