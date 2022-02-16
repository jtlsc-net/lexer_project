package edu.ufl.cise.plc;

import com.sun.source.tree.Tree;
import edu.ufl.cise.plc.ast.*;
import jdk.jshell.Snippet;

import java.util.*;
//TODO make parse function, make the error functions,
public class Parser implements IParser {
    private final ArrayList<Token> tokens;
    int curr = 0;
    IToken t;
    //change if needed
    Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }
    ASTNode newNode;

    ASTNode parse() throws PLCException{
        Expr ast = expr();


    }
    Expr expr(){

        if()


    }

    //TODO expr not done
    public Expr ConditionalExpr(){
        IToken firstToken = t;
        Expr e  = null;
        match(IToken.Kind.KW_IF);
        match(IToken.Kind.LPAREN);
        expr()



        return e;
    }
    //TODO
    Expr LogicalOrExpr(){
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;

        left =  LogicalAndExpr();
        while (isKind(IToken.Kind.OR)){
            IToken op = t;
            consume(IToken.Kind.OR,"");    //what to put in message?
            right = LogicalAndExpr();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;

    }


    Expr LogicalAndExpr(){
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left = ComparisonExpr();
        while (isKind(IToken.Kind.AND)){

            IToken op = t;

            consume(IToken.Kind.AND,"");    //what to put in message?
            right = ComparisonExpr ();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;
    }
    Expr ComparisonExpr (){

    }
    Expr AdditiveExpr (){

        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        left= MultiplicativeExpr();
        while (isKind(IToken.Kind.PLUS, IToken.Kind.MINUS)){
            IToken op = t;
            if(isKind(IToken.Kind.PLUS)) {
                consume(IToken.Kind.PLUS,"");    //what to put in message?
            }
            else{//else if or else
                consume(IToken.Kind.MINUS,"");
            }
            right= MultiplicativeExpr();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;
    }

    //TODO
    Expr MultiplicativeExpr(){
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

        IToken firstToken = t;
        Expr e = null;

        if(isKind(IToken.Kind.BOOLEAN_LIT)){
            e = new BooleanLitExpr(firstToken);
            consume(IToken.Kind.BOOLEAN_LIT,"");

        }
        else if(isKind(IToken.Kind.STRING_LIT)){
            e = new StringLitExpr(firstToken);
            consume(IToken.Kind.STRING_LIT,"")
        }
        else if(isKind(IToken.Kind.INT_LIT)){
            e = new IntLitExpr(firstToken);
            consume(IToken.Kind.INT_LIT, "int lit error")
        }
        else if(isKind(IToken.Kind.FLOAT_LIT)){
            e = new FloatLitExpr(firstToken);
            consume(IToken.Kind.FLOAT_LIT, "")
        }
        else if(isKind(IToken.Kind.IDENT)){
            e = new IdentExpr(firstToken);
            consume(IToken.Kind.IDENT, "")
        }
        else if(isKind(IToken.Kind.LPAREN)){
            consume(IToken.Kind.LPAREN,"");
            e= expr();
            match(IToken.Kind.RPAREN);
        }
        else{
            throw error();
        }
        return;
    }


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