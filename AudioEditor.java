package ProyectoFinal;

// Importa librerias para manejar audio y efectos
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.effects.DelayEffect;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.writer.WriterProcessor;

import javax.sound.sampled.*;
import java.io.*;

// Clase principal para editar audio
public class AudioEditor extends BaseAudioEditor implements AudioEffect {

    private Clip clip;

    @Override
    public AudioInputStream applyEffect(AudioInputStream inputStream) throws Exception {
        System.out.println("Applying effect to the audio stream...");
        return inputStream;
    }

    // Contenedor para guardar clips de audio cortados
    private AudioClipContainer<AudioInputStream> clipContainer = new AudioClipContainer<>();
    private AudioFormatType formatType;

    // Metodo para obtener el stream actual de audio
    public AudioInputStream getAudioStream() {
        return audioStream;
    }

    @Override
    public void loadAudio(File file) throws UnsupportedAudioFileException, IOException {
        // Verifica que sea .wav o .mp3
        if (!(file.getName().endsWith(".wav") || file.getName().endsWith(".mp3"))) {
            throw new IllegalArgumentException("Unsupported file type. Only .wav and .mp3 are allowed.");
        }
        if (file.getName().endsWith(".mp3")) {
            audioStream = AudioSystem.getAudioInputStream(file);
            formatType = AudioFormatType.MP3;
        } else {
            audioStream = AudioSystem.getAudioInputStream(file);
            formatType = AudioFormatType.WAV;
        }
        System.out.println("Audio loaded: " + file.getName() + " with format: " + formatType);
    }
    @Override
    public void saveAudio(File file) throws Exception {
        // Guarda el audio actual en un archivo
        if (audioStream == null) throw new IllegalStateException("No audio loaded");
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, file);
        System.out.println("Audio saved to: " + file.getAbsolutePath());
    }

    // Convierte milisegundos a cuadros de audio
    private long millisToFrames(int millis, AudioFormat format) {
        return (long) (millis / 1000.0 * format.getFrameRate());
    }

    // Metodo para cortar una parte del audio y guardarlo como clip
    public void cut(int startMillis, int endMillis) throws IOException {
        if(audioStream == null) throw new IllegalStateException("No audio loaded");

        AudioFormat format = audioStream.getFormat();
        long startFrame = millisToFrames(startMillis, format);
        long endFrame = millisToFrames(endMillis, format);
        long frameLength = endFrame - startFrame;

        // Salta a la parte inicial y lee los datos necesarios
        audioStream.skip(startFrame * format.getFrameSize());
        byte[] buffer = new byte[(int) (frameLength * format.getFrameSize())];
        audioStream.read(buffer);

        // Guarda el fragmento como un nuevo clip
        ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
        AudioInputStream clip = new AudioInputStream(byteStream, format, frameLength);
        clipContainer.addClip(clip);

        System.out.println("Audio cut from " + startMillis + "ms to " + endMillis + "ms and stored as a clip");
    }

    public void applyEchoEffect() throws Exception {
        if (audioStream == null) throw new IllegalStateException("No audio loaded");

        // Convierte el stream de audio en un arreglo de bytes
        byte[] audioBytes = audioStream.readAllBytes();
        AudioFormat format = audioStream.getFormat();

        // Convierte a formato compatible con TarsosDSP
        be.tarsos.dsp.io.TarsosDSPAudioFormat tarsosFormat = new be.tarsos.dsp.io.TarsosDSPAudioFormat(
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                format.isBigEndian(),
                format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
        );

        // Crea un procesador de audio para aplicar efectos
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromByteArray(audioBytes, format, 1024, 512);

        // Aplica un efecto de eco (delay)
        dispatcher.addAudioProcessor(new DelayEffect(0.5f, 0.6f, format.getSampleRate()));

        // Crea archivo temporal para guardar el audio procesado
        File tempFile;
        try {
            tempFile = File.createTempFile("processed_audio", ".wav");
        } catch (IOException e) {
            System.err.println("Error creating temporary file for processed audio.");
            throw e;
        }

        // Escribe el audio procesado en el archivo
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw")) {
            dispatcher.addAudioProcessor(new WriterProcessor(tarsosFormat, randomAccessFile));
            dispatcher.run();
        }

        // Carga el audio procesado de nuevo al programa
        audioStream = AudioSystem.getAudioInputStream(tempFile);
        System.out.println("Echo effect applied to audio...");
    }

    // Metodo para reproducir el audio cargado
    public void playAudio() {
        if (audioStream == null) {
            System.out.println("No audio loaded to play.");
            return;
        }
        try {
            audioStream.reset();
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            Thread playbackThread = new Thread(() -> {
                clip.start();
                System.out.println("Audio is playing...");
                // Keeps thread alive until playback completes
                try {
                    Thread.sleep(clip.getMicrosecondLength() / 1000);
                } catch (InterruptedException e) {
                    // Thread interrupted for pausing/stopping
                }
            });
            playbackThread.start();
        } catch (Exception e) {
            System.out.println("Error during audio playback: " + e.getMessage());
        }
    }

    // Pause the currently playing audio
    public void pauseAudio() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            System.out.println("Audio paused.");
        } else {
            System.out.println("No audio is playing.");
        }
    }

    public void playLastClip() {
        AudioInputStream clipStream = clipContainer.getLastClip();
        if (clipStream == null) {
            System.out.println("No clip available to play.");
            return;
        }
        try {
            // Open a new Clip for playback of the cut audio clip.
            Clip clipForCut = AudioSystem.getClip();
            clipForCut.open(clipStream);
            Thread playbackThread = new Thread(() -> {
                clipForCut.start();
                System.out.println("Cut clip is playing...");
                try {
                    Thread.sleep(clipForCut.getMicrosecondLength() / 1000);
                } catch (InterruptedException e) {
                    // Thread interrupted for pausing/stopping
                }
            });
            playbackThread.start();
        } catch (Exception e) {
            System.out.println("Error during cut clip playback: " + e.getMessage());
        }
    }

    // Metodo para aplicar otro efecto desde una clase externa
    public void applyEffect(AudioEffect effect) throws Exception {
        if (audioStream == null) throw new IllegalStateException("No audio loaded");
        audioStream = effect.applyEffect(audioStream);
        System.out.println("Efecto aplicado: " + effect.getClass().getSimpleName());
    }

}
