public class ArithmeticOperations {
    public double execute(String operator, double operand1, double operand2) throws Exception {
        switch (operator) {
            case "+": return operand1 + operand2;
            case "-": return operand1 - operand2;
            case "*": return operand1 * operand2;
            case "/": return operand1 / operand2;
            case "<": return operand1 < operand2 ? 1 : 0; // Retorna 1 para verdadero, 0 para falso
            case ">": return operand1 > operand2 ? 1 : 0; // Retorna 1 para verdadero, 0 para falso
            default: throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
}
