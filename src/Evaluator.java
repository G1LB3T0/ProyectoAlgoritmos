public class Evaluator {
    private ArithmeticOperations arithmeticOperations = new ArithmeticOperations();

    public double eval(String expression) throws Exception {
        // Eliminar los paréntesis y dividir por espacios
        String[] tokens = expression.replaceAll("[()]", "").trim().split("\\s+");
        if (tokens.length != 3) {
            throw new IllegalArgumentException("Invalid expression format.");
        }

        String operator = tokens[0];
        double operand1 = Double.parseDouble(tokens[1]);
        double operand2 = Double.parseDouble(tokens[2]);

        return arithmeticOperations.execute(operator, operand1, operand2);
    }
}
