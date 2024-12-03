package com.github.tartaricacid.mineretro.client.jna;

import com.sun.jna.*;

@SuppressWarnings("all")
public interface MineretroMiddleTier extends Library {
    public static final int RETRO_PIXEL_FORMAT_0RGB1555 = 0;
    public static final int RETRO_PIXEL_FORMAT_XRGB8888 = 1;
    public static final int RETRO_PIXEL_FORMAT_RGB565 = 2;

    MineretroMiddleTier INSTANCE = Native.load("libMineretro", MineretroMiddleTier.class);

    void MineretroLoadCore(String corePath);

    void MineretroUnloadCore();

    boolean MineretroLoadGame(String gamePath);

    void MineretroUnloadGame();

    void MineretroLoop();

    void mineretro_set_video(VideoRefresh video);

    void mineretro_set_audio(AudioSample audio);

    void mineretro_set_audio_batch(AudioSampleBatch audio);

    void mineretro_set_input_poll(InputPoll inputPoll);

    void mineretro_set_input_state(InputState inputState);

    void mineretro_set_system_and_save_dir(String systemDir, String saveDir);

    SystemInfo.ByValue mineretro_get_system_info();

    SystemAvInfo.ByValue mineretro_get_system_av_info();

    GameGeometry.ByValue mineretro_get_geometry_info();

    int mineretro_get_pixel_format();

    int mineretro_get_rotation();

    interface VideoRefresh extends Callback {
        void invoke(Pointer data, int width, int height, int pitch);
    }

    interface AudioSample extends Callback {
        void invoke(short left, short right);
    }

    interface AudioSampleBatch extends Callback {
        void invoke(Pointer data, int frames);
    }

    interface InputPoll extends Callback {
        void invoke();
    }

    interface InputState extends Callback {
        int invoke(int port, int device, int index, int id);
    }

    @Structure.FieldOrder({"base_width", "base_height", "max_width", "max_height", "aspect_ratio"})
    public class GameGeometry extends Structure {
        public int base_width;
        public int base_height;
        public int max_width;
        public int max_height;
        public float aspect_ratio;

        public static class ByValue extends GameGeometry implements Structure.ByValue {
        }

        public static class ByReference extends GameGeometry implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder({"fps", "sample_rate"})
    public class SystemTiming extends Structure {
        public double fps;
        public double sample_rate;

        public static class ByValue extends SystemTiming implements Structure.ByValue {
        }

        public static class ByReference extends SystemTiming implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder({"geometry", "timing"})
    public class SystemAvInfo extends Structure {
        public GameGeometry.ByValue geometry;
        public SystemTiming.ByValue timing;

        public static class ByValue extends SystemAvInfo implements Structure.ByValue {
        }

        public static class ByReference extends SystemAvInfo implements Structure.ByReference {
        }
    }

    @Structure.FieldOrder({"library_name", "library_version", "valid_extensions", "need_fullpath", "block_extract"})
    public class SystemInfo extends Structure {
        public String library_name;
        public String library_version;
        public String valid_extensions;
        public boolean need_fullpath;
        public boolean block_extract;

        public static class ByValue extends SystemInfo implements Structure.ByValue {
        }

        public static class ByReference extends SystemInfo implements Structure.ByReference {
        }
    }
}
