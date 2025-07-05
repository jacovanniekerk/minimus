package gj.compiler.minimus;

import java.util.HashMap;
import java.util.Map;

import static gj.compiler.minimus.Parser.Node;

public class Interpreter {

    private static Parser parser;

    public Interpreter(String program){
        parser = new Parser(program);
    }

    public Map<String, Integer> run() {
        Node node = parser.parse();
        return execute(node);
    }

    private Map<String, Integer> execute(Node entireProgram) {
        /* The symbol table in Minimus can be handles by a simple map (it only
           supports one type.  For more complex languages, elaborate symbol
           tables that also does type checking, etc. is definitely possible. */
        Map<String, Integer> symbolTable = new HashMap<>();

        /* The SyntaxTree starts with a statement or derivation of an
           expression. */
        int executionResult = handleNode(entireProgram, symbolTable);

        /* Since Minimus did not initially have any output instruction, the
           symbol table was always printed out - this just continues that. */
        System.out.println(symbolTable);
        System.out.println("Execution result: " + executionResult);
        return symbolTable;
    }

    /* Here, check the type of the node and does what it says.  Since Minimnus
       only supports "ints", we can simply return "int"  here.  Typically,
       "type-checking" will occur at this step to make sure the types are
       correct. At this point NO syntax checking is necessary, since that's the
       parser's job. */
    private int handleNode(Node root, Map<String, Integer> symbolTable) {
        switch (root.type) {
            case NODE_ASSIGNMENT:
                return handleAssignment(root, symbolTable);
            case NODE_IF:
                return handleIf(root, symbolTable);
            case NODE_WHILE:
                return handleWhile(root, symbolTable);
            case NODE_PRINT:
                return handlePrint(root, symbolTable);
            case NODE_SEQUENCE:
                return handleSequence(root, symbolTable);
            case NODE_EMPTY:
                return 0;
            case NODE_LESS_THAN:
            case NODE_GREATER_THAN:
            case NODE_LESS_EQUAL_THAN:
            case NODE_GREATER_EQUAL_THAN:
            case NODE_EQUALS:
            case NODE_NOT_EQUALS:
            case NODE_ADDITION:
            case NODE_SUBTRACTION:
            case NODE_MULTIPLY:
            case NODE_DIVIDE:
                return handleLeftRightOperation(root, symbolTable);
            case NODE_VARIABLE:
                return handleVariable(root, symbolTable);
            case NODE_INTEGER:
                return Integer.parseInt(root.value);
        }
        return 0;
    }

    /* Handles NODE_ASSIGNMENT: ALWAYS a single child that is of type
       "expression".  Evaluate the root and set the value in the symbol
       table. Since Minimus supports chained assignments "a=b=c=12", this
       method needs to return the type of the assignment.  For languages
       with different types (i.e. strings, ints, floats), type checking needs
       to happen here as well. */
    private int handleAssignment(Node root, Map<String, Integer> symbolTable) {
        int value = handleNode(root.children.get(0), symbolTable);
        symbolTable.put(root.value, value);
        return value;
    }

    /* Handles NODE_IF: First child is the "expression".  If the value is
       non-zero, then execute the 2nd child ("then"), and the 3rd child
       if it is 0 (the "else"). Method always returns 0. */
    private int handleIf(Node root, Map<String, Integer> symbolTable) {
        int condition = handleNode(root.children.get(0), symbolTable);
        if (condition != 0) {
            return handleNode(root.children.get(1), symbolTable);
        } else {
            // The else part is only executed if that child exists.
            if (root.children.size() > 2) {
                return handleNode(root.children.get(2), symbolTable);
            }
        }
        return 0;
    }

    /* Handles NODE_WHILE: First child is the expression, and while it is
       non-zero, execute the statement in the 2nd child. Always returns 0. */
    private int handleWhile(Node root, Map<String, Integer> symbolTable) {
        while(handleNode(root.children.get(0), symbolTable) != 0) {
            handleNode(root.children.get(1), symbolTable);
        }
        return 0;
    }

    /* Handles NODE_PRINT: First child is the item to print to screen,
       always returns 0. */
    private int handlePrint(Node root, Map<String, Integer> symbolTable) {
        int whatToPrint = handleNode(root.children.get(0), symbolTable);
        System.out.println(whatToPrint);
        return 0;
    }

    /* Handles NODE_SEQUENCE: So a sequence simply executes children (which are
       all statements) left to right. Returns 0. */
    private int handleSequence(Node root, Map<String, Integer> symbolTable) {
        for (Node child : root.children) {
            handleNode(child, symbolTable);
        }
        return 0;
    }

    /* Handles NODE_LESS_THAN, NODE_GREATER_THAN, NODE_LESS_EQUAL_THAN,
       NODE_GREATER_EQUAL_THAN, NODE_EQUALS, NODE_NOT_EQUALS, NODE_ADDITION,
       NODE_SUBTRACTION, NODE_MULTIPLY, NODE_DIVIDE.

       Since these  operations are so alike, we can easily throw them into a
       single function. More elaborate languages may need to split them
       especially if type checking is required. */
    private int handleLeftRightOperation(Node root, Map<String, Integer> symbolTable) {
        int lh = handleNode(root.children.get(0), symbolTable);
        int rh = handleNode(root.children.get(1), symbolTable);

        switch (root.type) {
            case NODE_LESS_THAN:
                return lh < rh ? 1 : 0;
            case NODE_GREATER_THAN:
                return lh > rh ? 1 : 0;
            case NODE_LESS_EQUAL_THAN:
                return lh <= rh ? 1 : 0;
            case NODE_GREATER_EQUAL_THAN:
                return lh >= rh ? 1 : 0;
            case NODE_EQUALS:
                return lh == rh ? 1 : 0;
            case NODE_NOT_EQUALS:
                return lh != rh ? 1 : 0;
            case NODE_ADDITION:
                return lh + rh;
            case NODE_SUBTRACTION:
                return lh - rh;
            case NODE_MULTIPLY:
                return lh * rh;
            case NODE_DIVIDE:
                return lh / rh;
        }
        throw new RuntimeException("Unsupported comparison/operation!");
    }

    /* Handles NODE_VARIABLE: Looks up and returns the value. */
    private int handleVariable(Node root, Map<String, Integer> symbolTable) {
        if (!symbolTable.containsKey(root.value)) {
            throw new RuntimeException(String.format("Variable %s is undefined!", root.value));
        }
        return symbolTable.get(root.value);
    }

}
