package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.Expr;

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
    void expr(){

        if()


    }
    void ConditionalExpr(){

    }
    void LogicalOrExpr(){
        LogicalAndExpr ();
        while (isKind(IToken.Kind.OR)){
            consume(IToken.Kind.OR,"");    //what to put in message?

            LogicalAndExpr ();
        }
        return;

    }

    void LogicalAndExpr(){
        ComparisonExpr ();
        while (isKind(IToken.Kind.AND)){
            consume(IToken.Kind.AND,"");    //what to put in message?

            ComparisonExpr ();
        }
        return;
    }
    void ComparisonExpr (){

    }
    void AdditiveExpr (){
        MultiplicativeExpr();
        while (isKind(IToken.Kind.PLUS, IToken.Kind.MINUS)){
            if(isKind(IToken.Kind.PLUS)) {
                consume(IToken.Kind.PLUS,"");    //what to put in message?
            }
            else{//else if or else
                consume(IToken.Kind.MINUS,"");
            }
            MultiplicativeExpr();
        }
        return;
    }
    void MultiplicativeExpr(){
        UnaryExpr();
        while (isKind(IToken.Kind.TIMES, IToken.Kind.DIV)){
            if(isKind(IToken.Kind.TIMES)) {
                consume(IToken.Kind.TIMES,"");    //what to put in message?
            }
            else{//else if or else
                consume(IToken.Kind.DIV,"");
            }
            UnaryExpr();
        }
        return;
    }
    void UnaryExpr(){

    }
    void UnaryExprPostfix(){

    }
    void PrimaryExpr(){

    }
    void PixelSelector(){
        if(isKind(IToken.Kind.BOOLEAN_LIT)){
            consume(IToken.Kind.BOOLEAN_LIT,"");

        }
        else if(isKind(IToken.Kind.STRING_LIT)){
            consume(IToken.Kind.STRING_LIT,"")
        }
        else if(isKind(IToken.Kind.INT_LIT)){
            consume(IToken.Kind.INT_LIT, "int lit error")
        }
        else if(isKind(IToken.Kind.FLOAT_LIT)){
            consume(IToken.Kind.FLOAT_LIT, "")
        }
        else if(isKind(IToken.Kind.IDENT)){
            consume(IToken.Kind.IDENT, "")
        }
        else if(isKind(IToken.Kind.LPAREN)){
            consume(IToken.Kind.LPAREN,"");
            expr();
            match(IToken.Kind.RPAREN);
        }
        else{
            throw error();
        }
        return;
    }


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
    protected boolean isKind(IToken.Kind kind) {
        return t.getKind() == kind;
    }
    protected boolean isKind(IToken.Kind... kinds) {
        for (IToken.Kind k : kinds) {
            if (k == t.getKind())
                return true;
        }
        return false;
    }









}