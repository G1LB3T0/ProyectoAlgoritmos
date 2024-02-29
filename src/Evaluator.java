import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {
    private ArithmeticOperations arithmeticOperations = new ArithmeticOperations();
    private Map<String, FunctionDefinition> functions = new HashMap<>();

    public String eval(String expression) throws Exception {
        // Comprobar si es una instrucción QUOTE
        if (expression.startsWith("'") || expression.startsWith("(QUOTE ")) {
            return handleQuote(expression);
        }

        if (expression.startsWith("(DEFUN")) {
            // Parsear y almacenar la definición de la función
            defineFunction(expression);
            return "Function defined.";
        }

        if (functions.containsKey(getFunctionName(expression))) {
            return evaluateFunctionCall(expression);
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

    private void defineFunction(String expression) {
        // Eliminar (DEFUN y el paréntesis final
        String withoutDefun = expression.substring(6, expression.length() - 1).trim();
        String[] parts = withoutDefun.split("\\s+", 3);
        String functionName = parts[0];
        List<String> parameters = Arrays.asList(parts[1].replaceAll("[()]", "").split("\\s+"));
        String body = parts[2];

        functions.put(functionName, new FunctionDefinition(parameters, body));
    }

    private String getFunctionName(String expression) {
        return expression.substring(1, expression.indexOf(' '));
    }
    
    private String evaluateFunctionCall(String expression) throws Exception {
        String functionName = getFunctionName(expression);
        FunctionDefinition function = functions.get(functionName);
    
        // Extraer argumentos
        String argsStr = expression.substring(expression.indexOf(' ') + 1, expression.length() - 1);
        List<String> arguments = Arrays.asList(argsStr.split("\\s+"));
    
        // Validar número de argumentos
        if (arguments.size() != function.getParameters().size()) {
            throw new IllegalArgumentException("Incorrect number of arguments for function " + functionName);
        }
    
        // Reemplazar parámetros por argumentos en el cuerpo de la función
        String evaluatedBody = function.getBody();
        for (int i = 0; i < arguments.size(); i++) {
            evaluatedBody = evaluatedBody.replaceAll("\\b" + function.getParameters().get(i) + "\\b", arguments.get(i));
        }
    
        // Evaluar el cuerpo reemplazado
        return eval(evaluatedBody);
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
