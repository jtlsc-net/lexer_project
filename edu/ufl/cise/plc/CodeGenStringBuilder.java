package edu.ufl.cise.plc;

public class CodeGenStringBuilder {
    StringBuilder delegate;
    //methods reimplemented—just call the delegates method
    public CodeGenStringBuilder append(String s){
        delegate.append(s);
        return this;
    }


    public CodeGenStringBuilder comma(){
        delegate.append(",");
        return this;
    }
    public CodeGenStringBuilder stringQuotes(){
        delegate.append("\"\"\"");
        return this;
    }


    public CodeGenStringBuilder lparen() {
        delegate.append("(");
        return this;
    }

    public CodeGenStringBuilder rparen() {
        delegate.append(")");
        return this;
    }
    public CodeGenStringBuilder equals() {
        delegate.append("=");
        return this;
    }

    public CodeGenStringBuilder semi() {
        delegate.append(";");
        return this;
    }

    public CodeGenStringBuilder newline() {
        //TODO fix
        delegate.append("\n");
        return this;
    }
}