package com.teliolabs.corba.data.types;

import com.teliolabs.corba.application.types.ExitCode;

public enum PathType {
    MAIN(1), PROTECTION(2);

    private final int code;

    PathType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static PathType fromCode(int code) {
        for (PathType exitCode : PathType.values()) {
            if (exitCode.code() == code) {
                return exitCode;
            }
        }
        return null; // Default fallback
    }
}
