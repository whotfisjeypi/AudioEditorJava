// Java
package Testing;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;

public class AudioEditorUI extends JFrame {

    private AudioEditor editor = new AudioEditor();
    private JTextArea logArea = new JTextArea();

    public AudioEditorUI() {
        super("Audio Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(createToolBar(), BorderLayout.NORTH);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(this::loadAudio);
        toolBar.add(loadButton);

        JButton playButton = new JButton("Play");
        playButton.addActionListener(this::playAudio);
        toolBar.add(playButton);


        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(this::stopAudio);
        toolBar.add(stopButton);

        JButton cutButton = new JButton("Cut");
        cutButton.addActionListener(this::cutAudio);
        toolBar.add(cutButton);

        JButton effectButton = new JButton("Effect");
        effectButton.addActionListener(this::applyEffect);
        toolBar.add(effectButton);

        JButton exportClipButton = new JButton("Export Clip");
        exportClipButton.addActionListener(this::exportClip);
        toolBar.add(exportClipButton);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this::saveAudio);
        toolBar.add(saveButton);

        JButton normButton = new JButton("Normalize");
        normButton.addActionListener(this::normalizeAudio);
        toolBar.add(normButton);

        return toolBar;
    }

    private void loadAudio(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Audio Files", "wav", "mp3"));
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                editor.loadAudio(file);
                logArea.append("Loaded: " + file.getName() + "\n");
            } catch (Exception ex) {
                logArea.append("Error loading audio: " + ex.getMessage() + "\n");
            }
        }
    }

    private void playAudio(ActionEvent e) {
        try {
            editor.playAudio();
            logArea.append("Playback started.\n");
        } catch (Exception ex) {
            logArea.append("Error during playback: " + ex.getMessage() + "\n");
        }
    }

    private void stopAudio(ActionEvent e) {
        editor.stopAudio();
        logArea.append("Playback stopped.\n");
    }

    private void cutAudio(ActionEvent e) {
        String start = JOptionPane.showInputDialog(this, "Enter start time in ms:");
        String end = JOptionPane.showInputDialog(this, "Enter end time in ms:");
        try {
            int startMillis = Integer.parseInt(start);
            int endMillis = Integer.parseInt(end);
            editor.cut(startMillis, endMillis);
            logArea.append("Audio cut from " + startMillis + "ms to " + endMillis + "ms\n");
        } catch (Exception ex) {
            logArea.append("Error during cutting: " + ex.getMessage() + "\n");
        }
    }

    private void applyEffect(ActionEvent e) {
        try {
            editor.applyEffect();
            logArea.append("Effect applied.\n");
        } catch (Exception ex) {
            logArea.append("Error applying effect: " + ex.getMessage() + "\n");
        }
    }

    private void exportClip(ActionEvent e) {
        String indexStr = JOptionPane.showInputDialog(this, "Enter clip index to export:");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("WAV Files", "wav"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".wav")) {
                file = new File(file.getAbsolutePath() + ".wav");
            }
            try {
                int index = Integer.parseInt(indexStr);
                editor.exportClip(index, file);
                logArea.append("Clip exported to " + file.getAbsolutePath() + "\n");
            } catch (Exception ex) {
                logArea.append("Error exporting clip: " + ex.getMessage() + "\n");
            }
        }
    }

    private void saveAudio(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("WAV Files", "wav"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                editor.saveAudio(file);
                logArea.append("Audio saved to " + file.getAbsolutePath() + "\n");
            } catch (Exception ex) {
                logArea.append("Error saving audio: " + ex.getMessage() + "\n");
            }
        }
    }

    private void normalizeAudio(ActionEvent e) {
        try {
            editor.applyNormalization();
            logArea.append("Normalization applied.\n");
        } catch (Exception ex) {
            logArea.append("Error during normalization: " + ex.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AudioEditorUI ui = new AudioEditorUI();
            ui.setVisible(true);
        });
    }
}