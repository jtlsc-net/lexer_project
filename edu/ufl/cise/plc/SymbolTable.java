package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Types;

import java.util.HashMap;

public class SymbolTable {

    //TODO:  Implement a symbol table class that is appropriate for this language.
    HashMap<String, Declaration> symbolMap = new HashMap<>();

    public boolean insert(String name, Declaration declaration) {
        return (symbolMap.putIfAbsent(name,declaration) == null);
    }

    public Declaration  lookup(String name){
        return symbolMap.get(name);
    }


}
