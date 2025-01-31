package com.teliolabs.corba.application.types;

public enum ExitCode {
    SUCCESS(0),
    GENERAL_ERROR(1),
    CONFIGURATION_ERROR(2),
    DATABASE_ERROR(3),
    NETWORK_ERROR(4);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

}
