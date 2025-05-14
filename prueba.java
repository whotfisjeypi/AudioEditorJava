package ProyectoFinal;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.util.Scanner;

// Clase principal para probar el editor de audio
public class prueba {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Entrada por consola
        AudioEditor editor = new AudioEditor();   // Crea una instancia del editor
        boolean exit = false;

        // Ciclo principal del menu
        while (!exit) {
            // Opciones del menu
            System.out.println("\nMenu:");
            System.out.println("1. Load Audio");
            System.out.println("2. Cut Audio");
            System.out.println("3. Apply Effect");
            System.out.println("4. Save Audio");
            System.out.println("5. Apply Echo Effect");
            System.out.println("6. Play Audio");
            System.out.println("7. Exit");
            System.out.print("Choose an option (1-7): ");

            String option = scanner.nextLine();

            try {
                switch (option) {
                    case "1":
                        // Cargar un archivo de audio
                        System.out.print("Enter audio file path: ");
                        String path = scanner.nextLine();
                        File input = new File(path);
                        editor.loadAudio(input);
                        break;

                    case "2":
                        // Cortar el audio entre dos tiempos
                        System.out.print("Enter start time in ms: ");
                        int startMillis = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter end time in ms: ");
                        int endMillis = Integer.parseInt(scanner.nextLine());
                        editor.cut(startMillis, endMillis);
                        break;

                    case "3":
                        // Aplicar efecto general
                        editor.applyEchoEffect();
                        break;

                    case "4":
                        // Guardar el audio en un archivo
                        System.out.print("Enter output file path: ");
                        String outputPath = scanner.nextLine();
                        File output = new File(outputPath);
                        editor.saveAudio(output);
                        break;

                    case "5":
                        // Aplicar efecto de eco
                        editor.applyEffect(new EchoEffect(editor.getAudioStream().getFormat().getSampleRate()));
                        break;

                    case "6":
                        // Reproducir el audio
                        editor.playAudio();
                        break;

                    case "7":
                        // Salir del programa
                        exit = true;
                        break;

                    default:
                        System.out.println("Invalid option. Please choose between 1 and 7.");
                        break;
                }
            } catch (UnsupportedAudioFileException e) {
                System.out.println("Unsupported audio file format.");
            } catch (Exception e) {
                e.printStackTrace(); // Muestra errores si ocurren
            }
        }

        scanner.close();
        System.out.println("Program terminated.");
    }
}