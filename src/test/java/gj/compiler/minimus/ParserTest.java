package gj.compiler.minimus;

import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;
import org.junit.jupiter.api.Test;

public class ParserTest {

    private SimpleTreeNode construct(Parser.Node root) {
        SimpleTreeNode node = new SimpleTreeNode(String.valueOf(root.type) +
                (root.value != null ? "(" + root.value + ")" : ""));
        for (Parser.Node child : root.children) {
            node.addChild(construct(child));
        }
        return node;
    }

    public void print(Parser.Node root) {
        SimpleTreeNode stn = construct(root);
        new TraditionalTreePrinter().print(new BorderTreeNodeDecorator(stn));
    }

    @Test
    public void testSimple() {
        String program = "a=12*(5+2);";
        Parser parser = new Parser(program);
        Parser.Node node = parser.parse();
        print(node);
    }

    @Test
    public void testIf() {
        String program = "{ a=2; b=0; if (a=/=2) { b = 1; } else { b = 2; } }";
        Parser parser = new Parser(program);
        Parser.Node node = parser.parse();
        print(node);
    }

    @Test
    public void testWhile() {
        String program = "{ a = 7; b = a * 2; while (a <= b) {b = b + 1;} }";
        Parser parser = new Parser(program);
        Parser.Node node = parser.parse();
        print(node);
    }

    @Test
    public void testComplexExpression() {
        String program = "{ a=12;b=13;c=a+b*(a-b)+(a+b)-(12-b-a*3+2-1)+(1); }";
        Parser parser = new Parser(program);
        Parser.Node node = parser.parse();
        print(node);
    }

    @Test
    public void testAssignment() {
        String program = "a=b=c=d=12;";
        Parser parser = new Parser(program);
        Parser.Node node = parser.parse();
        print(node);
    }

    @Test
    public void testPrint() {
        String program = "{print(12);}";
        Parser parser = new Parser(program);
        Parser.Node node = parser.parse();
        print(node);
    }

}
