package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;
import java.util.*;

public class Parser implements IParser {
    private final List<Token> tokens;
    int curr = 0;
    //change if needed
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }


    ASTNode parse() throws PLCException{



    }
    void EXP(IToken token){

        if()


    }
    void ConditionalExpr(){

    }
    void LogicalOrExpr(){

    }

    void LogicalAndExpr(){

    }
    void AdditiveExpr (){

    }
    void MultiplicativeExpr(){

    }
    void UnaryExpr(){

    }
    void UnaryExprPostfix(){

    }
    void PrimaryExpr(){

    }
    void PixelSelector(){

    }
    String First(){

    }
    String Forward(){

    }
    String Predict(){

    }

    //basically copy pasted from the book, which i think is allowed, but check later if this type of stuff needs
//changed
    private boolean match(IToken.Kind... kind) {
        for (IToken.Kind k : kind) {
            if (check(k)) {
                advance();
                return true;
            }
        }

        return false;
    }
    private boolean check(IToken.Kind kind) {
        if (isAtEnd()) return false;
        return peek().kind == kind;
    }
    private Token advance() {
        if (!isAtEnd()) curr++;
        return previous();
    }
    private boolean isAtEnd() {
        return peek().kind == IToken.Kind.EOF;
    }
    //does this overlap with peek in the lexer class
    private Token peek() {
        return tokens.get(curr);
    }
    private Token previous() {
        return tokens.get(curr - 1);
    }
    private Token consume(IToken.Kind kind, String message) {
        if (check(kind)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }
    static void error(Token token, String message) {
        if (token.kind == IToken.Kind.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }










}