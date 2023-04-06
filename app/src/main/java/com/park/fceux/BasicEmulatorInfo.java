package com.park.fceux;

abstract public class BasicEmulatorInfo implements EmulatorInfo {
    public boolean hasZapper() {
        return true;
    }

    public boolean isMultiPlayerSupported() {
        return true;
    }

    @Override
    public String[] getDeviceKeyboardNames() {
        String[] base = new String[]{
                "UP", "DOWN", "RIGHT", "LEFT",
                "START", "SELECT",
                "A", "B", "TURBO A", "TURBO B",
                "LEFT+UP", "RIGHT+UP", "RIGHT+DOWN", "LEFT+DOWN",
                "SAVE STATE 1", "LOAD STATE 1",
                "SAVE STATE 2", "LOAD STATE 2",
                "SAVE STATE 3", "LOAD STATE 3",
                "MENU", "FAST FORWARD", "EXIT",
        };

        if (isMultiPlayerSupported()) {
            String[] res = new String[base.length * 2];
            System.arraycopy(base, 0, res, 0, base.length);
            System.arraycopy(base, 0, res, base.length, base.length);
            return res;
        } else {
            return base;
        }
    }

    @Override
    public String[] getDeviceKeyboardDescriptions() {
        int len = getDeviceKeyboardNames().length;
        String[] descs = new String[len];

        for (int i = 0; i < len; i++) {
            if (isMultiPlayerSupported()) {
                descs[i] = "Player 1";
            } else {
                descs[i] = "";
            }
            if (isMultiPlayerSupported() && i >= len / 2) {
                descs[i] = "Player 2";
            }
        }
        return descs;
    }

    @Override
    public String getCheatInvalidCharsRegex() {
        return "[^\\p{L}\\?\\:\\p{N}]";
    }

}
