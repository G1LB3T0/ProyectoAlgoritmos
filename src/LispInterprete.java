import java.util.List;

public class LispInterprete {

    public Object eval(Object expr) {
        if (expr instanceof Integer) {
            // Números se retornan directamente
            return expr;
        } else if (expr instanceof List) {
            List<?> list = (List<?>) expr;
            String operator = (String) list.get(0);
            switch (operator) {
                case "+":
                    return (Integer) eval(list.get(1)) + (Integer) eval(list.get(2));
                case "-":
                    return (Integer) eval(list.get(1)) - (Integer) eval(list.get(2));
                // Agrega más operadores según sea necesario
                default:
                    throw new IllegalArgumentException("Operador desconocido: " + operator);
            }
        } else {
            throw new IllegalArgumentException("Expresión desconocida: " + expr);
        }
    }

    public static void main(String[] args) {
        LispInterprete interpreter = new LispInterprete();

        // Ejemplo de uso: (convert-f-to-c 100)
        List<Object> convertFtoCExpr = List.of("/", List.of("-", "f", 32), 5, 9);
        System.out.println(interpreter.eval(convertFtoCExpr));

        // Agrega más ejemplos de llamadas a eval aquí
    }
}
