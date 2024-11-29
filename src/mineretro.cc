#include <windows.h>
#include <stdio.h>
#include "libretro.h"
#include "mineretro.h"

#include <iostream>
#include <ostream>

namespace mineretro {
    const char *kLogLevel[] = {"Debug", "Info", "Warning", "Error"};

    LibretroReference libretro_reference;

    retro_variable *g_vars = nullptr;
    retro_audio_callback audio_callback;
    retro_system_av_info av_info;
    retro_game_geometry geometry_info;
    retro_pixel_format pixel_format;

    retro_video_refresh_t mineretro_video = nullptr;
    retro_audio_sample_t mineretro_audio = nullptr;
    retro_audio_sample_batch_t mineretro_audio_batch = nullptr;
    retro_input_poll_t mineretro_input_poll = nullptr;
    retro_input_state_t mineretro_input_state = nullptr;

    void MineretroInit(const char *core_path, const char *game_path) {
        CoreLoad(core_path);

        // 加载游戏
        CoreLoadGame(game_path);

        // 设置当前输入按键布局为 RETRO_DEVICE_JOYPAD
        libretro_reference.retro_set_controller_port_device(0, RETRO_DEVICE_JOYPAD);

        // 启用音频
        if (audio_callback.set_state) {
            audio_callback.set_state(true);
        }
    }

    void CoreLoad(const char *core_file) {
        // 初始化回调函数
        void (*set_environment)(retro_environment_t) = nullptr;
        void (*set_video_refresh)(retro_video_refresh_t) = nullptr;
        void (*set_input_poll)(retro_input_poll_t) = nullptr;
        void (*set_input_state)(retro_input_state_t) = nullptr;
        void (*set_audio_sample)(retro_audio_sample_t) = nullptr;
        void (*set_audio_sample_batch)(retro_audio_sample_batch_t) = nullptr;

        memset(&libretro_reference, 0, sizeof(libretro_reference));
        libretro_reference.hmodule = LoadLibrary(TEXT(core_file));

        libretro_reference.retro_init = reinterpret_cast<void(*)()>(GetProcAddress(
            libretro_reference.hmodule, "retro_init"));
        libretro_reference.retro_deinit = reinterpret_cast<void(*)()>(GetProcAddress(
            libretro_reference.hmodule, "retro_deinit"));
        libretro_reference.retro_api_version = reinterpret_cast<unsigned(*)()>(
            GetProcAddress(libretro_reference.hmodule, "retro_api_version"));
        libretro_reference.retro_get_system_info = reinterpret_cast<void(*)(retro_system_info *info)>(GetProcAddress(
            libretro_reference.hmodule, "retro_get_system_info"));
        libretro_reference.retro_get_system_av_info = reinterpret_cast<void(*)(retro_system_av_info *info)>(
            GetProcAddress(
                libretro_reference.hmodule, "retro_get_system_av_info"));
        libretro_reference.retro_set_controller_port_device = reinterpret_cast<void(*)(unsigned port, unsigned device)>(
            GetProcAddress(
                libretro_reference.hmodule, "retro_set_controller_port_device"));
        libretro_reference.retro_run = reinterpret_cast<void(*)()>(GetProcAddress(
            libretro_reference.hmodule, "retro_run"));
        libretro_reference.retro_load_game = reinterpret_cast<bool(*)(const retro_game_info *game)>(GetProcAddress(
            libretro_reference.hmodule, "retro_load_game"));
        libretro_reference.retro_unload_game = reinterpret_cast<void(*)()>(GetProcAddress(
            libretro_reference.hmodule, "retro_unload_game"));

        set_environment = reinterpret_cast<void(*)(retro_environment_t)>(
            GetProcAddress(libretro_reference.hmodule, "retro_set_environment"));
        set_video_refresh = reinterpret_cast<void(*)(retro_video_refresh_t)>(
            GetProcAddress(libretro_reference.hmodule, "retro_set_video_refresh"));
        set_input_poll = reinterpret_cast<void(*)(retro_input_poll_t)>(
            GetProcAddress(libretro_reference.hmodule, "retro_set_input_poll"));
        set_input_state = reinterpret_cast<void(*)(retro_input_state_t)>(
            GetProcAddress(libretro_reference.hmodule, "retro_set_input_state"));
        set_audio_sample = reinterpret_cast<void(*)(retro_audio_sample_t)>(
            GetProcAddress(libretro_reference.hmodule, "retro_set_audio_sample"));
        set_audio_sample_batch = reinterpret_cast<void(*)(retro_audio_sample_batch_t)>(
            GetProcAddress(libretro_reference.hmodule, "retro_set_audio_sample_batch"));

        CoreLog(RETRO_LOG_INFO, "API Version: %d", libretro_reference.retro_api_version());

        set_environment(reinterpret_cast<retro_environment_t>(CoreEnvironment));
        set_video_refresh(CoreVideoRefresh);
        set_input_poll(CoreInputPoll);
        set_input_state(CoreInputState);
        set_audio_sample(CoreAudioSample);
        set_audio_sample_batch(CoreAudioSampleBatch);

        libretro_reference.retro_init();
        libretro_reference.initialized = true;

        CoreLog(RETRO_LOG_INFO, "Core loaded");
    }

    bool CoreEnvironment(const unsigned cmd, const void *data) {
        switch (cmd) {
            case RETRO_ENVIRONMENT_SET_VARIABLES: {
                // 将 data 转换成 retro_variable[]，然后复制给 g_vars
                // 传入的数据类似于这样的格式：
                // { "foo_option", "Speed hack coprocessor X; false|true" }
                // { "sfb_max_players", "Maximum number of players allowed (Requires restart); 4|2|3|1|5|6|7|8" }
                // key 就是 foo_option, sfb_max_players
                // value 就是后面那部分
                // 后面那部分，分号之前的都是注释
                // 后面的是可用的值，用 | 分割
                const auto *vars = static_cast<const struct retro_variable *>(data);
                size_t num_vars = 0;
                for (const retro_variable *v = vars; v->key; ++v) {
                    num_vars++;
                }
                g_vars = static_cast<retro_variable *>(calloc(num_vars + 1, sizeof(*g_vars)));

                // 这里的目的就是把后面的数值进行分析，取第一个数值，存入
                // 比如上文的 foo_option，就取 false
                for (unsigned i = 0; i < num_vars; ++i) {
                    const retro_variable *in_var = &vars[i];
                    retro_variable *out_var = &g_vars[i];

                    const char *semicolon = strchr(in_var->value, ';');
                    const char *first_pipe = strchr(in_var->value, '|');

                    semicolon++;
                    while (isspace(*semicolon)) {
                        semicolon++;
                    }
                    if (first_pipe) {
                        out_var->value = static_cast<char *>(malloc(first_pipe - semicolon + 1));
                        memcpy(const_cast<char *>(out_var->value), semicolon, first_pipe - semicolon);
                        const_cast<char *>(out_var->value)[first_pipe - semicolon] = '\0';
                    } else {
                        out_var->value = strdup(semicolon);
                    }
                    out_var->key = strdup(in_var->key);
                }
                return true;
            }

            case RETRO_ENVIRONMENT_GET_VARIABLE: {
                auto *var = (struct retro_variable *) data;
                if (!g_vars) {
                    return false;
                }
                // 把我们 RETRO_ENVIRONMENT_SET_VARIABLES 这一步得到的数据
                // 对比 key 是一样的情况下，复制 value 过去
                for (const retro_variable *v = g_vars; v->key; ++v) {
                    if (strcmp(var->key, v->key) == 0) {
                        var->value = v->value;
                        break;
                    }
                }
                return true;
            }

            case RETRO_ENVIRONMENT_GET_VARIABLE_UPDATE: {
                bool *bval = (bool *) data;
                *bval = false;
                return true;
            }

            case RETRO_ENVIRONMENT_GET_LOG_INTERFACE: {
                auto *cb = (retro_log_callback *) data;
                cb->log = CoreLog;
                return true;
            }
            case RETRO_ENVIRONMENT_GET_PERF_INTERFACE: {
                return true;
            }

            case RETRO_ENVIRONMENT_GET_CAN_DUPE: {
                bool *bval = (bool *) data;
                *bval = true;
                return true;
            }

            case RETRO_ENVIRONMENT_SET_PIXEL_FORMAT: {
                const auto fmt = static_cast<const enum retro_pixel_format *>(data);
                if (*fmt > RETRO_PIXEL_FORMAT_RGB565) {
                    return false;
                }
                pixel_format = *fmt;
                return true;
            }

            case RETRO_ENVIRONMENT_SET_HW_RENDER: {
                return true;
            }

            case RETRO_ENVIRONMENT_SET_FRAME_TIME_CALLBACK: {
                return true;
            }

            case RETRO_ENVIRONMENT_SET_AUDIO_CALLBACK: {
                auto *audio_cb = static_cast<const struct retro_audio_callback *>(data);
                audio_callback = *audio_cb;
                return true;
            }

            case RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY:
            case RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY: {
                const char **dir = (const char **) data;
                *dir = ".";
                return true;
            }

            case RETRO_ENVIRONMENT_SET_GEOMETRY: {
                auto *info = static_cast<const struct retro_game_geometry *>(data);
                geometry_info = *info;
                return true;
            }

            case RETRO_ENVIRONMENT_SET_SUPPORT_NO_GAME: {
                // 有些引擎自带游戏
                libretro_reference.supports_no_game = *(bool *) data;
                return true;
            }

            case RETRO_ENVIRONMENT_GET_AUDIO_VIDEO_ENABLE: {
                int *value = (int *) data;
                // 启用音频和视频
                // 这玩意儿写的太抽象了
                *value = 1 << 0 | 1 << 1;
                return true;
            }

            default: return false;
        }
    }

    void CoreLoadGame(const char *filename) {
        retro_system_info system = {nullptr};
        retro_game_info info = {filename, nullptr};

        info.path = filename;
        info.meta = "";
        info.data = nullptr;
        info.size = 0;

        // 如果文件名不为空
        if (filename) {
            // 获取系统信息
            libretro_reference.retro_get_system_info(&system);

            // 如果是开启了 need_fullpath
            if (!system.need_fullpath) {
                CoreLog(RETRO_LOG_ERROR, "Not need full path...");
            }
        }

        if (!libretro_reference.retro_load_game(&info)) {
            CoreLog(RETRO_LOG_ERROR, "The core failed to load the content.");
        }

        // 获取视频信息
        libretro_reference.retro_get_system_av_info(&av_info);
    }

    void CoreVideoRefresh(const void *data, unsigned width, unsigned height, size_t pitch) {
        mineretro_video(data, width, height, pitch);
    }

    void CoreInputPoll() {
        mineretro_input_poll();
    }

    int16_t CoreInputState(unsigned port, unsigned device, unsigned index, unsigned id) {
        return mineretro_input_state(port, device, index, id);
    }

    size_t CoreAudioSampleBatch(const int16_t *data, const size_t frames) {
        return mineretro_audio_batch(data, frames);
    }

    void CoreAudioSample(const int16_t left, const int16_t right) {
        mineretro_audio(left, right);
    }

    void MineretroLoop() {
        if (audio_callback.callback) {
            audio_callback.callback();
        }
        libretro_reference.retro_run();
    }

    void MineretroDeinit() {
        if (libretro_reference.initialized) {
            libretro_reference.retro_deinit();
            FreeLibrary(libretro_reference.hmodule);
        }

        if (g_vars) {
            for (const retro_variable *v = g_vars; v->key; ++v) {
                free(const_cast<char *>(v->key));
                free(const_cast<char *>(v->value));
            }
            free(g_vars);
        }
    }

    void mineretro_set_video(const retro_video_refresh_t video) {
        mineretro_video = video;
    }

    void mineretro_set_audio(const retro_audio_sample_t audio) {
        mineretro_audio = audio;
    }

    void mineretro_set_audio_batch(const retro_audio_sample_batch_t audio) {
        mineretro_audio_batch = audio;
    }

    void mineretro_set_input_poll(const retro_input_poll_t input_poll) {
        mineretro_input_poll = input_poll;
    }

    void mineretro_set_input_state(const retro_input_state_t input_state) {
        mineretro_input_state = input_state;
    }

    retro_system_av_info mineretro_get_system_av_info() {
        return av_info;
    }

    retro_game_geometry mineretro_get_geometry_info() {
        return geometry_info;
    }

    retro_pixel_format mineretro_get_pixel_format() {
        return pixel_format;
    }

    void CoreLog(const retro_log_level level, const char *fmt, ...) {
        char buffer[4096] = {};
        va_list va;

        va_start(va, fmt);
        vsnprintf(buffer, sizeof(buffer), fmt, va);
        va_end(va);

        fprintf(stderr, "[%s] [Mineretro] %s", kLogLevel[level], buffer);
        fflush(stderr);

        if (level == RETRO_LOG_ERROR) {
            exit(EXIT_FAILURE);
        }
    }
}
