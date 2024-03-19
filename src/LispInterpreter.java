import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class LispInterpreter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Evaluator evaluator = new Evaluator();
        ExecutionContext context = new ExecutionContext(); // Crear el contexto de ej(Decución
        System.out.println("LISP Interpreter. Enter expressions or 'exit' to quit.");

        // Leer expresiones del archivo
        String fileName = "data/expressions.txt"; // Cambia esto al nombre de tu archivo
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Ignorar líneas vacías
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    // Evaluar la expresión del archivo
                    String result = evaluator.eval(line, context);
                    System.out.println("Result: " + result);
                } catch (Exception e) {
                    System.out.println("Error evaluating expression: " + line);
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        // Permitir que el usuario continúe interactuando con el intérprete Lisp
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input)) {
                break;
            }

            try {
                // Pasar el contexto cada vez que evaluamos una expresión
                System.out.println("Result: " + evaluator.eval(input, context));
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Interpreter terminated.");
    }
}
