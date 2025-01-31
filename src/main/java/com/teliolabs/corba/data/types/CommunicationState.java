package com.teliolabs.corba.data.types;

public enum CommunicationState {
    AVAILABLE(0), UNAVAILABLE(1);

    private final int state;

    CommunicationState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public static CommunicationState fromState(int state) {
        for (CommunicationState communicationState : CommunicationState.values()) {
            if (communicationState.getState() == state) {
                return communicationState;
            }
        }
        return null; // Default fallback
    }
}
