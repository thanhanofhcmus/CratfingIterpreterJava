package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static final Interpreter interpreter = new Interpreter();
    private static boolean runPrompt = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    public static void runFile(String path) throws IOException {
        runPrompt = false;
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (ErrorReporter.hadError())        { System.exit(65); }
        if (ErrorReporter.hadRuntimeError()) { System.exit(70); }
    }

    public static void runPrompt() throws IOException {
        runPrompt = true;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            ErrorReporter.printPrompt();
            String line = reader.readLine();
            if (null == line) { break; }
            if (line.isEmpty()) { continue; }
            run(line);
        }
    }

    public static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        List<Stmt> statements = new Parser(tokens).parse();
        if (ErrorReporter.hadError()) { return; }
        interpreter.interpret(statements);
    }

    public static boolean isRunPrompt() { return runPrompt; }
}
