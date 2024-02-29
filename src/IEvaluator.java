public interface IEvaluator {
    String eval(String expression, ExecutionContext context) throws Exception;    
}