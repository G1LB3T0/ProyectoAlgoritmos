import java.util.Scanner;

public class LispInterpreter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Evaluator evaluator = new Evaluator();
        ExecutionContext context = new ExecutionContext(); // Crear el contexto de ejecución
        System.out.println("LISP Interpreter. Enter expressions or 'exit' to quit.");

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
