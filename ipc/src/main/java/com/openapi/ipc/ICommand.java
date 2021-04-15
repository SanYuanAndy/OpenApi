package com.openapi.ipc;

import android.os.Bundle;

public interface ICommand {

    Bundle invoke(String arg, Bundle extras);
}
