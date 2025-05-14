package Testing;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.effects.DelayEffect;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.writer.WriterProcessor;

import javax.sound.sampled.*;
import java.io.*;
import java.io.RandomAccessFile;

public class AudioEditor extends BaseAudioEditor implements AudioEffect {

    private AudioClipContainer<AudioInputStream> clipContainer = new AudioClipContainer<>();
    private AudioFormatType formatType;
    private Clip currentClip;
    private byte[] originalAudioData;


    private AudioFormat audioFormat;

    @Override
    public void loadAudio(File file) throws Exception {
        if (!(file.getName().endsWith(".wav") || file.getName().endsWith(".mp3"))) {
            throw new IllegalArgumentException("Unsupported file type. Only .wav and .mp3 are allowed.");
        }
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        if (file.getName().toLowerCase().endsWith(".mp3")) {
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            in = AudioSystem.getAudioInputStream(decodedFormat, in);
            formatType = AudioFormatType.MP3;
            System.out.println("MP3 loaded and converted to PCM_SIGNED format.");
        } else {
            formatType = AudioFormatType.WAV;
            System.out.println("WAV loaded.");
        }
        audioFormat = in.getFormat();
        originalAudioData = in.readAllBytes();
        audioStream = createNewAudioStream();
    }


    private AudioInputStream createNewAudioStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(originalAudioData);
        return new AudioInputStream(bais, audioFormat, originalAudioData.length / audioFormat.getFrameSize());
    }

    @Override
    public void saveAudio(File file) throws Exception {
        if (audioStream == null) throw new IllegalStateException("No audio loaded");
        if(formatType == AudioFormatType.MP3) {
            System.out.println("MP3 saving is not fully supported. Converting to WAV format...");
        }
        // Use a new stream for saving so that the original remains intact
        AudioSystem.write(createNewAudioStream(), AudioFileFormat.Type.WAVE, file);
        System.out.println("Audio saved to: " + file.getAbsolutePath());
    }

    private long millisToFrames(int millis, AudioFormat format) {
        return (long) (millis / 1000.0 * format.getFrameRate());
    }

    public void cut(int startMillis, int endMillis) throws IOException {
        if(originalAudioData == null) throw new IllegalStateException("No audio loaded");

        audioStream = createNewAudioStream();
        if(startMillis < 0 || endMillis <= startMillis) {
            throw new IllegalArgumentException("Invalid cut range.");
        }
        AudioFormat format = audioStream.getFormat();
        long startFrame = millisToFrames(startMillis, format);
        long endFrame = millisToFrames(endMillis, format);
        long frameLength = endFrame - startFrame;

        long skipped = audioStream.skip(startFrame * format.getFrameSize());
        if (skipped < startFrame * format.getFrameSize()) {
            throw new IOException("Unable to skip to desired start position.");
        }

        byte[] buffer = new byte[(int) (frameLength * format.getFrameSize())];
        int bytesRead = audioStream.read(buffer);
        if (bytesRead < buffer.length) {
            throw new IOException("Could not read the expected number of frames.");
        }

        ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
        AudioInputStream clip = new AudioInputStream(byteStream, format, frameLength);
        clipContainer.addClip(clip);
        System.out.println("Audio cut from " + startMillis + "ms to " + endMillis + "ms and stored as a clip");
    }

    @Override
    public void applyEffect() throws Exception {
        if (originalAudioData == null) throw new IllegalStateException("No audio loaded");
        // Use a fresh stream for effect processing
        audioStream = createNewAudioStream();

        byte[] audioBytes = audioStream.readAllBytes();
        AudioFormat format = audioStream.getFormat();

        be.tarsos.dsp.io.TarsosDSPAudioFormat tarsosFormat = new be.tarsos.dsp.io.TarsosDSPAudioFormat(
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                format.isBigEndian(),
                format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
        );

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromByteArray(audioBytes, format, 1024, 512);
        dispatcher.addAudioProcessor(new DelayEffect(0.5f, 0.6f, format.getSampleRate()));

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

        audioStream = AudioSystem.getAudioInputStream(tempFile);
        System.out.println("Echo effect applied to audio...");
    }

    public void playAudio() throws Exception {
        if (originalAudioData == null) throw new IllegalStateException("No audio loaded");

        audioStream = createNewAudioStream();
        if (!audioStream.markSupported()) {
            audioStream = new AudioInputStream(new BufferedInputStream(audioStream), audioStream.getFormat(), audioStream.getFrameLength());
        }
        audioStream.mark(Integer.MAX_VALUE);
        currentClip = AudioSystem.getClip();
        currentClip.open(audioStream);
        currentClip.start();
        System.out.println("Playback started...");
    }

    public void stopAudio() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
            currentClip.close();
            try {
                audioStream.reset();
            } catch (IOException e) {
                System.err.println("Error resetting audio stream.");
            }
            System.out.println("Playback stopped.");
        }
    }

    public void exportClip(int index, File file) throws Exception {
        if (clipContainer.getClips().isEmpty()) throw new IllegalStateException("No clip available");
        if (index < 0 || index >= clipContainer.getClips().size()) {
            throw new IllegalArgumentException("Clip index out of range.");
        }
        AudioInputStream clipStream = clipContainer.getClips().get(index);
        AudioSystem.write(clipStream, AudioFileFormat.Type.WAVE, file);
        System.out.println("Clip exported to: " + file.getAbsolutePath());
    }

    public void applyNormalization() throws Exception {
        if (originalAudioData == null) throw new IllegalStateException("No audio loaded");
        System.out.println("Normalization effect applied (stub).");
    }
}


