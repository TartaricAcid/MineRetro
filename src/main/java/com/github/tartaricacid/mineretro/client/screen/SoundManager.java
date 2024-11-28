package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.client.jna.MineretroMiddleTier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundManager {
    private final SourceDataLine sourceDataLine;
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

        this.sampleBatch = (data, frames) -> sourceDataLine.write(data.getByteArray(0, frames * 4), 0, frames * 4);
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
