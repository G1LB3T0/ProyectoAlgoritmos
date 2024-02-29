import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {
    private ArithmeticOperations arithmeticOperations = new ArithmeticOperations();
    private Map<String, FunctionDefinition> functions = new HashMap<>();

    public String eval(String expression, ExecutionContext context) throws Exception {
        // Comprobar si es una instrucción QUOTE
        if (expression.startsWith("'") || expression.startsWith("(QUOTE ")) {
            return handleQuote(expression);
        }
    
        // Manejo de la definición de funciones (DEFUN)
        if (expression.startsWith("(DEFUN")) {
            defineFunction(expression);
            return "Function defined.";
        }
    
        // Identificar y evaluar llamadas a funciones
        String functionName = getFunctionName(expression);
        if (functions.containsKey(functionName)) {
            return evaluateFunctionCall(expression, context);
        }
    
        // Ajuste para manejar operaciones aritméticas
        return handleArithmetic(expression, context);
    }
    

    private String handleArithmetic(String expression, ExecutionContext context) throws Exception {
        // Remover los paréntesis exteriores y dividir la expresión en tokens de manera más inteligente
        String trimmedExpression = expression.trim().substring(1, expression.length() - 1).trim();
        List<String> tokens = splitExpression(trimmedExpression);
    
        String operator = tokens.get(0);
        if (tokens.size() < 3) { // Asegurarse de que hay al menos un operador y dos operandos
            throw new IllegalArgumentException("Invalid arithmetic expression format: " + expression);
        }
    
        try {
            double result = 0;
            for (int i = 1; i < tokens.size(); i++) {
                double operand = evaluateOperand(tokens.get(i), context);
                result = i == 1 ? operand : arithmeticOperations.execute(operator, result, operand);
            }
            return String.valueOf(result);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Operands must be numeric or valid expressions: " + tokens);
        }
    }
    
    private double evaluateOperand(String operand, ExecutionContext context) throws Exception {
        if (operand.startsWith("(")) { // Es una expresión anidada
            return Double.parseDouble(eval(operand, context));
        } else { // Es un valor numérico directo
            return Double.parseDouble(operand);
        }
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
        int spaceIndex = expression.indexOf(' ');
        int endIndex = spaceIndex != -1 ? spaceIndex : expression.length() - 1;
        return expression.substring(1, endIndex);
    }
    

    
    private String evaluateFunctionCall(String expression, ExecutionContext context) throws Exception {
        String functionName = getFunctionName(expression);
        FunctionDefinition function = functions.get(functionName);
    
        // Extraer argumentos
        String argsStr = expression.substring(expression.indexOf('(') + functionName.length() + 2, expression.length() - 1);
        List<String> arguments = Arrays.asList(argsStr.split("\\s+"));
    
        ExecutionContext functionContext = new ExecutionContext();
    
        List<String> parameters = function.getParameters();
        if (arguments.size() != parameters.size()) {
            throw new IllegalArgumentException("Incorrect number of arguments for function " + functionName);
        }
    
        for (int i = 0; i < arguments.size(); i++) {
            String param = parameters.get(i);
            // Importante: Evaluar cada argumento en el contexto actual
            String arg = eval(arguments.get(i), context);
            functionContext.setVariable(param, arg);
        }
    
        return eval(function.getBody(), functionContext);
    }

    private String handleQuote(String expression) {
        // Remover el prefijo ' o (QUOTE ) de la expresión
        String quotedExpression = expression.startsWith("'") ?
            expression.substring(1) :
            expression.substring("(QUOTE ".length(), expression.length() - 1); // Excluir el paréntesis final

        // Devolver la expresión tal cual, sin evaluar
        return quotedExpression.trim();
    }

    private List<String> splitExpression(String expression) {
        List<String> tokens = new ArrayList<>();
        int parenthesisCount = 0;
        StringBuilder currentToken = new StringBuilder();
        for (char c : expression.toCharArray()) {
            if (c == '(') parenthesisCount++;
            if (c == ')') parenthesisCount--;
            if (parenthesisCount == 0 && Character.isWhitespace(c)) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                }
            } else {
                currentToken.append(c);
            }
        }
        if (currentToken.length() > 0) tokens.add(currentToken.toString());
        return tokens;
    }

}
