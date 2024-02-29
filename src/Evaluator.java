public class Evaluator {
    private ArithmeticOperations arithmeticOperations = new ArithmeticOperations();

    public String eval(String expression) throws Exception {
        // Comprobar si es una instrucción QUOTE
        if (expression.startsWith("'") || expression.startsWith("(QUOTE ")) {
            return handleQuote(expression);
        }

        // Eliminar los paréntesis y dividir por espacios para operaciones aritméticas
        String[] tokens = expression.replaceAll("[()]", "").trim().split("\\s+");
        if (tokens.length != 3) {
            throw new IllegalArgumentException("Invalid expression format.");
        }

        String operator = tokens[0];
        double operand1 = Double.parseDouble(tokens[1]);
        double operand2 = Double.parseDouble(tokens[2]);

        // Convertir el resultado de las operaciones aritméticas a String para mantener la consistencia del tipo de retorno
        return String.valueOf(arithmeticOperations.execute(operator, operand1, operand2));
    }

    private String handleQuote(String expression) {
        // Remover el prefijo ' o (QUOTE ) de la expresión
        String quotedExpression = expression.startsWith("'") ?
            expression.substring(1) :
            expression.substring("(QUOTE ".length(), expression.length() - 1); // Excluir el paréntesis final

        // Devolver la expresión tal cual, sin evaluar
        return quotedExpression.trim();
    }
}
