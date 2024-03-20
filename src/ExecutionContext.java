import java.util.HashMap;
import java.util.Map;

public class ExecutionContext {
    private Map<String, String> variables = new HashMap<>();

    public void setVariable(String name, String value) {
        variables.put(name, value);
    }
    
    public String getVariable(String name) {
        return variables.getOrDefault(name, "NIL"); // Retorna "NIL" si la variable no existe
    }    

    public Map<String, String> getVariables(){
        return variables;
    }

    // Método para clonar el contexto actual, útil para llamadas a funciones
    public ExecutionContext clone() {
        ExecutionContext newContext = new ExecutionContext();
        newContext.variables.putAll(this.variables);
        return newContext;
    }
}
