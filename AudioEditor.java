package ProyectoFinal;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.effects.DelayEffect;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.writer.WriterProcessor;

import javax.sound.sampled.*;
import java.io.*;

public class AudioEditor extends BaseAudioEditor implements AudioEffect {

    private AudioClipContainer<AudioInputStream> clipContainer = new AudioClipContainer<>();
    private AudioFormatType formatType;

    @Override
    public void loadAudio(File file) throws UnsupportedAudioFileException, IOException {
        // Validate file extension
        if (!(file.getName().endsWith(".wav") || file.getName().endsWith(".mp3"))) {
            throw new IllegalArgumentException("Unsupported file type. Only .wav and .mp3 are allowed.");
        }
        audioStream = AudioSystem.getAudioInputStream(file);
        formatType = file.getName().endsWith(".wav") ? AudioFormatType.WAV : AudioFormatType.MP3;
        System.out.println("Audio loaded: " + file.getName() + " with format: " + formatType);
    }

    @Override
    public void saveAudio(File file) throws Exception {
        if (audioStream == null) throw new IllegalStateException("No audio loaded");
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, file);
        System.out.println("Audio saved to: " + file.getAbsolutePath());
    }

    private long millisToFrames(int millis, AudioFormat format) {
        return (long) (millis / 1000.0 * format.getFrameRate());
    }

    public void cut(int startMillis, int endMillis) throws IOException {
        if(audioStream == null) throw new IllegalStateException("No audio loaded");
        AudioFormat format = audioStream.getFormat();
        long startFrame = millisToFrames(startMillis, format);
        long endFrame = millisToFrames(endMillis, format);
        long frameLength = endFrame - startFrame;

        audioStream.skip(startFrame * format.getFrameSize());
        byte[] buffer = new byte[(int) (frameLength * format.getFrameSize())];
        audioStream.read(buffer);

        ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
        AudioInputStream clip = new AudioInputStream(byteStream, format, frameLength);
        clipContainer.addClip(clip);
        System.out.println("Audio cut from " + startMillis + "ms to " + endMillis + "ms and stored as a clip");
    }

    @Override
    public void applyEffect() throws Exception {
        if (audioStream == null) throw new IllegalStateException("No audio loaded");

        // Convert the AudioInputStream to a byte array
        byte[] audioBytes = audioStream.readAllBytes();
        AudioFormat format = audioStream.getFormat();

        // Convert javax.sound.sampled.AudioFormat to TarsosDSPAudioFormat
        be.tarsos.dsp.io.TarsosDSPAudioFormat tarsosFormat = new be.tarsos.dsp.io.TarsosDSPAudioFormat(
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                format.isBigEndian(),
                format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
        );

        // Create an AudioDispatcher from the byte array
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromByteArray(audioBytes, format, 1024, 512);

        // Add an echo effect (delay of 0.5 seconds, decay of 0.6)
        dispatcher.addAudioProcessor(new DelayEffect(0.5f, 0.6f, format.getSampleRate()));

        // Use a temporary file to store the processed audio; validate creation errors.
        File tempFile;
        try {
            tempFile = File.createTempFile("processed_audio", ".wav");
        } catch (IOException e) {
            System.err.println("Error creating temporary file for processed audio.");
            throw e;
        }

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw")) {
            dispatcher.addAudioProcessor(new WriterProcessor(tarsosFormat, randomAccessFile));
            dispatcher.run();
        }

        // Reload the processed audio back into the AudioInputStream
        audioStream = AudioSystem.getAudioInputStream(tempFile);
        System.out.println("Echo effect applied to audio...");
    }
}