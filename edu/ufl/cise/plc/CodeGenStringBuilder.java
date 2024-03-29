package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.Types;

public class CodeGenStringBuilder {
    StringBuilder delegate;
    //methods reimplemented—just call the delegates method
    public CodeGenStringBuilder() {
    	delegate = new StringBuilder();
    }
    public String getString() {
    	return delegate.toString();
    }
    public CodeGenStringBuilder append(String s){
        delegate.append(s);
        return this;
    }
    public CodeGenStringBuilder append(Types type){
        delegate.append(type.toString());
        return this;
    }
    public CodeGenStringBuilder comma(){
        delegate.append(",");
        return this;
    }
    public CodeGenStringBuilder LCurl(){
        delegate.append("{");
        return this;
    }
    public CodeGenStringBuilder RCurl(){
        delegate.append("}");
        return this;
    }
    public CodeGenStringBuilder question(){
        delegate.append("?");
        return this;
    }
    public CodeGenStringBuilder colon(){
        delegate.append(":");
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
    public CodeGenStringBuilder tab() {
        delegate.append("\t");
        return this;
    }

    public CodeGenStringBuilder semi() {
        delegate.append(";");
        return this;
    }

    public CodeGenStringBuilder newline() {
        //TODO check if right
        delegate.append("\n");
        return this;
    }
    public CodeGenStringBuilder space() {
    	delegate.append(" ");
    	return this;
    }
    public CodeGenStringBuilder insert(int offset, String str) {
    	delegate.insert(offset, str);
    	return this;
    }
}