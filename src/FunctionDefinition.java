import java.util.List;

public class FunctionDefinition {
    private List<String> parameters;
    private String body;

    public FunctionDefinition(List<String> parameters, String body) {
        this.parameters = parameters;
        this.body = body;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getBody() {
        return body;
    }
}
