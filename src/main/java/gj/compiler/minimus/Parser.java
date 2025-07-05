package gj.compiler.minimus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gj.compiler.minimus.Scanner.TokenType;
import static gj.compiler.minimus.Scanner.Token;

public class Parser {

    /* It's important not to get confused between the token types and the node
       types.  The node type defines the operation that the subtree can do, and
       it may/may not correlate with a token type. To make a clear distinction,
       node types have "node" as preamble and tokens have "token" */
    public enum NodeType {
        NODE_ASSIGNMENT, NODE_IF, NODE_WHILE, NODE_SEQUENCE, NODE_EMPTY, NODE_PRINT,
        NODE_LESS_THAN, NODE_GREATER_THAN, NODE_LESS_EQUAL_THAN, NODE_GREATER_EQUAL_THAN,
        NODE_EQUALS, NODE_NOT_EQUALS, NODE_ADDITION, NODE_SUBTRACTION, NODE_MULTIPLY, NODE_DIVIDE,
        NODE_VARIABLE, NODE_INTEGER
    }

    /* The node of the syntax tree.  This gets build dynamically as the code
       gets parsed. In the end this representation will make for an easy
       interpreter or can be used to generate code. */
    public static class Node {
        public NodeType type;
        public String value;
        public List<Node> children;

        public Node(NodeType type, String value, Node... child) {
            this.type = type;
            this.value = value;
            this.children = new ArrayList<>(Arrays.asList(child));
        }

        public Node(NodeType type) {
            this(type, null);
        }

        public void addChild(Node child) {
            this.children.add(child);
        }

    }

    private final Scanner scanner;

    public Parser(String program) {
        scanner = new Scanner(program);
    }

    /* The main entry point. */
    public Node parse() {
        return program();
    }

    /**
     * A convenience method that takes an error and formats it with the line
     * number.
     */
    private String e(String e) {
        return String.format("%s (line %d)", e, scanner.getLineNumber());
    }

    /**
     * The main entry point of the parser.  It is convenient to have this
     * non-terminal as it makes it easy to check for invalid code after the
     * program should end.
     * <pre> program ::= statement </pre>
     */
    private Node program() {
        Node statement = statement();

        // This should be the end of the program,
        if (scanner.next().type != TokenType.TOKEN_EOI) {
            throw new RuntimeException(e("Unexpected input after end of program!"));
        }

        return statement;
    }

    /**
     * A statement can start with "if", "while", "{" or ";".  Any other token
     * suggests an expression.  Carefully note the use of "peek()" and "next()".
     * It's VERY easy to lose track of the current token.  If a token has been
     * "peek()"ed, "next()" must still be called to advance to the next token.
     * <pre>
     * statement ::= "if" paren_expr statement ["else" statement] |
     *               "while" paren_expr statement |
     *               "{" { statement } "}" |
     *               expr ";" |
     *               ";"
     * <pre/>
     */
    private Node statement() {
        switch (scanner.peek().type) {
            case TOKEN_IF:
                scanner.next(); // discards known "if"
                Node statement = new Node(NodeType.NODE_IF, null, parenthesisExpression(), statement());
                if (scanner.peek().type == TokenType.TOKEN_ELSE) { // optional "else"
                    scanner.next(); // discards known "else" token
                    statement.addChild(statement()); // add the "else"
                }
                return statement;
            case TOKEN_WHILE:
                scanner.next(); // discards known "while" token
                return new Node(NodeType.NODE_WHILE, null, parenthesisExpression(), statement());
            case TOKEN_CURLY_OPEN:
                scanner.next(); // discards known "{" token

                // A statement starts either with: "if", "while", "print", "{", "id" or ";"
                TokenType[] tokens = new TokenType[]{TokenType.TOKEN_IF, TokenType.TOKEN_WHILE, TokenType.TOKEN_PRINT,
                        TokenType.TOKEN_CURLY_OPEN, TokenType.TOKEN_ID, TokenType.TOKEN_SEMICOLON};
                Node sequence = new Node(NodeType.NODE_SEQUENCE);
                while (Arrays.asList(tokens).contains(scanner.peek().type)) {
                    sequence.addChild(statement());
                }
                if (scanner.next().type != TokenType.TOKEN_CURLY_CLOSE) { // discards known "}" token
                    throw new RuntimeException(e("} expected or valid statement!"));
                }

                return sequence;
            case TOKEN_SEMICOLON:
                scanner.next(); // discards known ";" token
                return new Node(NodeType.NODE_EMPTY, null);
            case TOKEN_PRINT:
                scanner.next(); // discards known "print" token
                if (scanner.next().type != TokenType.TOKEN_PAREN_OPEN) {
                    throw new RuntimeException(e("( expected!"));
                }
                Node whatToPrint = expression();
                if (scanner.next().type != TokenType.TOKEN_PAREN_CLOSE) {
                    throw new RuntimeException(e(") expected!"));
                }
                if (scanner.next().type != TokenType.TOKEN_SEMICOLON) { // discards known "}" token
                    throw new RuntimeException(e("; expected!"));
                }
                return new Node(NodeType.NODE_PRINT, null, whatToPrint);
            default: // expression will check if syntactical correct
                Node expr = expression();
                if (scanner.next().type != TokenType.TOKEN_SEMICOLON) {
                    throw new RuntimeException(e("; expected!"));
                }
                return expr;
        }
    }

    /**
     * A parenthesisExpression is simply an expression inside parenthesis. There is no need to preserve the "brackets"
     * as the location within the tree will enforce precedence automatically.
     * <pre> paren_expr ::= "(" expr ")" </pre>
     */
    private Node parenthesisExpression() {
        if (scanner.next().type != TokenType.TOKEN_PAREN_OPEN) {
            throw new RuntimeException(e("( expected!"));
        }
        Node node = expression();
        if (scanner.next().type != TokenType.TOKEN_PAREN_CLOSE) {
            throw new RuntimeException(e(") expected!"));
        }
        return node;
    }

    /**
     * An expression is either an "assignment" or a "test()".  An assignment starts with an "id", followed by an "=".  A
     * "test()" can start with an "id" as well. This needs some "juggling" to get right: If the next token is an "id",
     * assume that it is a "test()" (and not an assignment), and check the type of the returned Node as well as the next
     * token in the stream. If the returned Node is only a variable and the next token is an "equal" sign, we have an
     * assignment.  Repurpose the node accordingly.
     * <pre> expr ::= id "=" expr | test </pre>
     **/
    private Node expression() {
        // If the next token is not an "id", it has to be a "test()".
        if (scanner.peek().type != TokenType.TOKEN_ID) {
            return test();
        }

        // It could either be an "assignment" or a "test()".
        Node tmp = test();
        if (tmp.type == NodeType.NODE_VARIABLE && scanner.peek().type == TokenType.TOKEN_ASSIGNMENT) {
            // It's an assignment, so we need to repurpose the returned node.
            scanner.next(); // discard known "assignment"
            return new Node(NodeType.NODE_ASSIGNMENT, tmp.value, expression());
        }

        // It was a "test()".
        return tmp;
    }

    /**
     * A test is either a sum, or a test between two sums.
     * <pre>
     * test ::= sum [ rel sum ]
     * rel ::= "<" | ">" | "<=" | ">=" | "==" | "=/="
     * </pre>
     */
    private Node test() {
        Node sum = sum();

        TokenType[] relOperation = new TokenType[]{TokenType.TOKEN_LESS_THAN, TokenType.TOKEN_GREATER_THAN,
                TokenType.TOKEN_LESS_EQUAL_THAN, TokenType.TOKEN_GREATER_EQUAL_THAN,
                TokenType.TOKEN_EQUALS, TokenType.TOKEN_NOT_EQUALS};

        if (Arrays.asList(relOperation).contains(scanner.peek().type)) {
            Token cmp = scanner.next();
            Node right = sum();
            switch (cmp.type) {
                case TOKEN_LESS_THAN:
                    return new Node(NodeType.NODE_LESS_THAN, null, sum, right);
                case TOKEN_GREATER_THAN:
                    return new Node(NodeType.NODE_GREATER_THAN, null, sum, right);
                case TOKEN_LESS_EQUAL_THAN:
                    return new Node(NodeType.NODE_LESS_EQUAL_THAN, null, sum, right);
                case TOKEN_GREATER_EQUAL_THAN:
                    return new Node(NodeType.NODE_GREATER_EQUAL_THAN, null, sum, right);
                case TOKEN_EQUALS:
                    return new Node(NodeType.NODE_EQUALS, null, sum, right);
                case TOKEN_NOT_EQUALS:
                    return new Node(NodeType.NODE_NOT_EQUALS, null, sum, right);
            }
        }
        return sum;
    }

    /**
     * A sum is series of additions and subtractions evaluated from left to right.
     * <pre>sum ::= factor { "+" | "-" factor }</pre>
     */
    private Node sum() {
        Node sum = factor();
        TokenType[] sumOperation = new TokenType[]{TokenType.TOKEN_PLUS, TokenType.TOKEN_MINUS};
        while (Arrays.asList(sumOperation).contains(scanner.peek().type)) {
            switch (scanner.next().type) { // guaranteed to be "plus" or "minus"
                case TOKEN_PLUS:
                    sum = new Node(NodeType.NODE_ADDITION, null, sum, factor());
                    break;
                case TOKEN_MINUS:
                    sum = new Node(NodeType.NODE_SUBTRACTION, null, sum, factor());
                    break;
            }
        }
        return sum;
    }

    /**
     * A factor is series of multiplications and divisions evaluated from left to right.
     * <pre> factor ::= term { "*" | "/" term } </pre>
     */
    private Node factor() {
        Node factor = term();

        TokenType[] factorOperation = new TokenType[]{TokenType.TOKEN_MULTIPLY, TokenType.TOKEN_DIVIDE};
        while (Arrays.asList(factorOperation).contains(scanner.peek().type)) {
            switch (scanner.next().type) { // guaranteed to be "multiply" or "divide"
                case TOKEN_MULTIPLY:
                    factor = new Node(NodeType.NODE_MULTIPLY, null, factor, term());
                    break;
                case TOKEN_DIVIDE:
                    factor = new Node(NodeType.NODE_DIVIDE, null, factor, term());
                    break;
            }
        }
        return factor;
    }

    /**
     * A term is either an id and integer or an expression in parentheses.
     * <pre> term ::= id | int | paren_expr </pre>
     */
    private Node term() {
        if (scanner.peek().type == TokenType.TOKEN_ID) {
            return id();
        } else if (scanner.peek().type == TokenType.TOKEN_INT) {
            return integer();
        }

        // For improved error-checking
        if (scanner.peek().type != TokenType.TOKEN_PAREN_OPEN) {
            throw new RuntimeException(e("Expected id, integer or expression."));
        }

        return parenthesisExpression();
    }

    /**
     * A reference to a variable.
     * <pre> id ::= "a" | "b" | "c" | "d" | ... | "z" </pre>
     */
    private Node id() {
        return new Node(NodeType.NODE_VARIABLE, scanner.next().value);
    }

    /**
     * A integer literal.
     * <pre> int ::= an unsigned integer </pre>
     **/
    private Node integer() {
        return new Node(NodeType.NODE_INTEGER, scanner.next().value);
    }

}
