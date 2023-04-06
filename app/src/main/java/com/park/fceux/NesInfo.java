package com.park.fceux;

import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;

public class NesInfo extends BasicEmulatorInfo {
    public static List<SfxProfile> sfxProfiles = new ArrayList<>();
    public static List<GfxProfile> gfxProfiles = new ArrayList<>();
    public static GfxProfile pal;
    public static GfxProfile ntsc;

    static {
        ntsc = new NesGfxProfile();
        ntsc.fps = 60;
        ntsc.name = "NTSC";
        ntsc.originalScreenWidth = 256;
        ntsc.originalScreenHeight = 224;
        gfxProfiles.add(ntsc);

        pal = new NesGfxProfile();
        pal.fps = 50;
        pal.name = "PAL";
        pal.originalScreenWidth = 256;
        pal.originalScreenHeight = 240;
        gfxProfiles.add(pal);

//        SfxProfile low = new NesSfxProfile();
//        low.name = "low";
//        low.bufferSize = 2048 * 8 * 2;
//        low.encoding = SfxProfile.SoundEncoding.PCM16;
//        low.isStereo = true;
//        low.rate = 11025;
//        low.quality = 0;
//        sfxProfiles.add(low);
//
//        SfxProfile medium = new NesSfxProfile();
//        medium.name = "medium";
//        medium.bufferSize = 2048 * 8 * 2;
//        medium.encoding = SfxProfile.SoundEncoding.PCM16;
//        medium.isStereo = true;
//        medium.rate = 22050;
//        medium.quality = 1;
//        sfxProfiles.add(medium);

        SfxProfile high = new NesSfxProfile();
        high.name = "high";
        high.bufferSize = 2048 * 8 * 2;
        high.encoding = SfxProfile.SoundEncoding.PCM16;
        high.isStereo = true;
        high.rate = 44100;
        high.quality = 2;
        sfxProfiles.add(high);
    }

    public boolean hasZapper() {
        return true;
    }

    @Override
    public SparseIntArray getKeyMapping() {
        SparseIntArray mapping = new SparseIntArray();
        mapping.put(EmulatorController.KEY_A, 0x01);
        mapping.put(EmulatorController.KEY_B, 0x02);
        mapping.put(EmulatorController.KEY_SELECT, 0x04);
        mapping.put(EmulatorController.KEY_START, 0x08);
        mapping.put(EmulatorController.KEY_UP, 0x10);
        mapping.put(EmulatorController.KEY_DOWN, 0x20);
        mapping.put(EmulatorController.KEY_LEFT, 0x40);
        mapping.put(EmulatorController.KEY_RIGHT, 0x80);
        mapping.put(EmulatorController.KEY_A_TURBO, 0x01 + 1000);
        mapping.put(EmulatorController.KEY_B_TURBO, 0x02 + 1000);
        return mapping;
    }

    @Override
    public String getName() {
        return "Nostalgia.NES";
    }

    @Override
    public GfxProfile getDefaultGfxProfile() {
        return ntsc;
    }

    @Override
    public SfxProfile getDefaultSfxProfile() {
        return sfxProfiles.get(0);
    }

    @Override
    public List<GfxProfile> getAvailableGfxProfiles() {
        return gfxProfiles;
    }

    @Override
    public List<SfxProfile> getAvailableSfxProfiles() {
        return sfxProfiles;
    }

    public boolean supportsRawCheats() {
        return true;
    }

    @Override
    public int getNumQualityLevels() {
        return 3;
    }

    @Override
    public int[] getDeviceKeyboardCodes() {
        return new int[0];
    }

    public static class NesGfxProfile extends GfxProfile {
        @Override
        public int toInt() {
            return fps == 50 ? 1 : 0;
        }
    }

    public static class NesSfxProfile extends SfxProfile {
        @Override
        public int toInt() {
            int x = rate / 11025;
            x += quality * 100;
            return x;
        }
    }
}