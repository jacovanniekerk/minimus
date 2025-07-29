package gj.compiler.minimus;

import org.junit.jupiter.api.Test;

import static gj.compiler.minimus.Scanner.TokenType;
import static gj.compiler.minimus.Scanner.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScannerTest {

    private void actualTest(Scanner scanner, TokenType[] expected) {
        for (TokenType e : expected) {
            assertEquals(e, scanner.peek().type);
            System.out.println(scanner.next());
        }
    }

    @Test
    public void testTest() {
        TokenType[] expected = {TOKEN_SEMICOLON, TOKEN_SEMICOLON, TOKEN_SEMICOLON, TOKEN_SEMICOLON};
        actualTest(new Scanner(";;  ;;"), expected);
    }

    @Test
    public void testSimple() {
        String program = "a=12;b=a+7*3;";
        TokenType[] expected = {TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_INT, TOKEN_SEMICOLON,
                TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_ID, TOKEN_PLUS, TOKEN_INT, TOKEN_MULTIPLY, TOKEN_INT, TOKEN_SEMICOLON};
        actualTest(new Scanner(program), expected);
    }

    @Test
    public void tesFullExpression() {
        String program = "a=12;b=(a-1)+(9*91+(a/4));";
        TokenType[] expected = {TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_INT, TOKEN_SEMICOLON,
                TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_PAREN_OPEN, TOKEN_ID, TOKEN_MINUS, TOKEN_INT, TOKEN_PAREN_CLOSE, TOKEN_PLUS,
                TOKEN_PAREN_OPEN, TOKEN_INT, TOKEN_MULTIPLY, TOKEN_INT, TOKEN_PLUS, TOKEN_PAREN_OPEN, TOKEN_ID, TOKEN_DIVIDE, TOKEN_INT, TOKEN_PAREN_CLOSE, TOKEN_PAREN_CLOSE,
                TOKEN_SEMICOLON};
        actualTest(new Scanner(program), expected);
    }

    @Test
    public void testIf() {
        String program = "a=1;b=0;if(a==1){b=1;}else{b=2;}";
        TokenType[] expected = {TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_INT, TOKEN_SEMICOLON, TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_INT, TOKEN_SEMICOLON,
                TOKEN_IF, TOKEN_PAREN_OPEN, TOKEN_ID, TOKEN_EQUALS, TOKEN_INT, TOKEN_PAREN_CLOSE, TOKEN_CURLY_OPEN, TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_INT, TOKEN_SEMICOLON, TOKEN_CURLY_CLOSE,
                TOKEN_ELSE, TOKEN_CURLY_OPEN, TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_INT, TOKEN_SEMICOLON, TOKEN_CURLY_CLOSE};
        actualTest(new Scanner(program), expected);
    }

    @Test
    public void testWhile() {
        String program = "a=1;while(a=/=10)a=a+1;";
        TokenType[] expected = {TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_INT, TOKEN_SEMICOLON, TOKEN_WHILE, TOKEN_PAREN_OPEN,
                TOKEN_ID, TOKEN_NOT_EQUALS, TOKEN_INT, TOKEN_PAREN_CLOSE, TOKEN_ID, TOKEN_ASSIGNMENT, TOKEN_ID, TOKEN_PLUS, TOKEN_INT, TOKEN_SEMICOLON};
        actualTest(new Scanner(program), expected);
    }

    @Test
    public void testPrint() {
        String program = "print(99);";
        TokenType[] expected = {TOKEN_PRINT, TOKEN_PAREN_OPEN, TOKEN_INT, TOKEN_PAREN_CLOSE, TOKEN_SEMICOLON};
        actualTest(new Scanner(program), expected);
    }

}
