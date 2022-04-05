package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;
import edu.ufl.cise.plc.CodeGenStringBuilder;

public class CodeGenVisitor implements ASTVisitor{
    public CodeGenVisitor(String packageName ){



    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        boolean bool = booleanLitExpr.getValue();

        sb.append(booleanLitExpr.getText());

        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));

    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        Type type = stringLitExpr.getType();

        //TODO check if append is right
        sb.stringQuotes();
        sb.append(stringLitExpr.getValue());
        sb.stringQuotes();


        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        Type type = intLitExpr.getType();


        int intValue = intLitExpr.getValue();

        //TODO check if right
        sb.append(String.valueOf(intValue));

        if (intLitExpr.getCoerceTo() != type && intLitExpr.getCoerceTo() != null)  {
            genTypeConversion(type, intLitExpr.getCoerceTo(), sb);
        }
        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));

    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        Type type = floatLitExpr.getType();
        float floatValue = floatLitExpr.getValue();



        sb.append(floatLitExpr.getText());
        sb.append("f");

        if (floatLitExpr.getCoerceTo() != Type.FLOAT && floatLitExpr.getCoerceTo() != null) {
            genTypeConversion(type, floatLitExpr.getCoerceTo(), sb);
        }
        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {



        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        Type type = unaryExpression.getType();
        Expr expr = unaryExpression.getExpr();


        Kind op = unaryExpression.getOp().getKind();
        Type exprType = expr.getCoerceTo() != null ? expr.getCoerceTo() : expr.getType();

        //TODO check if binary expr is in assignment 5
        if (type == Type.IMAGE || type == Type.COLOR || type == Type.COLORFLOAT)
            throw new UnsupportedOperationException("Not implemented");
        else {
            sb.lparen();
            sb.append(unaryExpression.getOp().getText());
            unaryExpression.getExpr().visit(this, sb);
            sb.rparen();

        }
        if (unaryExpression.getCoerceTo() != type) {
            genTypeConversion(type, unaryExpression.getCoerceTo(), sb);
        }
        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        Type type = binaryExpr.getType();
        Expr leftExpr = binaryExpr.getLeft();
        Expr rightExpr = binaryExpr.getRight();
        Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo() : leftExpr.getType();
        Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo() : rightExpr.getType();
        Kind op = binaryExpr.getOp().getKind();

        //TODO check if binary expr is in assignment 5
        // if (not handled in assignment 5)
        //      throw new UnsupportedOperationException("Not implemented");
        //else {
        sb.lparen();

        binaryExpr.getLeft().visit(this, sb);

        sb.append(binaryExpr.getOp().getText());
        binaryExpr.getRight().visit(this, sb);
        sb.rparen();
        //}
        if (binaryExpr.getCoerceTo() != type) {
            genTypeConversion(type, binaryExpr.getCoerceTo(), sb);
        }
        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        Type type = identExpr.getType();
        String text = identExpr.getText();

        //TODO might be wrong visit/also check if it needed
        //  identExpr.visit(this, sb);
        sb.append(text);


        if (identExpr.getCoerceTo() != type && identExpr.getCoerceTo() != null)  {
            genTypeConversion(type, identExpr.getCoerceTo(), sb);
        }


        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));

    }

    @Override
    //( <condition> ) ? <trueCase> : <falseCase>
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();

        String name = conditionalExpr.getCondition().getText();
        Expr trueExpr= conditionalExpr.getTrueCase();
        Expr falseExpr= conditionalExpr.getFalseCase();
        sb.lparen();
        sb.append(name);
        sb.rparen();
        sb.question();
        trueExpr.visit(this,sb);
        sb.colon();
        falseExpr.visit(this,sb);

        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();

        String name = assignmentStatement.getName();
        Expr expr = assignmentStatement.getExpr();


        sb.append(name);
        sb.equals();
        expr.visit(this,sb);
        sb.append(expr.getText());
        sb.semi();


        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        String name = readStatement.getName();
        Expr expr = readStatement.getSource();

        if(readStatement.getSource().getType() != Type.CONSOLE){
            throw new UnsupportedOperationException("Not implemented");
        }
        else {
            sb.append(name);
            sb.equals();
            expr.visit(this,sb);
            sb.semi();
        }

        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {



        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        Type type = nameDef.getType();
        String name = nameDef.getName();

        //TODO Need help on this


        sb.append(String.valueOf(type));
        sb.append(name);


        //      if (unaryExpression.getCoerceTo() != type) {
        //        genTypeConversion(type, unaryExpression.getCoerceTo(), sb);
        //  }
        return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Expr expr = returnStatement.getExpr();
        sb.append("return ");
        expr.visit(this, sb);
        sb.semi().newline();
        return sb;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        return null;
    }


    public void genTypeConversion(Type type, Type coerceTo, CodeGenStringBuilder sb) {


    }









}
