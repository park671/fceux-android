package com.park.fceux;

import android.util.SparseIntArray;

import java.util.List;

public interface EmulatorInfo {

    String getName();

    boolean hasZapper();

    boolean supportsRawCheats();

    String getCheatInvalidCharsRegex();

    GfxProfile getDefaultGfxProfile();

    SfxProfile getDefaultSfxProfile();

    List<GfxProfile> getAvailableGfxProfiles();

    List<SfxProfile> getAvailableSfxProfiles();

    SparseIntArray getKeyMapping();

    int getNumQualityLevels();

    int[] getDeviceKeyboardCodes();

    String[] getDeviceKeyboardNames();

    String[] getDeviceKeyboardDescriptions();

    boolean isMultiPlayerSupported();

}
