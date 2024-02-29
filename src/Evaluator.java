import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {
    private ArithmeticOperations arithmeticOperations = new ArithmeticOperations();
    private Map<String, FunctionDefinition> functions = new HashMap<>();

    public String eval(String expression, ExecutionContext context) throws Exception {
        expression = expression.trim();
        if (expression.matches("\\((-?\\d+(\\.\\d+)?)\\)")) { // Chequea si es un número entre paréntesis
            return expression.substring(1, expression.length() - 1);
        } else if (expression.matches("-?\\d+(\\.\\d+)?")) { // Chequea si es un número sin paréntesis
            return expression;
        } else if (expression.startsWith("(SETQ")) {
            return handleSetq(expression, context);
        }
        
        if (expression.startsWith("'") || expression.startsWith("(QUOTE ")) {
            return handleQuote(expression);
        }

        if (expression.startsWith("(DEFUN")) {
            defineFunction(expression);
            return "Function defined.";
        }

        String functionName = getFunctionName(expression);
        if (functions.containsKey(functionName)) {
            return evaluateFunctionCall(expression, context);
        }

        if (expression.startsWith("(SETQ")) {
            return handleSetq(expression, context);
        }        

        return handleArithmetic(expression, context);
    }

    private String handleSetq(String expression, ExecutionContext context) throws Exception {
        // Primero, elimina los paréntesis exteriores y luego divide la expresión.
        String trimmedExpression = expression.trim().substring(5, expression.length() - 1).trim();
        
        // Divide la expresión en partes basadas en espacios, esperando al menos 2 partes después de SETQ: nombre de la variable y valor.
        String[] parts = trimmedExpression.split("\\s+", 2);
    
        // Verifica que tengamos al menos dos partes: el nombre de la variable y el valor/la expresión a asignar.
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid SETQ expression: " + expression);
        }
    
        // Nombre de la variable es el primer elemento, y el valor/la expresión a asignar es el segundo.
        String variableName = parts[0];
        String valueExpression = parts[1];
    
        // Evalúa el valor/la expresión a asignar para obtener el valor final.
        String evaluatedValue = eval(valueExpression, context);
    
        // Asigna el valor evaluado a la variable en el contexto.
        context.setVariable(variableName, evaluatedValue);
    
        // Retorna el valor asignado para confirmación.
        return evaluatedValue;
    }
    
    

    private String handleArithmetic(String expression, ExecutionContext context) throws Exception {
        String trimmedExpression = expression.trim().substring(1, expression.length() - 1).trim();
        List<String> tokens = splitArguments(trimmedExpression);

        String operator = tokens.get(0);
        if (tokens.size() < 3) {
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
        } else {
            try {
                // Intenta parsear el operando directamente como un número
                return Double.parseDouble(operand);
            } catch (NumberFormatException e) {
                // Si falla el parseo, asume que es una variable y obtiene su valor del contexto
                String value = context.getVariable(operand);
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Variable " + operand + " is not defined.");
                }
                return Double.parseDouble(value);
            }
        }
    }    

    private void defineFunction(String expression) {
        String withoutDefun = expression.substring(6, expression.length() - 1).trim();
        String[] parts = withoutDefun.split("\\s+", 3);
        String functionName = parts[0];
        List<String> parameters = Arrays.asList(parts[1].replaceAll("[()]", "").split("\\s+"));
        String body = parts[2];

        functions.put(functionName, new FunctionDefinition(parameters, body));
    }

    private String getFunctionName(String expression) {
        int spaceIndex = expression.indexOf(' ');
        int endIndex = spaceIndex != -1 ? spaceIndex : expression.length();
        return expression.substring(1, endIndex);
    }

    private String evaluateFunctionCall(String expression, ExecutionContext context) throws Exception {
        String functionName = getFunctionName(expression);
        FunctionDefinition function = functions.get(functionName);

        String argsStr = expression.substring(expression.indexOf('(') + functionName.length() + 2, expression.length() - 1).trim();
        List<String> arguments = splitArguments(argsStr);

        ExecutionContext functionContext = new ExecutionContext();
        List<String> parameters = function.getParameters();

        if (arguments.size() != parameters.size()) {
            throw new IllegalArgumentException("Incorrect number of arguments for function " + functionName + ". Expected: " + parameters.size() + ", Got: " + arguments.size());
        }

        for (int i = 0; i < arguments.size(); i++) {
            String argValue = eval(arguments.get(i), context);
            functionContext.setVariable(parameters.get(i), argValue);
        }

        return eval(function.getBody(), functionContext);
    }

    private String handleQuote(String expression) {
        String quotedExpression = expression.startsWith("'") ?
            expression.substring(1) :
            expression.substring("(QUOTE ".length(), expression.length() - 1);

        return quotedExpression.trim();
    }

    private List<String> splitArguments(String argsStr) {
        List<String> arguments = new ArrayList<>();
        int start = 0;
        int parenthesisCounter = 0;
        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);
            if (c == '(') parenthesisCounter++;
            if (c == ')') parenthesisCounter--;
            boolean isSpace = c == ' ';
            if ((isSpace && parenthesisCounter == 0) || i == argsStr.length() - 1) {
                String arg = argsStr.substring(start, (isSpace ? i : i + 1)).trim();
                if (!arg.isEmpty()) {
                    arguments.add(arg);
                }
                start = i + 1;
            }
        }
        return arguments;
    }
}