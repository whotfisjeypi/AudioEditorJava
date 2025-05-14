package Testing;

import javax.sound.sampled.AudioInputStream;
import java.io.File;

public abstract class BaseAudioEditor {
    protected AudioInputStream audioStream;
    public abstract void loadAudio(File file) throws Exception;
    public abstract void saveAudio(File file) throws Exception;
}
