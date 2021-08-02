package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/*
Parse <- declaration
declaration     = varDeclaration | statement
varDeclaration  = IDENTIFIER ("=" expression)? ";"
statement       = printStatement | exprStatement | block
                | ifStatement | whileStatement | forStatement
                | breakStatement | continueStatement
printStatement  = "print" expression ";"
exprStatement   = expression ";"
block           = "{" declaration* "}"
ifStatement     = "if" (expression | group) block ("else" block)?
whileStatement  = "while" (expression | group) block
forStatement    = "for" "(" (varDeclaration | exprStatement)? ";" expression? ";" expression? ")" block
expression      = assignment
assignment      = ternary "=" VARIABLE
ternary         = commaList "?" commaList ":" commaList
commaList       = logical ("or" | "and" logical)*
logical         = equality "," equality
equality        = comparison ("==" | "!=") comparison
comparison      = term ("<" | ">" | "<=" ">=") term
term            = factor (("+" | "-") factor)*
factor          = unary (("*" | "/") unary)*
unary           = ("!" | "-") unary | primary
primary         = "true" | "false" | "nil" | NUMBER | STRING | IDENTIFIER | group
group           = "(" expression ")"
VARIABLE        = named assignment
*/

public class Parser {
    private static class ParseException extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        try {
            List<Stmt> statements = new ArrayList<>();
            while (!isAtEnd()) {
                statements.add(declaration());
            }

            // make last expression always print in prompt mode
            if (Lox.isRunPrompt()) {
                var lastStmt = statements.get(statements.size() - 1);
                if (lastStmt instanceof Stmt.Expression) {
                    statements.remove(statements.size() - 1);
                    statements.add(new Stmt.Print(((Stmt.Expression) lastStmt).expr));
                }
            }

            return statements;
        } catch (ParseException error) {
            ErrorReporter.error(peek().line,"Parser", error.getMessage());
            return null;
        }
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) { return varDeclaration(); }
            else            { return statement(); }
        } catch (ParseException error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect a variable name");

        Expr initializer = null;
        if (match(EQUAL)) { initializer = expression(); }

        consume(SEMICOLON, "Expect ';' after variable declaration");
        return new Stmt.Variable(name, initializer);
    }

    private Stmt statement() {
        if      (match(PRINT))      { return printStatement();    }
        else if (match(LEFT_BRACE)) { return blockStatement();    }
        else if (match(IF))         { return ifStatement();       }
        else if (match(WHILE))      { return whileStatement();    }
        else if (match(FOR))        { return forStatement();      }
        else if (match(BREAK))      { return breakStatement();    }
        else if (match(CONTINUE))   { return continueStatement(); }
        else                        { return exprStatement();     }
    }

    private Stmt printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "'print' statement expects ';' after expression");
        return new Stmt.Print(expr);
    }

    private Stmt exprStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expects ';' after expression to make an expression statement");
        return new Stmt.Expression(expr);
    }

    private Stmt blockStatement() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(declaration());
        }

        consume(RIGHT_BRACE, "Need '}' to close a block");
        return new Stmt.Block(stmts);
    }

    private Stmt ifStatement() {
        Expr condition = match(LEFT_PAREN) ? bracedExpression() : expression();
        consume(LEFT_BRACE, "If statement must have a block {}");
        Stmt ifBlock = blockStatement();
        Stmt elseBlock = null;
        if (match(ELSE)) {
            consume(LEFT_BRACE, "Else statement must have a block {}");
            elseBlock = blockStatement();
        }
        return new Stmt.If(condition, ifBlock, elseBlock);
    }

    private Stmt whileStatement() {
        Expr condition = match(LEFT_PAREN) ? bracedExpression() : expression();
        consume(LEFT_BRACE, "While statement must have a block {}");
        Stmt block = blockStatement();
        return new Stmt.While(condition, block);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "For loop must have ()");

        Stmt init = null;
        if (check(SEMICOLON)) { advance(); }
        else                  { init = declaration(); }

        Expr condition = check(SEMICOLON) ? new Expr.Literal(true) : expression();
        consume(SEMICOLON, "Expected ; after loop condition");

        Expr increase = check(RIGHT_PAREN) ? null : expression();

        consume(RIGHT_PAREN, "Expect ')' at the end of for loop declaration");
        consume(LEFT_BRACE, "For statement must have a block {}");
        Stmt block = blockStatement();

        return new Stmt.For(init, condition, increase, block);
    }

    private Stmt breakStatement() {
        consume(SEMICOLON, "Expect ; after break");
        return new Stmt.Break();
    }

    private Stmt continueStatement() {
        consume(SEMICOLON, "Expect ; after continue");
        return new Stmt.Continue();
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = ternary();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            // Purposely not throw to not activate panic mode
            //noinspection ThrowableNotThrown
            error(equals, "Invalid assign target");
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = commaList();

        if (match(QUESTION)) {
            Expr first = commaList();
            consume(COLON, "Must have colon(':') in ternary expression");
            Expr second = commaList();
            expr = new Expr.Ternary(expr, first, second);
        }

        return expr;
    }

    private Expr commaList() {
        Expr expr = logical();

        while (match(COMMA)) {
            Token operator = previous();
            Expr right = logical();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr logical() {
        Expr expr = equality();

        while (match(AND, OR)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(operator, expr);
        } else {
            return primary();
        }
    }

    private Expr primary() {
        Token token = advance();

        switch (token.type) {
            case FALSE: return new Expr.Literal(false);
            case TRUE:  return new Expr.Literal(true);
            case NIL:   return new Expr.Literal(null);
            case IDENTIFIER: return new Expr.Variable(previous());
            case NUMBER: case STRING: return new Expr.Literal(token.literal);
            case LEFT_PAREN: return bracedExpression();
        }

        // quick and dirty fix for going back
        current--;
        throw error(token, "Expected an expression");
    }

    private Expr bracedExpression() {
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expect ')' after expression");
        return expr;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

   private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
   }

   private ParseException error(Token token, String message) {
       ErrorReporter.error(token, "Parser", message);
       return new ParseException();
   }

   private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case CLASS: case FUN: case VAR: case FOR:
                case IF: case WHILE: case PRINT: case RETURN:
                    return;
            }
            advance();
        }
   }

   private Token consume(TokenType expected, String errorMessage) {
        if (check(expected)) {
            return advance();
        }
        throw error(peek(), errorMessage);
   }
}
