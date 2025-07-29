package gj.compiler.minimus;

/**
 * ~ Scanner or Lexical Analyzer ~++
 * <p/>
 * The main purpose of a Lexical Analyzer (or lexer, scanner) in a compiler is
 * to read the source code and break it down into a stream of tokens, which are
 * meaningful units of text like keywords, identifiers, operators, and
 * constants. This process, called lexical analysis, is the first phase of
 * compilation, acting as a preprocessor for the parser.
 * <p/>
 * Grammar for Minimus in EBNF:
 * <p/>
 * <pre>
 * program      ::= statement
 * statement    ::= "if" paren_expr statement ["else" statement] |
 *                  "while" paren_expr statement |
 *                  "{" { statement } "}" |
 *                  expr ";" |
 *                  ";" |
 *                  print "(" expr ")" ";"
 * paren_expr   ::= "(" expr ")"
 * expr         ::= id "=" expr | test
 * test         ::= sum [ rel sum ]
 * rel          ::= "<" | ">" | "<=" | ">=" | "==" | "=/="
 * sum          ::= factor { "+" | "-" factor }
 * factor       ::= term { "*" | "/" term }
 * term         ::= id | int | paren_expr
 * id           ::= "a" | "b" | "c" | "d" | ... | "z"
 * int          ::= an unsigned integer
 * </pre>
 */
public class Scanner {

    public static final char END_OF_TEXT = 0x03;

    /* This defines all the possible tokens for the language.  Traditionally
       this could also be defined as a list of constants, but enums are much
       more elegant.  A token is the smallest building block of your language,
       a group of characters having a collective meaning. */
    public enum TokenType {
        TOKEN_IF, TOKEN_ELSE, TOKEN_WHILE, TOKEN_PRINT,
        TOKEN_CURLY_OPEN, TOKEN_CURLY_CLOSE, TOKEN_PAREN_OPEN, TOKEN_PAREN_CLOSE, TOKEN_SEMICOLON, TOKEN_ASSIGNMENT,
        TOKEN_LESS_THAN, TOKEN_LESS_EQUAL_THAN, TOKEN_GREATER_EQUAL_THAN, TOKEN_GREATER_THAN,
        TOKEN_EQUALS, TOKEN_NOT_EQUALS,
        TOKEN_PLUS, TOKEN_MINUS, TOKEN_MULTIPLY, TOKEN_DIVIDE,
        TOKEN_ID, TOKEN_INT, TOKEN_EOI
    }

    /* The Token dataclass defines the actual instance of the Token (lexeme)
       that was found in the program.  It represents the type as well as the
       value, if applicable. */
    public static class Token {
        public TokenType type;
        public String lexeme;

        public Token(TokenType type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }

        public Token(TokenType type) {
            this(type, null);
        }

        @Override
        public String toString() {
            if (lexeme != null) {
                return String.format("Symbol {type=%s, lexeme=%s}", type, lexeme);
            } else {
                return String.format("Symbol {type=%s}", type);
            }
        }
    }

    /* The scanner receives the entire program as a String as shown below.
       Today, it is trivial to process the entire program and put all the
       tokens in a list that is fully traversable. However, traditionally,
       lexical analysers were build to process tokens one at a time as the
       parser only requires the current (and possibly the next) token at any
       given time. Ideally, this String could be replaced by a InputStream
       reading characters from the source file as needed.  A string was chosen
       for simplicity's sake. */
    private final String program;

    /* The "location" stores the position in the String that needs to be read
       next, while "current" stores the next token to be returned.  */
    private int location = 0;
    private Token current = null;

    /* Keeping track of the current line number. */
    private int line = 1;

    public Scanner(String program) {
        this.program = program;
    }

    /* Returns the next character in the program (or 0x03 if end of input is
       found. */
    private char nextChar() {
        char ch = location < program.length() ? program.charAt(location++) : END_OF_TEXT;
        if (ch == '\n') {
            line = line + 1;
        }
        return ch;
    }

    /* This peeks ahead without moving the location pointer. */
    private char peekChar() {
        return location < program.length() ? program.charAt(location) : END_OF_TEXT;
    }

    /* Handles "<" and "<=". */
    private Token checkSmallerEqual() {
        if (peekChar() == '=') {
            nextChar();
            return new Token(TokenType.TOKEN_LESS_EQUAL_THAN);
        } else {
            return new Token(TokenType.TOKEN_LESS_THAN);
        }
    }

    /* Handles ">" and ">=". */
    private Token checkGreaterEqual() {
        if (peekChar() == '=') {
            nextChar();
            return new Token(TokenType.TOKEN_GREATER_EQUAL_THAN);
        } else {
            return new Token(TokenType.TOKEN_GREATER_THAN);
        }
    }

    /* Handles "==",  "=/=" and "=". */
    private Token checkEqualsNotEquals() {
        if (peekChar() == '=') {
            nextChar();
            return new Token(TokenType.TOKEN_EQUALS);
        } else if (peekChar() == '/') {
            nextChar();
            char tmp = nextChar();
            if (tmp == 0x03) {
                throw new RuntimeException("Scanner error: Unexpected end of input!");
            } else if (tmp != '=') {
                throw new RuntimeException(String.format("Scanner error: Unknown character: %c", tmp));
            }
            return new Token(TokenType.TOKEN_NOT_EQUALS);
        } else {
            return new Token(TokenType.TOKEN_ASSIGNMENT);
        }
    }

    /* Handles INT, IF, ELSE, WHILE and ID. */
    private Token checkOther(char ch) {

        // Is this a possible integer?
        if (Character.isDigit(ch)) {
            StringBuilder b = new StringBuilder(String.valueOf(ch));
            while (Character.isDigit(peekChar())) {
                b.append(nextChar());
            }
            return new Token(TokenType.TOKEN_INT, b.toString());
        }

        // Either reserved word, id or unknown.
        StringBuilder b = new StringBuilder(String.valueOf(ch));
        while (Character.isAlphabetic(peekChar())) {
            b.append(nextChar());
        }

        // Reserved word?
        String s = b.toString();
        switch (s) {
            case "if":
                return new Token(TokenType.TOKEN_IF);
            case "else":
                return new Token(TokenType.TOKEN_ELSE);
            case "while":
                return new Token(TokenType.TOKEN_WHILE);
            case "print":
                return new Token(TokenType.TOKEN_PRINT);
        }

        // Must be an id!
        if (s.length() == 1 && s.charAt(0) >= 'a' && s.charAt(0) <= 'z') {
            return new Token(TokenType.TOKEN_ID, s);
        }

        // We don't know what it is!
        throw new RuntimeException(String.format("Scanner error: unknown token: %s", s));
    }

    /* Reads the next token in the source stream. */
    private Token getNextToken() {

        char ch = nextChar();
        while (Character.isWhitespace(ch)) {
            ch = nextChar();
        }

        switch (ch) {
            case END_OF_TEXT:
                return new Token(TokenType.TOKEN_EOI);
            case '{':
                return new Token(TokenType.TOKEN_CURLY_OPEN);
            case '}':
                return new Token(TokenType.TOKEN_CURLY_CLOSE);
            case '(':
                return new Token(TokenType.TOKEN_PAREN_OPEN);
            case ')':
                return new Token(TokenType.TOKEN_PAREN_CLOSE);
            case ';':
                return new Token(TokenType.TOKEN_SEMICOLON);
            case '<':
                return checkSmallerEqual();
            case '>':
                return checkGreaterEqual();
            case '=':
                return checkEqualsNotEquals();
            case '+':
                return new Token(TokenType.TOKEN_PLUS);
            case '-':
                return new Token(TokenType.TOKEN_MINUS);
            case '*':
                return new Token(TokenType.TOKEN_MULTIPLY);
            case '/':
                return new Token(TokenType.TOKEN_DIVIDE);
            default:
                return checkOther(ch);
        }
    }

    /**
     * Returns the next token without advancing to the next token.
     * @return Token object
     */
    public Token peek() {
        if (current == null) {
            current = getNextToken();
        }
        return current;
    }

    /**
     * Returns the next token and advance to the next token.
     * @return Token object
     */
    public Token next() {
        Token tokenToReturn = peek();
        current = getNextToken();
        return tokenToReturn;
    }

    /**
     * Return the current line number.
     * @return current line number as an int
     */
    public int getLineNumber() {
        return line;
    }

}
