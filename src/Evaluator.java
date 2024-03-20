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
        } else if (expression.matches("\\((<|>|=)\\s+[^\\s]+\\s+[^\\s]+\\)")) {
            return handleComparison(expression, context);
        } else if (expression.matches("\\((-?\\d+(\\.\\d+)?)\\)")) { // Chequea si es un número entre paréntesis
            return expression.substring(1, expression.length() - 1);
        } else if (expression.matches("-?\\d+(\\.\\d+)?")) { // Chequea si es un número sin paréntesis
            return expression;
        } else if (expression.startsWith("(SETQ")) {
            return handleSetq(expression, context);
        } else if (expression.startsWith("'") || expression.startsWith("(QUOTE ") || expression.startsWith("(' ")) {
            return handleQuote(expression, context);
        }

        if (expression.startsWith("(DEFUN")) {
            defineFunction(expression);
            return "Function defined.";
        } else if (expression.startsWith("(IF") || expression.startsWith("(COND ")) {
            return handleCondition(expression, context); // Llama a evalIfStatement para manejar el IF
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
        String trimmedExpression = expression.trim().substring(5, expression.length() - 1).trim();
        String[] parts = trimmedExpression.split("\\s+", 2);
    
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid SETQ expression: " + expression);
        }
    
        String variableName = parts[0];
        String valueExpression = parts[1];
    
        // Si el valor a asignar es una lista o un valor simple
        if (valueExpression.startsWith("(") && valueExpression.endsWith(")")) {
            // Para listas, podrías optar por evaluar o simplemente almacenar la expresión literal
            // Aquí estamos eligiendo almacenar la expresión literal sin evaluar
            context.setVariable(variableName, valueExpression);
            return valueExpression;
        } else {
            // Si no es una lista, evalúa la expresión para obtener el valor final y luego asigna ese valor a la variable
            String evaluatedValue = eval(valueExpression, context);
            context.setVariable(variableName, evaluatedValue);
            return evaluatedValue;
        }
    }    
       
    private String handleAtom(String expression, ExecutionContext context) throws Exception {
        String argument = extractCompleteArgument(expression);
        
        // Consulta si la variable existe en el contexto. Si no existe, "value" será null.
        String value = context.getVariables().containsKey(argument) ? context.getVariable(argument) : "No definido";
    
        // Actualiza el argumento solo si la variable existe en el contexto y tiene un valor diferente de "NIL".
        if (!value.equals("No definido") && !value.equals("NIL")) {
            argument = value;
        }
    
    
        // Verifica si el argumento representa directamente una lista
        if (argument.startsWith("(") && argument.endsWith(")")) {
            return "NIL"; // Es una lista, por lo tanto, no es un átomo
        }
    
        // Intenta determinar si el argumento es un número
        try {
            Double.parseDouble(argument);
            return "T"; // Es un átomo si es un número
        } catch (NumberFormatException e) {
            // Si no es un número, verifica si el argumento es un símbolo válido
            if (argument.matches("[a-zA-Z]+") || value.equals("NIL")) {
                return "T"; // Es un átomo si es un símbolo o "NIL"
            } else {
                return "NIL"; // No es un átomo si no cumple los criterios anteriores
            }
        }
    }
    
        
    
    private String extractCompleteArgument(String expression) {
        expression = expression.trim();
        // Busca el inicio del argumento después del primer espacio y hasta el último paréntesis
        int firstSpaceIndex = expression.indexOf(' ') + 1;
        // El argumento es lo que está después del nombre del comando, incluyendo los paréntesis
        return expression.substring(firstSpaceIndex, expression.length() - 1).trim();
    }
                   
    
    private String handleList(String expression, ExecutionContext context) throws Exception {
        // Remueve la palabra "LIST" si está presente y los paréntesis exteriores.
        // También maneja el caso de que la lista esté precedida por una comilla simple para evitar evaluación.
        boolean evaluateItems = true;
        if (expression.startsWith("(LIST")) {
            expression = expression.substring(5, expression.length() - 1).trim();
        } else if (expression.startsWith("'")) {
            evaluateItems = false;
            expression = expression.substring(2, expression.length() - 1).trim();
        } else {
            expression = expression.substring(1, expression.length() - 1).trim();
        }
    
        // Divide los elementos de la lista
        List<String> elements = splitArguments(expression);
        StringBuilder listRepresentation = new StringBuilder("(");
    
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);
            if (evaluateItems) {
                // Evalúa cada elemento si se debe evaluar
                element = eval(element, context);
            }
            listRepresentation.append(element);
            if (i < elements.size() - 1) {
                listRepresentation.append(" ");
            }
        }
        listRepresentation.append(")");
    
        return listRepresentation.toString();
    }
    
    
    private String handleEqual(String expression, ExecutionContext context) throws Exception {
        // Extrae los dos argumentos
        String[] arguments = extractArguments(expression, 2);
    
        // Evalúa las expresiones y toma en cuenta las variables
        String value1 = evaluateExpression(arguments[0], context);
        String value2 = evaluateExpression(arguments[1], context);
    
        // Compara los valores obtenidos
        return value1.equals(value2) ? "T" : "NIL";
    }
    
    private String evaluateExpression(String expression, ExecutionContext context) throws Exception {
        // Si es un número, simplemente devuelve el número como una cadena
        if (expression.matches("-?\\d+(\\.\\d+)?")) {
            return expression;
        }
    
        // Si es una variable, obtiene su valor del contexto y evalúa nuevamente la expresión
        String value = context.getVariable(expression);
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Variable " + expression + " is not defined.");
        }
        return eval(value, context); // Evalúa nuevamente la expresión
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
        boolean result;
        switch (operator) {
            case "<":
                result = operand1 < operand2;
                break;
            case ">":
                result = operand1 > operand2;
                break;
            case "=":
                result = operand1 == operand2;
                break;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    
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
            String arg = arguments.get(i);
            // Verifica si el argumento es una expresión o una referencia directa a una variable.
            if (arg.matches("-?\\d+(\\.\\d+)?") || arg.startsWith("(")) {
                // Si el argumento es un número o una expresión, evalúa antes de pasarlo.
                String evaluatedArg = eval(arg, context);
                functionContext.setVariable(function.getParameters().get(i), evaluatedArg);
            } else {
                // Si es un nombre de variable, pasa el argumento directamente.
                // Verifica si la variable existe en el contexto global antes de usar su nombre como valor.
                String value = context.getVariable(arg);
                if (!value.isEmpty()) {
                    functionContext.setVariable(function.getParameters().get(i), value);
                } else {
                    // Si la variable no está definida en el contexto global, trata el argumento como una cadena literal.
                    functionContext.setVariable(function.getParameters().get(i), arg);
                }
            }
        }
    
        return eval(function.getBody(), functionContext);
    }
    
    private String getFunctionName(String expression) {
        int spaceIndex = expression.indexOf(' ');
        int endIndex = spaceIndex != -1 ? spaceIndex : expression.length();
        return expression.substring(1, endIndex);
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

    private String handleQuote(String expression, ExecutionContext context) throws Exception {
        String quotedExpression;
        if (expression.startsWith("'")) {
            quotedExpression = expression.substring(1);
        } else {
            quotedExpression = expression.substring("(QUOTE ".length(), expression.length() - 1);
        }
        
        quotedExpression = quotedExpression.trim();
    
        // Verifica si la expresión está entre comillas dobles para tratarla como un literal
        if (quotedExpression.startsWith("\"") && quotedExpression.endsWith("\"")) {
            // Si está entre comillas, devuelve el contenido sin las comillas
            return quotedExpression.substring(1, quotedExpression.length() - 1);
        } else {
            // Si no está entre comillas, verifica si es una variable en el contexto
            String value = context.getVariable(quotedExpression);
            if (!value.isEmpty()) {
                // Si la expresión es una variable definida, devuelve su valor
                return value;
            } else {
                // Si no es una variable definida, devuelve la expresión literal
                return quotedExpression;
            }
        }
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

    private String handleCondition(String expression, ExecutionContext context) throws Exception {
        String code = expression.substring(4, expression.length() - 1);
    
        // Divide la expresión en sus partes, considerando los condicionales IF anidados
        List<String> parts = splitArguments(code);
    
        if (parts.size() != 3) {
            throw new IllegalArgumentException("IF statement must have exactly 3 parts");
        }
    
        String conditionResult = eval(parts.get(0), context);
    
        // Evalúa recursivamente la rama que cumple la condición
        if (conditionResult.equals("T")) {
            return evalRecursively(parts.get(1), context);
        } else {
            return evalRecursively(parts.get(2), context);
        }
    }
    
    // Función auxiliar para evaluar expresiones recursivamente
    private String evalRecursively(String expression, ExecutionContext context) throws Exception {
        if (expression.startsWith("(")) {
            // Si la expresión comienza con '(', es una expresión compuesta
            return eval(expression, context);
        } else {
            // Si no, simplemente es un átomo o una expresión simple
            return expression;
        }
    }    
}
