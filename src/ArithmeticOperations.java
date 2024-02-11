public class ArithmeticOperations {
    public double execute(String operator, double operand1, double operand2) throws Exception {
        return switch (operator) {
            case "+" -> operand1 + operand2;
            case "-" -> operand1 - operand2;
            case "*" -> operand1 * operand2;
            case "/" -> operand1 / operand2;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
