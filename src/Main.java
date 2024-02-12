import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        try {
            // Llama al intérprete de Lisp con el programa como argumento
            Process process = new ProcessBuilder("sbcl", "--script", "temperatura.clj").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Lee la salida del programa Lisp
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Espera a que el proceso termine
            int exitCode = process.waitFor();
            System.out.println("El proceso Lisp terminó con el código de salida: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
