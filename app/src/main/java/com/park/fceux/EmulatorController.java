package com.park.fceux;

import android.util.SparseIntArray;
import android.view.View;


public interface EmulatorController {

    int KEY_A = 0x01;
    int KEY_B = 0x02;
    int KEY_A_TURBO = 0x01 + 1000;
    int KEY_B_TURBO = 0x02 + 1000;
    int KEY_X = 2;
    int KEY_Y = 3;
    int KEY_START = 0x08;
    int KEY_SELECT = 0x04;
    int KEY_UP = 0x10;
    int KEY_DOWN = 0x20;
    int KEY_LEFT = 0x40;
    int KEY_RIGHT = 0x80;
    int ACTION_DOWN = 0;
    int ACTION_UP = 1;

    void onResume();

    void onPause();

    void onWindowFocusChanged(boolean hasFocus);

    View getView();

    void onDestroy();

}
