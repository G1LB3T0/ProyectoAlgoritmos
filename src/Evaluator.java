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
        if (expression.startsWith("(ATOM")) {
            return handleAtom(expression, context);
        } else if (expression.startsWith("(LIST")) {
            return handleList(expression, context);
        } else if (expression.startsWith("(EQUAL")) {
            return handleEqual(expression, context);
        } else if (expression.matches("\\((<|>)\\s+[^\\s]+\\s+[^\\s]+\\)")) {
            return handleComparison(expression, context);
        } else if (expression.matches("\\((-?\\d+(\\.\\d+)?)\\)")) { // Chequea si es un número entre paréntesis
            return expression.substring(1, expression.length() - 1);
        } else if (expression.matches("-?\\d+(\\.\\d+)?")) { // Chequea si es un número sin paréntesis
            return expression;
        } else if (expression.startsWith("(SETQ")) {
            return handleSetq(expression, context);
        } else if (expression.startsWith("'") || expression.startsWith("(QUOTE ")) {
            return handleQuote(expression);
        } else if (expression.startsWith("(DEFUN")) {
            defineFunction(expression);
            return "Function defined.";
        } else if (expression.startsWith("(IF")) {
            return handleIf(expression, context); // Llama a evalIfStatement para manejar el IF
        } else {
            String functionName = getFunctionName(expression);
            if (functions.containsKey(functionName)) {
                return evaluateFunctionCall(expression, context);
            } else if (expression.startsWith("(SETQ")) {
                return handleSetq(expression, context);
            } else {
                return handleArithmetic(expression, context);
            }
        }
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
    
    private String handleAtom(String expression, ExecutionContext context) throws Exception {
        String argument = extractArgument(expression); // Implementa esta función para extraer el único argumento de la expresión
        try {
            Double.parseDouble(argument); // Intenta parsear como número
            return "T"; // Es un átomo si es un número
        } catch (NumberFormatException e) {
            return argument.matches("[a-zA-Z]+") ? "T" : "NIL"; // Es un átomo si es una palabra sin espacios
        }
    }
    
    private String handleList(String expression, ExecutionContext context) throws Exception {
        // Lista siempre retorna T porque cualquier expresión dentro de (LIST ...) se considera una lista
        return "T";
    }
    
    private String handleEqual(String expression, ExecutionContext context) throws Exception {
        // Extrae los dos argumentos y compáralos
        String[] arguments = extractArguments(expression, 2); // Implementa esta función para extraer exactamente dos argumentos
        double operand1 = Double.parseDouble(eval(arguments[0], context));
        double operand2 = Double.parseDouble(eval(arguments[1], context));
        return operand1 == operand2 ? "T" : "NIL";
    }

    private String handleComparison(String expression, ExecutionContext context) throws Exception {
        String operator = expression.substring(1, 2);
        String[] arguments = extractArguments(expression, 2);
        double operand1;
        double operand2;

        // Evalúa las expresiones y toma en cuenta las variables
        if (arguments[0].matches("-?\\d+(\\.\\d+)?")) {
            operand1 = Double.parseDouble(arguments[0]);  // Si es un número, simplemente lo conviertes
        } else {
            operand1 = Double.parseDouble(context.getVariable(arguments[0])); // Si es una variable, obtienes su valor del contexto
        }

        if (arguments[1].matches("-?\\d+(\\.\\d+)?")) {
            operand2 = Double.parseDouble(arguments[1]);  // Si es un número, simplemente lo conviertes
        } else {
            operand2 = Double.parseDouble(context.getVariable(arguments[1])); // Si es una variable, obtienes su valor del contexto
        }

        // Realiza la comparación
        boolean result = operator.equals("<") ? operand1 < operand2 : operand1 > operand2;

        return result ? "T" : "NIL";
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
        int firstSpaceIndex = withoutDefun.indexOf(' ');
        int paramsStartIndex = withoutDefun.indexOf('(', firstSpaceIndex);
        int paramsEndIndex = withoutDefun.indexOf(')', paramsStartIndex);
        
        if (firstSpaceIndex == -1 || paramsStartIndex == -1 || paramsEndIndex == -1) {
            throw new IllegalArgumentException("Function definition is malformed: " + expression);
        }
    
        String functionName = withoutDefun.substring(0, firstSpaceIndex).trim();
        String paramsString = withoutDefun.substring(paramsStartIndex + 1, paramsEndIndex).trim();
        String body = withoutDefun.substring(paramsEndIndex + 1).trim();
    
        if (functionName.isEmpty() || paramsString.isEmpty() || body.isEmpty()) {
            throw new IllegalArgumentException("Function definition requires a name, parameters, and a body.");
        }
    
        List<String> parameters = Arrays.asList(paramsString.split("\\s+"));
        
        functions.put(functionName, new FunctionDefinition(parameters, body));
    }    
    
    private String evaluateFunctionCall(String expression, ExecutionContext context) throws Exception {
        String functionName = getFunctionName(expression);
        FunctionDefinition function = functions.get(functionName);
    
        String argsStr = expression.substring(expression.indexOf('(') + functionName.length() + 2, expression.length() - 1).trim();
        List<String> arguments = splitArguments(argsStr);
    
        if (arguments.size() != function.getParameters().size()) {
            throw new IllegalArgumentException("Incorrect number of arguments for function " + functionName + ". Expected: " + function.getParameters().size() + ", Got: " + arguments.size());
        }
    
        ExecutionContext functionContext = new ExecutionContext();
        for (int i = 0; i < arguments.size(); i++) {
            // Reemplaza la llamada a eval con una lógica similar a evaluateOperand
            // para manejar específicamente nombres de variables y expresiones anidadas.
            String arg = arguments.get(i);
            String argValue;
            if (arg.matches("-?\\d+(\\.\\d+)?")) { // Es un número
                argValue = arg;
            } else {
                // Intenta obtener el valor de la variable del contexto si no es un número.
                argValue = context.getVariable(arg);
                if (argValue.equals("NIL")) {
                    throw new IllegalArgumentException("Variable " + arg + " is not defined.");
                }
            }
            functionContext.setVariable(function.getParameters().get(i), argValue);
        }
    
        return eval(function.getBody(), functionContext);
    }    

    private String getFunctionName(String expression) {
        int spaceIndex = expression.indexOf(' ');
        int endIndex = spaceIndex != -1 ? spaceIndex : expression.length();
        return expression.substring(1, endIndex);
    }

    private String extractArgument(String expression) {
        // Esta función asume que se extrará el primer argumento después del operador
        String trimmedExpression = expression.trim().substring(expression.indexOf(' ') + 1, expression.length() - 1).trim();
        return extractFirstArgument(trimmedExpression);
    }
    
    private String[] extractArguments(String expression, int expectedArgs) {
        // Extrae y devuelve exactamente expectedArgs argumentos de la expresión
        List<String> arguments = new ArrayList<>();
        String argsStr = expression.trim().substring(expression.indexOf(' ') + 1, expression.length() - 1).trim();
        while (!argsStr.isEmpty() && arguments.size() < expectedArgs) {
            String arg = extractFirstArgument(argsStr);
            arguments.add(arg);
            argsStr = argsStr.substring(arg.length()).trim();
            if (argsStr.startsWith(" ")) {
                argsStr = argsStr.substring(1);
            }
        }
        return arguments.toArray(new String[0]);
    }
    
    private String extractFirstArgument(String argsStr) {
        if (argsStr.startsWith("(")) {
            int parenCount = 1;
            for (int i = 1; i < argsStr.length(); i++) {
                if (argsStr.charAt(i) == '(') {
                    parenCount++;
                } else if (argsStr.charAt(i) == ')') {
                    parenCount--;
                    if (parenCount == 0) {
                        return argsStr.substring(0, i + 1);
                    }
                }
            }
        } else {
            int firstSpace = argsStr.indexOf(' ');
            int firstParen = argsStr.indexOf('(');
            if (firstSpace == -1) firstSpace = argsStr.length();
            if (firstParen == -1) firstParen = argsStr.length();
            return argsStr.substring(0, Math.min(firstSpace, firstParen));
        }
        return argsStr; // Fallback, debería manejarse adecuadamente en el código que llama
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
    private String handleIf(String expression, ExecutionContext context) throws Exception {
        String codigo = expression.substring(4, expression.length() - 1);

        String[] partes = codigo.split("(?=\\()");

        if (partes.length != 3) {
            throw new IllegalArgumentException("IF statement must have exactly 3 parts");
        }

        String conditionResult = eval(partes[0], context);

        String parteAEvaluar = conditionResult.equals("T") ? partes[1] : partes[2];

        return eval(parteAEvaluar, context);
    }



    private void executeAction(String action) {
        // Aquí puedes implementar la lógica para ejecutar la acción correspondiente
        System.out.println("Ejecutando acción: " + action);
    }



}