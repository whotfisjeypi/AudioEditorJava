package ProyectoFinal;

import javax.sound.sampled.AudioInputStream;

// Clase base para crear efectos de audio
public abstract class BaseEffect implements AudioEffect {

    // Frecuencia de muestreo del audio
    protected float sampleRate;

    // Constructor que guarda la frecuencia
    public BaseEffect(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    // Metodo que debe aplicar el efecto (lo implementa cada clase hija)
    @Override
    public abstract AudioInputStream applyEffect(AudioInputStream input) throws Exception;
}
