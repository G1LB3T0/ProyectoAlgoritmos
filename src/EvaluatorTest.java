

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EvaluatorTest {
    private Evaluator evaluator;
    private ExecutionContext context;

    @Before
    public void setUp() {
        evaluator = new Evaluator();
        context = new ExecutionContext(); // Asegúrate de que el contexto esté inicializado correctamente
    }

    @Test
    public void testAtomTrue() throws Exception {
        assertEquals("T", evaluator.eval("(ATOM 1)", context));
    }

    @Test
    public void testList() throws Exception {
        assertEquals("T", evaluator.eval("(LIST 1 2 3)", context));
    }

    @Test
    public void testEqualTrue() throws Exception {
        assertEquals("T", evaluator.eval("(EQUAL 5 5)", context));
    }

    @Test
    public void testEqualFalse() throws Exception {
        assertEquals("NIL", evaluator.eval("(EQUAL 5 3)", context));
    }

    @Test
    public void testSetq() throws Exception {
        assertEquals("10", evaluator.eval("(SETQ X 10)", context));
        assertEquals("10", context.getVariable("X"));
    }

    @Test
    public void testIfTrue() throws Exception {
        evaluator.eval("(SETQ X 10)", context);
        assertEquals("Verdadero", evaluator.eval("(IF (= 10 10) (QUOTE Verdadero) (QUOTE Falso))", context));
    }

    @Test
    public void testIfFalse() throws Exception {
        assertEquals("No", evaluator.eval("(IF (= 10 1) Hola No)", context));
    }

    // Aquí habrá que definir las funciones antes de llamarlas, por ejemplo:
    @Test
    public void testFactorial() throws Exception {
        evaluator.eval("(DEFUN FACT (N) (IF (= N 0) 1 (* N (FACT (- N 1)))))", context);
        assertEquals("120.0", evaluator.eval("(FACT 5)", context));
    }

    @Test
    public void testFibonacci() throws Exception {
        evaluator.eval("(DEFUN FIB (N) (IF (= N 0) 0 (IF (= N 1) 1 (+ (FIB (- N 1)) (FIB (- N 2))))))", context);
        assertEquals("5.0", evaluator.eval("(FIB 5)", context));
    }
}

