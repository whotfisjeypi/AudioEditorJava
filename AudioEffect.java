package ProyectoFinal;

import javax.sound.sampled.AudioInputStream;

public interface AudioEffect {
    AudioInputStream applyEffect(AudioInputStream input) throws Exception;
}