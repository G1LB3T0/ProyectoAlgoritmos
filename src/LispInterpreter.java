import java.util.Scanner;

public class LispInterpreter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Evaluator evaluator = new Evaluator();
        System.out.println("LISP Interpreter. Enter expressions or 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input)) {
                break;
            }

            try {
                System.out.println("Result: " + evaluator.eval(input));
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Interpreter terminated.");
    }
}
