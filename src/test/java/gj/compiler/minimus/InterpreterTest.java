package gj.compiler.minimus;

import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;
import org.junit.jupiter.api.Test;

public class InterpreterTest {

    @Test
    public void testSimple() {
        String program = "{ a=12*(5+2); }";
        Interpreter i = new Interpreter((program));
        i.run();
    }

    @Test
    public void testWhile() {
        String program = "{ b=1; a=2; while (b < 10) { a=a*2; b=b+1; }}";
        Interpreter i = new Interpreter((program));
        i.run();
    }

    @Test
    public void testPrint() {
        String program = "{ a = 2; while (a <= 100) { print(a*a); a = a + 1; } }";
        Interpreter i = new Interpreter((program));
        i.run();
    }

}
