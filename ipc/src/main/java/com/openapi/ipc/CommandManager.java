package com.openapi.ipc;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;


public class CommandManager {
    private static CommandManager sInstance = new CommandManager();
    private Map<String, ICommand> mCommandMap = new HashMap<>();

    public static CommandManager getInstance() {
        return sInstance;
    }

    private CommandManager() {

    }

    public Bundle invoke(String method, String arg, Bundle extras) {
        Bundle bundle = null;
        do {
            ICommand command = mCommandMap.get(method);
            if (command == null) {
                break;
            }
            bundle = command.invoke(arg, extras);
        } while (false);
        return bundle;
    }

    public void regMethod(String method, ICommand command) {
        mCommandMap.put(method, command);
    }

}
