package Utilities;

import java.util.concurrent.ConcurrentHashMap;

public final class VolatileDatabase {

    public static ConcurrentHashMap<String, ProtocolStateMachine> database = new ConcurrentHashMap<>();

    private VolatileDatabase(){

    }

}
