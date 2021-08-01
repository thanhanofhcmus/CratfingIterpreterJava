package com.craftinginterpreters.lox;

import java.util.*;

import static com.craftinginterpreters.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private boolean isInComment = false;

    private static final  Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("class",   CLASS);
        keywords.put("else",    ELSE);
        keywords.put("false",   FALSE);
        keywords.put("for",     FOR);
        keywords.put("fun",     FUN);
        keywords.put("if",      IF);
        keywords.put("nil",     NIL);
        keywords.put("or",      OR);
        keywords.put("print",   PRINT);
        keywords.put("return",  RETURN);
        keywords.put("super",   SUPER);
        keywords.put("this",    THIS);
        keywords.put("true",    TRUE);
        keywords.put("var",     VAR);
        keywords.put("while",   WHILE);
    }

    Scanner(String source) { this.source = source; }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // add semicolon for last statement while running prompt
        if (Lox.isRunPrompt()) {
            TokenType tt = tokens.get(tokens.size() - 1).type;
            if (tt != RIGHT_BRACE && tt != SEMICOLON) {
                tokens.add(new Token(SEMICOLON, ";", null, line));
            }
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        if (isInComment) {
            if ('\n' == peek()) {
                line++;
            } else if ('*' == c && '/' == peek()) {
                isInComment = false;
                advance();
            }
            return;
        }
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case '*' -> addToken(STAR);
            case ':' -> addToken(COLON);
            case ';' -> addToken(SEMICOLON);
            case '?' -> addToken(QUESTION);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '"' -> string();
            case '\n' -> line++;
            case ' ', '\t', '\r' -> {}
            case '/' -> {
                if (match('/')) {
                    while (!isAtEnd() && peek() != '\n') {
                        advance();
                    }
                } else if (match('*')) {
                    isInComment = true;
                } else {
                    addToken(SLASH);
                }
            }
            default -> {
                if      (isDigit(c)) { number(); }
                else if (isAlpha(c)) { identifier(); }
                else                 { error("Unexpected character: " + c);
                }
            }
        }
    }

    private boolean isAtEnd() { return current >= source.length(); }

    private char advance() { return source.charAt(current++); }

    private char peek() {
        if (isAtEnd()) { return '\0'; }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) { return '\0'; }
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (expected != peek()) { return false; }
        current++;
        return true;
    }

    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }

    private boolean isAlpha(char c) {
        return  (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                 c == '_';
    }

    private boolean isAlphaNumeric(char c) { return isDigit(c) || isAlpha(c); }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }

        double literal = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, literal);
    }

    private void string() {
        while (!isAtEnd() && peek() != '"') {
            if ('\n' == peek()) { line++; }
            advance();
        }

        if (isAtEnd()) { error("Unterminated string"); }

        advance();

        String literal = source.substring(start + 1, current - 1);
        addToken(STRING, literal);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (null == type) { type = IDENTIFIER; }
        addToken(type);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void error(String message) {
        ErrorReporter.error(line, "Scanner", message);
    }
}
