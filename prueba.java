// File: prueba.java
package ProyectoFinal;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.util.Scanner;

public class prueba {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AudioEditor editor = new AudioEditor();
        boolean exit = false;

        while (!exit) {
            System.out.println("\nMenu:");
            System.out.println("1. Load Audio");
            System.out.println("2. Cut Audio");
            System.out.println("3. Apply Effect");
            System.out.println("4. Save Audio");
            System.out.println("5. Apply Echo Effect");
            System.out.println("6. Play Audio");
            System.out.println("7. Pause Audio");
            System.out.println("8. Exit");
            System.out.print("Choose an option (1-8): ");

            String option = scanner.nextLine();

            try {
                switch (option) {
                    case "1":
                        System.out.print("Enter audio file path: ");
                        String path = scanner.nextLine();
                        File input = new File(path);
                        editor.loadAudio(input);
                        break;
                    case "2":
                        System.out.print("Enter start time in ms: ");
                        int startMillis = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter end time in ms: ");
                        int endMillis = Integer.parseInt(scanner.nextLine());
                        editor.cut(startMillis, endMillis);
                        break;
                    case "3":
                        // Apply a general effect (echo in this example)
                        editor.applyEchoEffect();
                        break;
                    case "4":
                        System.out.print("Enter output file path: ");
                        String outputPath = scanner.nextLine();
                        File output = new File(outputPath);
                        editor.saveAudio(output);
                        break;
                    case "5":
                        // Apply an external effect (example using EchoEffect)
                        editor.applyEffect(new EchoEffect(editor.getAudioStream().getFormat().getSampleRate()));
                        break;
                    case "6":
                        editor.playAudio();
                        break;
                    case "7":
                        editor.pauseAudio();
                        break;
                    case "8":
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid option. Please choose between 1 and 8.");
                        break;
                }
            } catch (UnsupportedAudioFileException e) {
                System.out.println("Unsupported audio file format.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scanner.close();
        System.out.println("Program terminated.");
    }
}