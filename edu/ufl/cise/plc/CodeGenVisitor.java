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
import edu.ufl.cise.plc.ast.Types;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;
import edu.ufl.cise.plc.CodeGenStringBuilder;
import edu.ufl.cise.plc.runtime.ConsoleIO;

import edu.ufl.cise.plc.runtime.ColorTuple;
import edu.ufl.cise.plc.runtime.ImageOps;
import edu.ufl.cise.plc.runtime.javaCompilerClassLoader.PLCLangExec;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.naming.Name;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CodeGenVisitor implements ASTVisitor {
    private String packageName;
    private ArrayList<String> imports;

    public CodeGenVisitor(String packageName) {
        this.packageName = packageName;
        this.imports = new ArrayList<String>();
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        boolean bool = booleanLitExpr.getValue();

        sb.append(booleanLitExpr.getText());

        return sb;

    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Type type = stringLitExpr.getType();

        // TODO check if append is right
        sb.stringQuotes().newline();
        sb.append(stringLitExpr.getValue());
        sb.stringQuotes();

        return sb;
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Type type = intLitExpr.getType();

        int intValue = intLitExpr.getValue();

        // TODO check if right

        if(intLitExpr.getCoerceTo() == Type.IMAGE) {
            sb.append(String.valueOf(intValue));
        }
        else if(intLitExpr.getCoerceTo() == Type.COLOR) {
            sb.append(String.valueOf(intValue));
        }
        else if (intLitExpr.getCoerceTo() != type && intLitExpr.getCoerceTo() != null) {
            //System.out.println(intLitExpr.getCoerceTo().toString());
            genTypeConversion(type, intLitExpr.getCoerceTo(), sb);
            sb.append(String.valueOf(intValue));
            sb.append(")");
        }
        else {
            sb.append(String.valueOf(intValue));
        }
        // return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));

        return sb;

    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Type type = floatLitExpr.getType();
        float floatValue = floatLitExpr.getValue();

        if (floatLitExpr.getCoerceTo() != Type.FLOAT && floatLitExpr.getCoerceTo() != null) {
            genTypeConversion(type, floatLitExpr.getCoerceTo(), sb);
            sb.append(floatLitExpr.getText());
            sb.append("f");
            sb.append(")");
        }
        else {
            sb.append(floatLitExpr.getText());
            sb.append("f");
        }



        return sb;
        // return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if(imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
            imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
        }
        if(imports.indexOf("java.awt.Color") == -1) {
            imports.add("java.awt.Color");
        }

        sb.append("ColorTuple.unpack(Color." + colorConstExpr.getFirstToken().getText() + ".getRGB())");

        return sb;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        if (imports.indexOf("edu.ufl.cise.plc.runtime.ConsoleIO") == -1) {
            imports.add("edu.ufl.cise.plc.runtime.ConsoleIO");
        }
        if (imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
            imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
        }
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        String box = "";
        // sb.append("(Integer) ConsoleIO.readValueFromConsole(�ｿｽgINT�ｿｽh, �ｿｽgEnter
        // integer:�ｿｽh);");
        box = "(" + primitiveToWrapper(Types.toString(consoleExpr.getCoerceTo())) + ")";
        String type = consoleExpr.getCoerceTo().toString();
        String prompt = "Enter " + Types.toString(consoleExpr.getCoerceTo()) + ":";
        //TODO check this//still not working//maybe this isnt needed
        if(consoleExpr.getCoerceTo() == Type.COLOR){
            prompt = "RGB values:";
        }

        String consoleStatement = box + " ConsoleIO.readValueFromConsole(\"" + type + "\", \"" + prompt + "\")";
        sb.append(consoleStatement);


        //sb.append("ConsoleIO.readValueFromConsole(ConsoleIO.readValueFromConsole(“COLOR”, “Enter RGB values:”);");


        return sb;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
            imports.add("edu.ufl.cise.plc.runtime.ImageOps");
        }
        if (imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
            imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
        }
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append("new ColorTuple");
        sb.lparen();
        colorExpr.getRed().visit(this, sb);
        sb.comma().space();
        colorExpr.getGreen().visit(this, sb);
        sb.comma().space();
        colorExpr.getBlue().visit(this, sb);
        sb.rparen();

        return sb;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Type type = unaryExpression.getType();
        Expr expr = unaryExpression.getExpr();

     //   if ((type == Type.IMAGE || type == Type.COLOR || type == Type.COLORFLOAT) ||
       //         (expr.getType() == Type.INT && unaryExpression.getOp().getKind() == Kind.COLOR_OP)){
            //TODO idk what this if does
            if(unaryExpression.getOp().getKind() == Kind.COLOR_OP){
                if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                    imports.add("edu.ufl.cise.plc.runtime.ImageOps");
                }
                if(expr.getType() == Type.INT){
                    if(imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
                        imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
                    }
                    sb.append("ColorTuple." + unaryExpression.getOp().getText() + "(");
                    unaryExpression.getExpr().visit(this,sb);
                    sb.rparen();
                }
                else if(expr.getType() == Type.COLOR ){
                    if(imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
                        imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
                    }
                    //  BufferedImage image = (BufferedImage)(new PLCLangExec("mypackage",true)).exec(input, null);
                    sb.append("ColorTuple." + unaryExpression.getOp().getText() + "(");
                    unaryExpression.getExpr().visit(this,sb);
                    sb.rparen();

                }else if(expr.getType() == Type.IMAGE ){


                    //TODO this is probably wrong, needs work
                    sb.append("ImageOps.extract");
                    String opName = unaryExpression.getOp().getText();
                  //  System.out.println(opName);
                    switch(opName) {
                        case "getRed" ->{
                            sb.append("Red");
                            break;
                        }
                        case "getBlue"->{
                            sb.append("Blue");
                            break;
                        }
                        case "getGreen"->{
                            sb.append("Green");
                            break;

                        }
                        default -> {
                            throw new IllegalArgumentException("Wrong Op type");

                        }
                    }
                    sb.append("(");
                    expr.visit(this,sb);
                    sb.rparen().newline();


                }


            }
            //TODO idk if this is right

      //  }

        	else if (unaryExpression.getOp().getKind() == Kind.IMAGE_OP) {
    			expr.visit(this, sb);
    			sb.append("." + unaryExpression.getText()).lparen().rparen();
    		}
        else {
               // System.out.println("test" + unaryExpression.getOp().getText());
            sb.lparen();
            sb.append(unaryExpression.getOp().getText());
            sb.space();
            unaryExpression.getExpr().visit(this, sb);
            sb.rparen();

        }
//		if (unaryExpression.getCoerceTo() != type) {
//			genTypeConversion(type, unaryExpression.getCoerceTo(), sb);
//		}
        return sb;
    }

    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        // CodeGenStringBuilder sb = new CodeGenStringBuilder();
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Type type = binaryExpr.getType();
        Expr left = binaryExpr.getLeft();
        Expr right = binaryExpr.getRight();
        // Expr leftExpr = binaryExpr.getLeft();
        // Expr rightExpr = binaryExpr.getRight();
//        Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo() : leftExpr.getType();
        // Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo() :
        // rightExpr.getType();
        // Kind op = binaryExpr.getOp().getKind();

        // TODO check if binary expr is in assignment 5
        // if (not handled in assignment 5)
        // throw new UnsupportedOperationException("Not implemented");
        // else {
        sb.lparen();
        if ((left.getType() == Type.COLOR && right.getType() == Type.COLOR) || (left.getCoerceTo() == Type.COLOR && right.getCoerceTo() == Type.COLOR)) {
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ImageOps");
            }
            if(imports.indexOf("static edu.ufl.cise.plc.runtime.ImageOps.OP.*") == -1) {
                imports.add("static edu.ufl.cise.plc.runtime.ImageOps.OP.*");
            }
            if(imports.indexOf("static edu.ufl.cise.plc.runtime.ImageOps.BoolOP.*") == -1) {
                imports.add("static edu.ufl.cise.plc.runtime.ImageOps.BoolOP.*");
            }
            sb.append("ImageOps.binaryTupleOp(" + binaryExpr.getOp().getKind().toString() + ", ");
            left.visit(this, sb);
            sb.comma().space();
            right.visit(this, sb);
            sb.rparen();
        }
        else if((left.getType() == Type.IMAGE || right.getType() == Type.IMAGE) && (left.getType() == Type.INT || right.getType() == Type.INT)) {
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ImageOps");
            }
            if(imports.indexOf("static edu.ufl.cise.plc.runtime.ImageOps.OP.*") == -1) {
                imports.add("static edu.ufl.cise.plc.runtime.ImageOps.OP.*");
            }
            if(imports.indexOf("static edu.ufl.cise.plc.runtime.ImageOps.BoolOP.*") == -1) {
                imports.add("static edu.ufl.cise.plc.runtime.ImageOps.BoolOP.*");
            }
            sb.append("ImageOps.binaryImageScalarOp(" + binaryExpr.getOp().getKind().toString() + ", ");
            left.visit(this, sb);
            sb.comma().space();
            right.visit(this, sb);
            sb.rparen();
        }
        else if(binaryExpr.getLeft().getType() == Type.IMAGE && right.getType() == Type.COLOR) {
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ImageOps");
            }
            if(imports.indexOf("static edu.ufl.cise.plc.runtime.ImageOps.OP.*") == -1) {
                imports.add("static edu.ufl.cise.plc.runtime.ImageOps.OP.*");
            }
            if(imports.indexOf("static edu.ufl.cise.plc.runtime.ImageOps.BoolOP.*") == -1) {
                imports.add("static edu.ufl.cise.plc.runtime.ImageOps.BoolOP.*");
            }
            sb.append("ImageOps.binaryImageScalarOp(ImageOps.OP." + binaryExpr.getOp().getKind().toString() + ", ");
            left.visit(this, sb);
            sb.comma().space();
            right.visit(this, sb);
            sb.rparen().comma().space();
            sb.append("ColorTuple.makePackedColor(ColorTuple.getRed(");
            right.visit(this,sb);
            sb.rparen().comma().space();
            sb.append("ColorTuple.makePackedColor(ColorTuple.getGreen(");
            right.visit(this,sb);
            sb.rparen().comma().space();
            sb.append("ColorTuple.makePackedColor(ColorTuple.getBlue(");
            right.visit(this,sb);
            sb.rparen().rparen().semi();


        }
        else if(binaryExpr.getLeft().getType() == Type.STRING && binaryExpr.getOp().getKind() == Kind.EQUALS) {
            binaryExpr.getLeft().visit(this, sb);
            sb.append(".equals(");
            binaryExpr.getRight().visit(this, sb);
            sb.rparen();
        }
        else if(binaryExpr.getLeft().getType() == Type.STRING && binaryExpr.getOp().getKind() == Kind.NOT_EQUALS) {
            sb.append("!");
            binaryExpr.getLeft().visit(this, sb);
            sb.append(".equals(");
            binaryExpr.getRight().visit(this, sb);
            sb.rparen();
        }
        else {
            sb.lparen();
            binaryExpr.getLeft().visit(this, sb);
            sb.rparen();

            sb.append(binaryExpr.getOp().getText());
            sb.lparen();
            binaryExpr.getRight().visit(this, sb);
            sb.rparen();
        }
        sb.rparen();
        // }

        return sb;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Type type = identExpr.getType();
        String text = identExpr.getText();

        // TODO might be wrong visit/also check if it needed
        // identExpr.visit(this, sb);

        if(identExpr.getCoerceTo() == Type.INT && identExpr.getType() == Type.COLOR) {
            sb.append(text);
        }
        else if(identExpr.getCoerceTo() == Type.COLOR && identExpr.getType() == Type.INT) {
            sb.append(text);
        }
        else if (identExpr.getCoerceTo() != type && identExpr.getCoerceTo() != null) {
            genTypeConversion(type, identExpr.getCoerceTo(), sb);
            sb.append(text);
            sb.append(")");
        }
        else {
            sb.append(text);
        }

        // return ((CodeGenStringBuilder) arg).append(String.valueOf(sb));
        return sb;

    }

    @Override
    // ( <condition> ) ? <trueCase> : <falseCase>
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;

        Expr trueExpr = conditionalExpr.getTrueCase();
        Expr falseExpr = conditionalExpr.getFalseCase();
        sb.lparen();
        conditionalExpr.getCondition().visit(this, sb);
        sb.rparen();
        sb.question();
        trueExpr.visit(this, sb);
        sb.colon();
        falseExpr.visit(this, sb);

        return sb;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;

        dimension.getWidth().visit(this, sb);
        sb.comma().space();
        dimension.getHeight().visit(this, sb);

        return sb;
        //throw new UnsupportedOperationException("Dimension not yet implemented.");
    }

    record testside(CodeGenStringBuilder sb, String imageName){}
    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append(pixelSelector.getX().getText() + ", " + pixelSelector.getY().getText());




        //if()
        //   sb.append("ImageOps.getRGB(" +pixelSelector.getText());
        // sb.comma();
        // pixelSelector.getX().visit(this, sb);
        // sb.comma();
        //pixelSelector.getY().visit(this, sb);

        //


        return sb;
/*
        if(arg instanceof testside){
            CodeGenStringBuilder sb = ((testside) arg).sb;
            pixelSelector.getY().visit(this, sb);
            sb.comma();
            pixelSelector.getX().visit(this, sb);
            //lhs
            //sb.append(pixelSelector.getX().getText() + ", " + pixelSelector.getY().getText());
        }
        //rhs
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        pixelSelector.getY().visit(this, sb);
        sb.comma();
        pixelSelector.getX().visit(this, sb);


        return sb;
        */

    }





/*
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        // if target type image
        //   if get pixel selector != null
        //handle x and y
        //else
        //if expr.getCoerceTo()==type.color
        //imageops.setcolor
        //else rhs is image
        //if dim != null
        //resize rhs   imageops.resize(lhs)
        //else
        //clone

        //else
        //append(assignmentstatement.getname "=" visitexpr)
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        CodeGenStringBuilder fakeOne = new CodeGenStringBuilder();
        String name = assignmentStatement.getName();
        Expr expr = assignmentStatement.getExpr();
        //  System.out.println(assignmentStatement.getTargetDec().getType().toString());


        if (assignmentStatement.getTargetDec().getType() == Type.IMAGE && assignmentStatement.getExpr().getType() == Type.IMAGE) {
            if(assignmentStatement.getSelector() != null){
                    if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                        imports.add("edu.ufl.cise.plc.runtime.ImageOps");
                    }
                    assignmentStatement.getSelector().visit(this, fakeOne);
                    String[] splitArr = fakeOne.getString().split(",");
                    String var1 = splitArr[0].trim();
                    String var2 = splitArr[1].trim();
                    sb.append("for( int " + var1 + " = 0; " + var1 + " < " + name + ".getWidth(); " + var1 + "++)").newline().tab().tab().tab();
                    sb.append("for( int " + var2 + " = 0; " + var2 + " < " + name + ".getHeight(); " + var2 + "++)").newline().tab().tab().tab().tab();
                    sb.append("ImageOps.setColor(" + name + ", " + var1 + ", " + var2 + ", ");
                    expr.visit(this, sb);
                    sb.rparen();

            }








            //  System.out.println("test");
            if (assignmentStatement.getTargetDec().getDim() != null) {
                sb.append("ImageOps.resize("+ assignmentStatement.getName());
                sb.comma();
                assignmentStatement.getSelector().visit(this,sb);
                sb.append(")");
            }

                // assignmentStatement.getName() = assignmentStatement.getTargetDec();
                sb.append("ImageOps.resize("+ assignmentStatement.getName());
                sb.comma();
                assignmentStatement.getSelector().visit(this,sb);
                sb.append(")");
                if(expr instanceof   IdentExpr){
                    sb.append("ImageOps.clone(");
                    expr.visit(this,sb);
                    sb.append(")");
                }


            }

                if (expr.getCoerceTo() == Type.INT) {
                    if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                        imports.add("edu.ufl.cise.plc.runtime.ImageOps");
                    }
                    assignmentStatement.getSelector().visit(this, fakeOne);
                    String[] splitArr = fakeOne.getString().split(",");
                    String var1 = splitArr[0].trim();
                    String var2 = splitArr[1].trim();
                    sb.append("for( int " + var1 + " = 0; " + var1 + " < " + name + ".getWidth(); " + var1 + "++)").newline().tab().tab().tab();
                    sb.append("for( int " + var2 + " = 0; " + var2 + " < " + name + ".getHeight(); " + var2 + "++)").newline().tab().tab().tab().tab();
                    sb.append("ImageOps.setColor(" + name + ", " + var1 + ", " + var2 + ", ColorTuple.unpack(ColorTuple.truncate(");
                    expr.visit(this, sb);
                    sb.rparen().rparen();
                }


        }

        else {

            sb.append(name);
            sb.equals();
            if (expr.getCoerceTo() != expr.getType() && expr.getCoerceTo() != null) {
                //			if(declaration.getExpr().)
                genTypeConversionNoParen(expr.getType(), expr.getCoerceTo(), sb);
                sb.lparen();
                expr.visit(this, sb);
                sb.rparen();
            }
            else {
                expr.visit(this, sb);
                //	        sb.append(expr.getText());
            }
        }
        sb.semi();
        sb.newline();



        return sb;
    }
*/
@Override
public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
    CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
    CodeGenStringBuilder fakeOne = new CodeGenStringBuilder();
    String name = assignmentStatement.getName();
    Expr expr = assignmentStatement.getExpr();
    //System.out.println(assignmentStatement.toString());

    if (assignmentStatement.getTargetDec().getType() == Type.IMAGE) {
        if( expr.getType() == Type.IMAGE){
            if (assignmentStatement.getTargetDec().getDim() != null) {
                sb.append("ImageOps.resize(");
                assignmentStatement.getName();
                sb.comma();
                assignmentStatement.getSelector().visit(this,sb);
                sb.append(")");
            } else {
                sb.append("ImageOps.clone(");
                expr.visit(this,sb);
                sb.append(")");
            }



        }


        else if ( expr.getCoerceTo() == Type.COLOR) {
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ImageOps");
            }
            assignmentStatement.getSelector().visit(this, fakeOne);
            String[] splitArr = fakeOne.getString().split(",");
       //     String var1 = splitArr[0].trim();
       //     String var2 = splitArr[1].trim();
       //     sb.append("for( int " + var1 + " = 0; " + var1 + " < " + name + ".getWidth(); " + var1 + "++)").newline().tab().tab().tab();
       //     sb.append("for( int " + var2 + " = 0; " + var2 + " < " + name + ".getHeight(); " + var2 + "++)").newline().tab().tab().tab().tab();
       //     sb.append("ImageOps.setColor(" + name + ", " + var1 + ", " + var2 + ", ");
                 String var1 = assignmentStatement.getSelector().getX().getText();
                 String var2 = assignmentStatement.getSelector().getY().getText();
                 sb.append("for( int " + var1 + " = 0; " + var1 + " < " + name + ".getWidth(); " + var1 + "++)").newline().tab().tab().tab();
                 sb.append("for( int " + var2 + " = 0; " + var2 + " < " + name + ".getHeight(); " + var2 + "++)").newline().tab().tab().tab().tab();
                 sb.append("ImageOps.setColor(" + name + ", " + var1 + ", " + var2 + ", ");



            expr.visit(this, sb);
            sb.rparen();
        } else if (assignmentStatement.getTargetDec().getType() == Type.IMAGE && expr.getCoerceTo() == Type.INT) {
            //   sb.append("for(int " + assignmentStatement.getSelector().getX() + " =0; "+ assignmentStatement.getSelector().getX() + " < " + name + ".getWidth();\n");
            // sb.append(assignmentStatement.getSelector().getX() + )
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ImageOps");
            }
            assignmentStatement.getSelector().visit(this, fakeOne);
            String[] splitArr = fakeOne.getString().split(",");
            String var1 = splitArr[0].trim();
            String var2 = splitArr[1].trim();
            sb.append("for( int " + var1 + " = 0; " + var1 + " < " + name + ".getWidth(); " + var1 + "++)").newline().tab().tab().tab();
            sb.append("for( int " + var2 + " = 0; " + var2 + " < " + name + ".getHeight(); " + var2 + "++)").newline().tab().tab().tab().tab();
            sb.append("ImageOps.setColor(" + name + ", " + var1 + ", " + var2 + ", ");
            expr.visit(this, sb);
            sb.rparen();


        }




    }



    //TODO problem with this
 /*
        if (assignmentStatement.getTargetDec().getType() == Type.IMAGE) {
            //TODO check if this works/ this was given pseudocode by a ta, might not work for us
            // if target type image
            //   if get pixel selector != null
            //handle x and y
            //else
            //if expr.getCoerceTo()==type.color
            //imageops.setcolor
            //else rhs is image
            //if dim != null
            //resize rhs   imageops.resize(rhs)
            //else
            //clone
            //else
            //append(assignmentstatement.getname "=" visitexpr)
            if (assignmentStatement.getSelector() != null) {
                assignmentStatement.getSelector().getX().visit(this, sb);
                sb.comma();
                assignmentStatement.getSelector().getY().visit(this, sb);
            }
            else {
                if (expr.getCoerceTo() == Type.COLOR) {
                    sb.append("ImageOps.setcolor(" + name + ",");
                    //TODO check if this should be get x and get y instead
                    assignmentStatement.getSelector().visit(this, sb);
                    sb.comma();
                    assignmentStatement.getExpr().visit(this, sb);
                    sb.append(")");
                }
                else if (expr.getCoerceTo() == Type.COLOR) {
                    if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                        imports.add("edu.ufl.cise.plc.runtime.ImageOps");
                    }
                    assignmentStatement.getSelector().visit(this, fakeOne);
                    String[] splitArr = fakeOne.getString().split(",");
                    String var1 = splitArr[0].trim();
                    String var2 = splitArr[1].trim();
                    sb.append("for( int " + var1 + " = 0; " + var1 + " < " + name + ".getWidth(); " + var1 + "++)").newline().tab().tab().tab();
                    sb.append("for( int " + var2 + " = 0; " + var2 + " < " + name + ".getHeight(); " + var2 + "++)").newline().tab().tab().tab().tab();
                    sb.append("ImageOps.setColor(" + name + ", " + var1 + ", " + var2 + ", ");
                    expr.visit(this, sb);
                    sb.rparen();
                }
                else {
                    // expr.setCoerceTo(Type.IMAGE);
                    if (assignmentStatement.getTargetDec().getDim() != null) {
                        sb.append("ImageOps.resize(");
                        assignmentStatement.getName();
                        sb.comma();
                        assignmentStatement.getSelector().visit(this,sb);
                        sb.append(")");
                    } else {
                        sb.append("ImageOps.clone(");
                        expr.visit(this,sb);
                        sb.append(")");
                    }
                }
            }
        }
*/


/*
            ///TODO is this namedef for assignment statement
            if(assignmentStatement.getExpr().getType() == Type.IMAGE && assignmentStatement.getTargetDec().getDim() != null&& expr.getCoerceTo()==Type.COLOR ){
             //   sb.append("for(int " + assignmentStatement.getSelector().getX() + " =0; "+ assignmentStatement.getSelector().getX() + " < " + name + ".getWidth();\n");
               // sb.append(assignmentStatement.getSelector().getX() + )
                if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                    imports.add("edu.ufl.cise.plc.runtime.ImageOps");
                }
                assignmentStatement.getSelector().visit(this, fakeOne);
                String [] splitArr = fakeOne.getString().split(",");
                String var1 = splitArr[0].trim();
                String var2 = splitArr[1].trim();
                sb.append("for( int " + var1 + " = 0; " + var1 + " < " + name + ".getWidth(); " + var1 + "++)").newline().tab().tab().tab();
                sb.append("for( int " + var2 + " = 0; " + var2 + " < " + name + ".getHeight(); " + var2 + "++)").newline().tab().tab().tab().tab();
                sb.append("ImageOps.setColor(" + name + ", " + var1 + ", " + var2 + ", ");
                expr.visit(this, sb);
                sb.rparen();
            }
            */


    else {
        sb.append(name);
        sb.equals();
        if (expr.getCoerceTo() != expr.getType() && expr.getCoerceTo() != null) {
            //			if(declaration.getExpr().)
            genTypeConversionNoParen(expr.getType(), expr.getCoerceTo(), sb);
            sb.lparen();
            expr.visit(this, sb);
            sb.rparen();
        }
        else {
            expr.visit(this, sb);
            //	        sb.append(expr.getText());
        }
    }
    sb.semi();
    sb.newline();





    return sb;
}
/*
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        // if target type image
        //   if get pixel selector != null
        //handle x and y
        //else
        //if expr.getCoerceTo()==type.color
        //imageops.setcolor
        //else rhs is image
        //if dim != null
        //resize rhs   imageops.resize(lhs)
        //else
        //clone
        //else
        //append(assignmentstatement.getname "=" visitexpr)
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        CodeGenStringBuilder fakeOne = new CodeGenStringBuilder();
        String name = assignmentStatement.getName();
        Expr expr = assignmentStatement.getExpr();
        if (assignmentStatement.getTargetDec().getType() == Type.IMAGE) {
            //TODO check if this works/ this was given pseudocode by a ta, might not work for us
            // if target type image
            //   if get pixel selector != null
            //handle x and y
            //else
            //if expr.getCoerceTo()==type.color
            //imageops.setcolor
            //else rhs is image
            //if dim != null
            //resize rhs   imageops.resize(lhs)
            //else
            //clone
            //else
            //append(assignmentstatement.getname "=" visitexpr)
            if (assignmentStatement.getSelector() != null) {
                assignmentStatement.getSelector().getX().visit(this, sb);
                sb.comma();
                assignmentStatement.getSelector().getY().visit(this, sb);
            }
            else {
                if (expr.getCoerceTo() == Type.COLOR) {
                    if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
                        imports.add("edu.ufl.cise.plc.runtime.ImageOps");
                    }
                    assignmentStatement.getSelector().visit(this, fakeOne);
                    String [] splitArr = fakeOne.getString().split(",");
                    String var1 = splitArr[0].trim();
                    String var2 = splitArr[1].trim();
                    sb.append("for( int " + var1 + " = 0; " + var1 + " < " + name + ".getWidth(); " + var1 + "++)").newline().tab().tab().tab();
                    sb.append("for( int " + var2 + " = 0; " + var2 + " < " + name + ".getHeight(); " + var2 + "++)").newline().tab().tab().tab().tab();
                    sb.append("ImageOps.setColor(" + name + ", " + var1 + ", " + var2 + ", ");
                    expr.visit(this, sb);
                    sb.rparen();
                }
                else {
                    // expr.setCoerceTo(Type.IMAGE);
                    if (assignmentStatement.getTargetDec().getDim() != null) {
                        sb.append("ImageOps.resize(");
                        assignmentStatement.getName();
                        sb.comma();
                        assignmentStatement.getSelector().visit(this,sb);
                        sb.append(")");
                    } else {
                        sb.append("ImageOps.clone(");
                        expr.visit(this,sb);
                        sb.append(")");
                    }
                }
            }
        }
        else {
            sb.append(name);
            sb.equals();
            if (expr.getCoerceTo() != expr.getType() && expr.getCoerceTo() != null) {
                //			if(declaration.getExpr().)
                genTypeConversionNoParen(expr.getType(), expr.getCoerceTo(), sb);
                sb.lparen();
                expr.visit(this, sb);
                sb.rparen();
            }
            else {
                expr.visit(this, sb);
                //	        sb.append(expr.getText());
            }
        }
        sb.semi();
        sb.newline();
        return sb;
    }
*/



    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        // TODO maybe need fix to have printstream
        // PrintStream stream = ConsoleIO.console.println();
        // ConsoleIO.console.println(<source>) ;
        if(writeStatement.getDest().getType() == Type.CONSOLE && writeStatement.getSource().getType() == Type.IMAGE){
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ConsoleIO") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ConsoleIO");
            }
            sb.append("ConsoleIO.displayImageOnScreen(");
            writeStatement.getSource().visit(this, sb);
            sb.rparen().semi().newline();
        }
        else if(writeStatement.getDest().getType() == Type.STRING && writeStatement.getSource().getType() == Type.IMAGE ){
            //TODO check if <sourceImage> is represented here
            if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
            }
            sb.append("FileURLIO.writeImage(");
            writeStatement.getSource().visit(this, sb);
            sb.comma().space();
            writeStatement.getDest().visit(this, sb);
            sb.rparen().semi().newline();



        }else if(writeStatement.getDest().getType() == Type.STRING  && writeStatement.getSource().getType() != Type.IMAGE){
            if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
            }
            sb.append("FileURLIO.writeValue(");
            writeStatement.getSource().visit(this, sb);
            sb.comma().space();
            writeStatement.getDest().visit(this, sb);
            sb.rparen().semi().newline();

        }
        else {
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ConsoleIO") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ConsoleIO");
            }
            sb.append("ConsoleIO.console.println(");
            writeStatement.getSource().visit(this, sb);
            sb.rparen().semi().newline();
        }

        return sb;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        String name = readStatement.getName();
        Expr expr = readStatement.getSource();
        if (readStatement.getSource().getType() != Type.CONSOLE) {
            //TODO Check if should be placed here
            if(readStatement.getTargetDec().getDim() != null &&  readStatement.getSource().getType() ==Type.IMAGE){
                if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") == -1) {
                    imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
                }
                //TODO this might not be right
                readStatement.getTargetDec().getDim().visit(this, sb);
                sb.equals();
                sb.append("FileURLIO.readImage(");
                readStatement.getSource().visit(this, sb);
                sb.comma();
                readStatement.getSelector().visit(this,sb);
                sb.rparen().semi();


            }
            else if(readStatement.getSource().getType() == Type.STRING && readStatement.getTargetDec().getType() == Type.IMAGE) {
                if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") == -1) {
                    imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
                }
                if (readStatement.getTargetDec().getDim() != null) {
                	if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
						imports.add("edu.ufl.cise.plc.runtime.ImageOps");
					}
                	sb.append(name).space();
	                sb.equals().space();
					sb.append("ImageOps.resize(");
					sb.append("FileURLIO.readImage(");
					readStatement.getSource().visit(this, sb);
					sb.rparen().comma().space();
					readStatement.getTargetDec().getDim().visit(this, sb);
					sb.rparen().semi().newline();
                }
                else {
	                sb.append(name).space();
	                sb.equals().space();
	                genTypeConversionNoParen(readStatement.getSource().getType(), readStatement.getTargetDec().getType(), sb);
	                sb.space().append("FileURLIO.readImage(");
	                readStatement.getSource().visit(this, sb);
	                sb.rparen().semi().newline();
                }
            }
            else if(readStatement.getSource().getType() != Type.IMAGE) {
                if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") == -1) {
                    imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
                }
                sb.append(name).space();
                sb.equals().space();
                genTypeConversionNoParen(readStatement.getSource().getType(), readStatement.getTargetDec().getType(), sb);
                sb.space().append("FileURLIO.readValueFromFile(");
                readStatement.getSource().visit(this, sb);
                sb.rparen().semi().newline();
            }
            else{
                if( readStatement.getSource().getType() ==Type.IMAGE) {
                    if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") == -1) {
                        imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
                    }
                    readStatement.getTargetDec().getDim().visit(this, sb);
                    sb.equals();
                    sb.append("FileURLIO.readImage(");
                    readStatement.getSource().visit(this, sb);
                    sb.rparen().semi();


                }
            }

        } else {
            sb.append(name);
            sb.equals();
            expr.visit(this, sb);
            sb.semi().newline();
        }

        return sb;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {

        CodeGenStringBuilder sb = new CodeGenStringBuilder();
        List<NameDef> namedef = program.getParams();
        List<ASTNode> decAndStatement = program.getDecsAndStatements();
        // sb.append("package " + packageName + ";").newline();
        sb.append("public class ");
        sb.append(program.getName());
        sb.LCurl().newline();
        sb.append("\tpublic static ");
        sb.append(Types.toString(program.getReturnType()));
        sb.append(" apply(");

        if (namedef.size() > 1) {
            namedef.get(0).visit(this, sb);
            for (int x = 1; x < namedef.size(); x++) {
                sb.comma().space();
                namedef.get(x).visit(this, sb);
            }
        } else {
            for (int x = 0; x < namedef.size(); x++) {
                namedef.get(x).visit(this, sb);
            }

        }
        sb.rparen().LCurl().newline();
        for (int x = 0; x < decAndStatement.size(); x++) {
            sb.tab().tab();
            decAndStatement.get(x).visit(this, sb);
            sb.newline();
        }
        sb.tab().RCurl().newline().RCurl();

        if (imports.size() > 0) {
            for (int x = 0; x < imports.size(); x++) {
                sb.insert(0, "import " + imports.get(x) + ";\n");
            }
        }
        if(packageName != null && packageName != "") {
            sb.insert(0, "package " + packageName + ";\n");
        }
        return sb.getString();
    }
    // public Object visitParams(List<NameDef> namedef, Object arg) throws Exception
    // {

    // }
    // public Object visitImport(Program program, Object arg) throws Exception {

    // }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {

        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Type type = nameDef.getType();
        String name = nameDef.getName();

        if(type == Type.IMAGE) {
            sb.append("BufferedImage ");
        }
        else if (type == Type.COLOR) {
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
            }
            sb.append("ColorTuple").space();
        }
        else if (type == Type.STRING) {
            sb.append("String").space();
        } else {
            sb.append(String.valueOf(type).toLowerCase()).space();
        }
        sb.append(name);

        return sb;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        Type type = nameDefWithDim.getType();
        String name = nameDefWithDim.getName();

        if(type == Type.IMAGE) {
            sb.append("BufferedImage ");
        }
        else if(type == Type.COLOR) {
            if (imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
                imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
            }
            sb.append("ColorTuple").space();
        }
        else {
            if (type == Type.STRING) {
                sb.append("String").space();
            } else {
                sb.append(String.valueOf(type).toLowerCase()).space();
            }
        }
        sb.append(name).space();

        return sb;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") != -1) {
            sb.append("FileURLIO.closeFiles();").newline();
            sb.tab().tab();
        }
        Expr expr = returnStatement.getExpr();
        sb.append("return ");
        expr.visit(this, sb);
        sb.semi();
        return sb;
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        IToken opExpr = declaration.getOp();
        //System.out.println(declaration.getType() + " " + declaration.getExpr().getType());
        if (declaration.getOp() == null) {
            declaration.getNameDef().visit(this, sb);
            if(declaration.getNameDef().getType() == Type.IMAGE) {
                if (imports.indexOf("java.awt.image.BufferedImage") == -1) {
                    imports.add("java.awt.image.BufferedImage");
                }
                sb.equals().space();
                sb.append("new BufferedImage(");
                declaration.getDim().visit(this, sb);
                sb.append(", BufferedImage.TYPE_INT_RGB)");
            }
            sb.semi();
        }
        else {
            Kind op = declaration.getOp().getKind();
            if (op == Kind.ASSIGN || op == Kind.LARROW) {
                declaration.getNameDef().visit(this, sb);
                sb.equals();
                //sb.append(opExpr.getText());
                if (declaration.getNameDef().getType() == Type.IMAGE) {
                    if (imports.indexOf("java.awt.image.BufferedImage") == -1) {
                        imports.add("java.awt.image.BufferedImage");
                    }
                    if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") == -1) {
                        imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
                    }
                    sb.space();
                    if(declaration.getDim() == null) {
                        //BinaryExpr x = (BinaryExpr) declaration.getExpr();
                        String x = declaration.getExpr().toString();
                        String[] y = x.split(" ");
                        if(!y[0].equals("BinaryExpr")) {
                        	if(declaration.getExpr().getType() == Type.IMAGE) {
								if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
									imports.add("edu.ufl.cise.plc.runtime.ImageOps");
								}
								sb.append("ImageOps.clone(");
								declaration.getExpr().visit(this, sb);
								sb.rparen();
							}
							else {
								sb.append("FileURLIO.readImage(");
								declaration.getExpr().visit(this, sb);
								sb.rparen();
							}
                        }
                        else {
                            declaration.getExpr().visit(this, sb);

                        }
                    }
                    else if(declaration.getExpr().getType() == Type.IMAGE) {
						if (imports.indexOf("edu.ufl.cise.plc.runtime.ImageOps") == -1) {
							imports.add("edu.ufl.cise.plc.runtime.ImageOps");
						}
						sb.append("ImageOps.resize(");
						declaration.getExpr().visit(this, sb);
						sb.comma().space();
						declaration.getDim().visit(this, sb);
						sb.rparen();
					}
                    else {
                        sb.append("FileURLIO.readImage(");
                        declaration.getExpr().visit(this, sb);
                        sb.comma().space();
                        declaration.getDim().visit(this, sb);
                        sb.rparen();
                    }
                }
                else {
                    if(op == Kind.LARROW && declaration.getExpr().getType() == Type.STRING) {
                        if (imports.indexOf("edu.ufl.cise.plc.runtime.FileURLIO") == -1) {
                            imports.add("edu.ufl.cise.plc.runtime.FileURLIO");
                        }
                        sb.space();
                        genTypeConversionNoParen(declaration.getExpr().getType(), declaration.getNameDef().getType(), sb);
                        sb.space().append("FileURLIO.readValueFromFile(");
                        declaration.getExpr().visit(this, sb);
                        sb.rparen();
                    }
                    else if (declaration.getType() == Type.INT && declaration.getExpr().getType() == Type.COLOR) {
                        if (imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
                            imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
                        }
                        sb.space();
                        //sb.append("ColorTuple.pack(");
                        declaration.getExpr().visit(this, sb);
                        sb.append(".pack()");
                    }
                    else if (declaration.getType() == Type.COLOR && declaration.getExpr().getType() == Type.INT) {
                        if (imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
                            imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
                        }
                        sb.append("ColorTuple.unpack(");
                        declaration.getExpr().visit(this, sb);
                        sb.rparen();
                    }
                    else if (declaration.getExpr().getCoerceTo() != declaration.getExpr().getType() && declaration.getExpr().getCoerceTo() != null) {
                        //					if(declaration.getExpr().)
                        genTypeConversionNoParen(declaration.getType(), declaration.getExpr().getCoerceTo(), sb);
                        sb.lparen();
                        declaration.getExpr().visit(this, sb);
                        sb.rparen();
                    }
                    else {
                        declaration.getExpr().visit(this, sb);
                    }
                }

                sb.semi();
            }
//            else if(op == Kind.LARROW) {
//            	if(declaration.getExpr().getType() != Type.CONSOLE) {
//            		throw new UnsupportedOperationException("rhs of read assignment is not console: " + declaration.getExpr().getType());
//            	}
//            	declaration.getNameDef().visit(this, sb);
//            	sb.append(opExpr.getText());
//            	declaration.getExpr().visit(this, sb);
//            	sb.semi();
//            }
        }

        return sb;

    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        if(imports.indexOf("edu.ufl.cise.plc.runtime.ColorTuple") == -1) {
            imports.add("edu.ufl.cise.plc.runtime.ColorTuple");
        }
        CodeGenStringBuilder sb = (CodeGenStringBuilder) arg;
        sb.append("ColorTuple.unpack(");
        unaryExprPostfix.getExpr().visit(this,sb);
        sb.append(".getRGB(");
        unaryExprPostfix.getSelector().visit(this, sb);
        sb.append("))");



        return sb;
    }

    public void genTypeConversion(Type type, Type coerceTo, CodeGenStringBuilder sb) {
        sb.append("((" + Types.toString(coerceTo) + ")");
    }

    public void genTypeConversionNoParen(Type type, Type coerceTo, CodeGenStringBuilder sb) {
        sb.append("(" + Types.toString(coerceTo) + ")");
    }


    // Function to turn primitives to wrapper types (i.e. int -> Integer)
    private String primitiveToWrapper(String primitive) {
        return switch (primitive) {
            case "boolean" -> "Boolean";
            case "float" -> "Float";
            case "int" -> "Integer";
            case "String" -> "String";
            default -> throw new IllegalArgumentException("No wrapper for type: " + primitive);
        };
    }



}