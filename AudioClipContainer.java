package ProyectoFinal;

import javax.sound.sampled.AudioInputStream;
import java.util.ArrayList;
import java.util.List;

public class AudioClipContainer<T extends AudioInputStream> {
    private List<T> clips = new ArrayList<>();

    public void addClip(T clip) {
        clips.add(clip);
    }

    public List<T> getClips() {
        return clips;
    }

}
