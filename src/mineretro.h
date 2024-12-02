#ifndef MINERETRO_MINERETRO_H
#define MINERETRO_MINERETRO_H

namespace mineretro {
    struct LibretroReference {
        HMODULE hmodule;
        bool initialized;
        bool supports_no_game;
        retro_perf_counter *perf_counter_last;

        void (*retro_init)();

        void (*retro_deinit)();

        unsigned (*retro_api_version)();

        void (*retro_get_system_info)(retro_system_info *info);

        void (*retro_get_system_av_info)(retro_system_av_info *info);

        void (*retro_set_controller_port_device)(unsigned port, unsigned device);

        void (*retro_reset)();

        void (*retro_run)();

        bool (*retro_load_game)(const retro_game_info *game);

        void (*retro_unload_game)();
    };

    extern "C" {
    void MineretroInit(const char *core_path, const char *game_path);

    void MineretroLoop();

    void MineretroDeinit();

    void mineretro_set_video(retro_video_refresh_t video);

    void mineretro_set_audio(retro_audio_sample_t audio);

    void mineretro_set_audio_batch(retro_audio_sample_batch_t audio);

    void mineretro_set_input_poll(retro_input_poll_t input_poll);

    void mineretro_set_input_state(retro_input_state_t input_state);

    void mineretro_set_system_and_save_dir(char *system, char *save);

    retro_system_av_info mineretro_get_system_av_info();

    retro_game_geometry mineretro_get_geometry_info();

    retro_pixel_format mineretro_get_pixel_format();

    unsigned mineretro_get_rotation();
    }

    void CoreLoad(const char *core_file);

    bool CoreEnvironment(unsigned cmd, const void *data);

    void CoreLoadGame(const char *filename);

    void CoreVideoRefresh(const void *data, unsigned width, unsigned height, size_t pitch);

    void CoreInputPoll();

    int16_t CoreInputState(unsigned port, unsigned device, unsigned index, unsigned id);

    size_t CoreAudioSampleBatch(const int16_t *data, size_t frames);

    void CoreAudioSample(int16_t left, int16_t right);

    void CoreLog(retro_log_level level, const char *fmt, ...);
}

#endif // MINERETRO_MINERETRO_H
