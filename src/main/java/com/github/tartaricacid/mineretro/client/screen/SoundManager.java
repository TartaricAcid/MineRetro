package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.client.jna.MineretroMiddleTier;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;

public class SoundManager {
    private final SourceDataLine sourceDataLine;
    private final MineretroMiddleTier.AudioSample sampleOnce;
    private final MineretroMiddleTier.AudioSampleBatch sampleBatch;

    public SoundManager(MineretroMiddleTier mineRetro) {
        try {
            float sampleRate = (float) mineRetro.mineretro_get_system_av_info().timing.sample_rate;
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate, 16, 2, 2 * 2, sampleRate, false);
            this.sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        this.sampleOnce = (left, right) -> {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putShort(left);
            buffer.putShort(right);
            byte[] byteArray = buffer.array();
            float masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
            adjustVolume(byteArray, masterVolume);
            sourceDataLine.write(byteArray, 0, 4);
        };

        this.sampleBatch = (data, frames) -> {
            byte[] byteArray = data.getByteArray(0, frames * 4);
            float masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
            adjustVolume(byteArray, masterVolume);
            sourceDataLine.write(byteArray, 0, frames * 4);
        };
    }

    private static void adjustVolume(byte[] audioData, float volumeFactor) {
        if (volumeFactor >= 1) {
            return;
        }
        // 转为整数以减少浮点运算
        int volumeIntFactor = (int) (volumeFactor * (1 << 15));
        for (int i = 0; i < audioData.length; i += 2) {
            int sample = (audioData[i + 1] << 8) | (audioData[i] & 0xFF);
            sample = (sample * volumeIntFactor) >> 15;
            // 限幅
            sample = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample));
            audioData[i] = (byte) (sample & 0xFF);
            audioData[i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
    }

    public MineretroMiddleTier.AudioSample getSampleOnce() {
        return sampleOnce;
    }

    public MineretroMiddleTier.AudioSampleBatch getAudioSampleBatch() {
        return sampleBatch;
    }

    public void close() {
        if (sourceDataLine != null) {
            sourceDataLine.drain();
            sourceDataLine.close();
        }
    }
}
