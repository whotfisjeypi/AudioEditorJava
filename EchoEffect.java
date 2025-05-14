package ProyectoFinal;

import be.tarsos.dsp.*; // Libreria para procesar audio
import be.tarsos.dsp.io.TarsosDSPAudioFormat;

import java.io.*;
import javax.sound.sampled.*;

// Clase que aplica un efecto de eco al audio
public class EchoEffect extends BaseEffect {

    // Constructor que recibe la frecuencia de muestreo
    public EchoEffect(float sampleRate) {
        super(sampleRate);
    }

    // Aplica el efecto de eco al audio recibido
    @Override
    public AudioInputStream applyEffect(AudioInputStream input) throws Exception {
        // Lee el audio en bytes
        byte[] audioBytes = input.readAllBytes();
        AudioFormat format = input.getFormat();

        // Convierte el formato al que usa TarsosDSP
        TarsosDSPAudioFormat tarsosFormat = new TarsosDSPAudioFormat(
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                format.isBigEndian(),
                format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED
        );

        // Crea un procesador de audio desde los bytes
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromByteArray(audioBytes, format, 1024, 512);

        // Agrega el efecto de eco (0.5 segundos de retardo, 0.6 de decaimiento)
        dispatcher.addAudioProcessor(new DelayEffect(0.5f, 0.6f, format.getSampleRate()));

        // Crea archivo temporal para guardar el audio con efecto
        File tempFile = File.createTempFile("effect_echo", ".wav");
        try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
            dispatcher.addAudioProcessor(new WriterProcessor(tarsosFormat, raf));
            dispatcher.run(); // Procesa el audio
        }

        // Devuelve el nuevo audio con efecto aplicado
        return AudioSystem.getAudioInputStream(tempFile);
    }
}
