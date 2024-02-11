import java.util.Scanner;

public class LispInterpreter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("LISP Interpreter. Enter expressions or 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            
            if ("exit".equalsIgnoreCase(input)) {
                break;
            }
            
            try {
                System.out.println("Result: " + eval(input));
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
        System.out.println("Interpreter terminated.");
    }
    
    private static double eval(String expression) throws Exception {
        // Eliminar los paréntesis y dividir por espacios
        String[] tokens = expression.replaceAll("[()]", "").trim().split("\\s+");
        if (tokens.length != 3) {
            throw new IllegalArgumentException("Invalid expression format.");
        }
        
        String operator = tokens[0];
        double operand1 = Double.parseDouble(tokens[1]);
        double operand2 = Double.parseDouble(tokens[2]);
        
        return switch (operator) {
            case "+" -> operand1 + operand2;
            case "-" -> operand1 - operand2;
            case "*" -> operand1 * operand2;
            case "/" -> operand1 / operand2;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
