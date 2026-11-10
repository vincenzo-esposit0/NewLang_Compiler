package visitor;

import nodes.*;
import esercitazione5.*;
import table.SymbolRecord;
import table.SymbolTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class MyCTranslatorVisitor implements MyVisitor {

    private Stack<SymbolTable> stackScope = new Stack<>();
    private String codeGeneratorC = "";
    private boolean varDeclGlobal = true;

    @Override
    public String visit(ASTNode node) {
        codeGeneratorC = "";

        switch (node.getClass().getSimpleName()) {
            case "ProgramNode" -> visitProgramNode((ProgramNode) node);
            case "VarDeclNode" -> visitVarDeclNode((VarDeclNode) node);
            case "FunDeclNode" -> visitFunDeclNode((FunDeclNode) node);
            case "BodyNode" -> visitBodyNode((BodyNode) node);
            case "ParDeclNode" -> visitParDeclNode((ParDeclNode) node);
            case "FunCallNode" -> visitFunCallNode((FunCallNode) node);
            case "FunCallExprNode" -> visitFunCallExprNode((FunCallExprNode) node);
            case "FunCallStatNode" -> visitFunCallStatNode((FunCallStatNode) node);
            case "IfStatNode" -> visitIfStatNode((IfStatNode) node);
            case "ElseNode" -> visitElseNode((ElseNode) node);
            case "ForStatNode" -> visitForStatNode((ForStatNode) node);
            case "WhileStatNode" -> visitWhileStatNode((WhileStatNode) node);
            case "ReadStatNode" -> visitReadStatNode((ReadStatNode) node);
            case "WriteStatNode" -> visitWriteStatNode((WriteStatNode) node);
            case "AssignStatNode" -> visitAssignStatNode((AssignStatNode) node);
            case "ReturnStatNode" -> visitReturnStatNode((ReturnStatNode) node);
            case "BiVarExprNode" -> visitBiVarExprNode((BiVarExprNode) node);
            case "UniVarExprNode" -> visitUniVarExprNode((UniVarExprNode) node);
            case "ConstNode" -> visitConstNode((ConstNode) node);
            case "IdNode" -> visitIdNode((IdNode) node);
        }

        return codeGeneratorC;
    }

    private void visitProgramNode(ProgramNode node) {
        StringBuilder sb = new StringBuilder();

        stackScope.push(node.getSymbolTable());

        codeGeneratorC += includeLibraries();

        ArrayList<VarDeclNode> varDeclNodeList = node.getVarDeclList();
        ArrayList<FunDeclNode> funDeclNodeList = node.getFunDeclList();

        Collections.reverse(varDeclNodeList);

        //Definisco i prototipi delle funzioni
        for(FunDeclNode funElement: funDeclNodeList){
            FunDeclNode funDecl;

            if(funElement.isMain()){
                funDecl = funElement.getFunDecl();
            } else{
                funDecl = funElement;
            }


            if(funDecl != null){
                //Intestazione della funzione
                sb.append(typeConverter(numericToString(funDecl.getAstType())))
                        .append(" ")
                        .append(funDecl.getId().getNomeId())
                        .append("(");

                //Analisi dei parametri
                if(funDecl.getParDeclList() != null){
                    for(ParDeclNode parElement: funDecl.getParDeclList()){
                        //Questo for mi serve per inserire nei prototipi i tipi che si aspetta di ottenere
                        for(IdInitNode idElement: parElement.getIdList()){
                                sb.append(typeConverter(parElement.getTypeVar()));

                            if(parElement.getOut()){
                                sb.append("*");
                            }
                            sb.append(",");
                        }
                    }
                    //Tolgo l'ultima virgola che viene messa in automatico
                    sb.deleteCharAt(sb.length()-1);
                }
                sb.append(");\n");
            }
        }

        codeGeneratorC += sb.toString();

        codeGeneratorC += converterFunctions();

        String code = codeGeneratorC;
        sb.setLength(0);

        for(VarDeclNode varDecl : varDeclNodeList){
            if(varDecl != null){
                sb.append(varDecl.accept(this));
            }
        }

        varDeclGlobal = false;
        for(FunDeclNode funDecl : funDeclNodeList){
            if(funDecl != null){
                sb.append(funDecl.accept(this));
            }
        }

        codeGeneratorC = code + sb;
        stackScope.pop();
    }

    private void visitVarDeclNode(VarDeclNode node) {
        StringBuilder sb = new StringBuilder();

        String varType = node.getType();

        ArrayList<IdInitNode> idInitList = node.getIdInitList();
        ArrayList<IdInitNode> idInitObblList = node.getIdInitObblList();

        if(varType != null || node.isVar()){
            //Controllo se il flag della variabili globali è TRUE
            if(varDeclGlobal){

                //Se è di tipo VAR
                if(node.isVar()){
                    for (IdInitNode elObbl: idInitObblList){
                        genericVarElement(elObbl, sb);
                    }
                } else {
                    for (IdInitNode el: idInitList){
                        if(el.getExpr() != null) {
                            sb.append(typeConverter(varType))
                                    .append(" ")
                                    .append(el.getId().getNomeId())
                                    .append(" = ")
                                    .append(el.getExpr().accept(this))
                                    .append(";\n");
                        } else {
                            sb.append(typeConverter(varType))
                                    .append(" ")
                                    .append(el.getId().getNomeId())
                                    .append(";\n");
                        }
                    }
                }
            } else {
                if(node.isVar()){
                    for(IdInitNode elObbl: idInitObblList){
                        genericVarElement(elObbl, sb);
                    }
                } else{
                    for(IdInitNode el: idInitList){
                        //Se è una STRINGA viene trattato diversamente
                        if(typeConverter(varType).equals("char*")){
                            //Il typeConverter mi restituirà il tipo in C
                            sb.append(typeConverter(varType))
                                    .append(" ")
                                    .append(el.getId().getNomeId());

                            if (el.getExpr() != null) {
                                sb.append(" = ")
                                        .append(el.getExpr().accept(this));
                            } else {
                                sb.append(" = \"\"");
                            }

                        } else {
                            sb.append(typeConverter(varType))
                                    .append(" ")
                                    .append(el.getId().getNomeId());

                            if (el.getExpr() != null) {
                                sb.append(" = ")
                                        .append(el.getExpr().accept(this));
                            }
                        }

                        sb.append(";\n");
                    }
                }
            }
        }

        codeGeneratorC = sb.toString();
    }

    private void visitFunDeclNode(FunDeclNode node) {
        StringBuilder sb = new StringBuilder();

        FunDeclNode funDeclNode;

        if(node.isMain()){
            funDeclNode = node.getFunDecl();
        } else{
            funDeclNode = node;
        }

        stackScope.push(funDeclNode.getSymbolTable());

        if(node.isMain()){
            generateFunction(sb, funDeclNode);

            sb.setLength(0);

            codeGeneratorC += "int main(int argc, char** argv){ \n";
            codeGeneratorC += funDeclNode.getId().getNomeId()+"(";

            //Generazione dei parametri della funzione
            paramGeneration(sb, funDeclNode);

            codeGeneratorC += sb + ");\n";
            codeGeneratorC += "return (EXIT_SUCCESS);\n}\n";
        }
        else {
            generateFunction(sb, funDeclNode);
        }

        stackScope.pop();
    }

    private void visitBodyNode(BodyNode node) {
        StringBuilder sb = new StringBuilder();

        ArrayList<VarDeclNode> varDeclNodeList = node.getVarDeclList();
        ArrayList<StatNode> statNodeList = node.getStatList();

        //array contenenti la lista di varElement finale per assigned e not assigned
        ArrayList<VarDeclNode> varNotAssignedList = new ArrayList<>();
        ArrayList<VarDeclNode> varAssignedList = new ArrayList<>();

        // Separo le dichiarazioni di variabili in due liste: assegnate e non assegnate
        for (VarDeclNode varElement : varDeclNodeList) {
            if (varElement.getIdInitList() != null) {
                //liste temporanee per tenere traccia di tutti gli elementi presenti in varElement
                ArrayList<IdInitNode> notAssignedList = new ArrayList<>();
                ArrayList<IdInitNode> assignedList = new ArrayList<>();

                for (int i = 0; i < varElement.getIdInitList().size(); i++) {
                    IdInitNode idElement = varElement.getIdInitList().get(i);
                    if (idElement.getExpr() == null) {
                        notAssignedList.add(idElement);
                    } else {
                        assignedList.add(idElement);
                    }

                    //controllo dell'ultimo elemento di varElement in modo da riempire le liste di assigned e notAssigned
                    if (i == varElement.getIdInitList().size() - 1) {
                        if(!notAssignedList.isEmpty()) {
                            VarDeclNode newVarNotAssigned = new VarDeclNode(varElement.getName(), varElement.getType(), new ArrayList<>(notAssignedList));
                            varNotAssignedList.add(newVarNotAssigned);
                        }

                        if (!assignedList.isEmpty()) {
                            VarDeclNode newVarAssigned = new VarDeclNode(varElement.getName(), varElement.getType(), new ArrayList<>(assignedList));
                            varAssignedList.add(0, newVarAssigned);
                        }
                    }
                }
            }

            if (varElement.getIdInitObblList() != null) {
                for (IdInitNode idElement : varElement.getIdInitObblList()) {
                    if (!varAssignedList.contains(varElement)) {
                        varAssignedList.add(varElement);
                    }
                }
            }

        }

        Collections.reverse(varNotAssignedList);
        Collections.reverse(varAssignedList);

        //Unisco le due liste mantenendo l'ordine delle variabili non assegnate prima di quelle assegnate
        varNotAssignedList.addAll(varAssignedList);
        varDeclNodeList.clear();
        varDeclNodeList.addAll(varNotAssignedList);

        // Itero sulla lista ordinata e genero il codice
        for (VarDeclNode varElement : varDeclNodeList) {
            if (varElement != null) {
                sb.append(varElement.accept(this));
            }
        }

        Collections.reverse(statNodeList);
        // Itero sulla lista delle istruzioni e genero il codice
        for (StatNode statElement : statNodeList) {
            if (statElement != null) {
                sb.append(statElement.accept(this));
            }
        }

        codeGeneratorC = sb.toString();
    }

    private void visitParDeclNode(ParDeclNode node) {
        StringBuilder sb = new StringBuilder();
        String typeC = typeConverter(node.getTypeVar());

        ArrayList<IdInitNode> idInitNodeList = node.getIdList();

        for(IdInitNode idElement: idInitNodeList){
            sb.append(typeC);

            if(node.getOut()){
                sb.append("* ");
            } else {
                sb.append(" ");
            }

            sb.append(idElement.getId().getNomeId()).append(",");
        }

        codeGeneratorC = sb.toString();
    }

    private void visitFunCallNode(FunCallNode node) {
        StringBuilder sb = new StringBuilder();

        String nomeID = node.getId().getNomeId();

        ArrayList<ExprNode> exprNodeList = node.getExprList();
        ArrayList<Integer> parCallList = new ArrayList<>();

        if (exprNodeList != null) {
            for (ExprNode exprElement : exprNodeList) {
                exprElement.accept(this);
                parCallList.add(exprElement.getAstType());
            }
        }

        SymbolRecord symbolRecord = lookup(nomeID);

        ArrayList<Integer> paramsTypeList = symbolRecord.getParInitialize().getParamsTypeList();
        ArrayList<Boolean> paramsOutList = symbolRecord.getParInitialize().getParamsOutList();

        if (parCallList.size() == paramsTypeList.size() && parCallList.size() == paramsOutList.size() && !parCallList.isEmpty()) {
            for (int i = 0; i < parCallList.size(); i++) {
                if (parCallList.get(i).equals(paramsTypeList.get(i)) && paramsOutList.get(i)) {
                    sb.append("&").append(exprNodeList.get(i).accept(this)).append(",");
                } else {
                    sb.append(exprNodeList.get(i).accept(this)).append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        codeGeneratorC = sb.toString();
    }

    private void visitFunCallExprNode(FunCallExprNode node) {
        FunCallNode funCallNode = node.getFunCall();

        String nomeID = node.getFunCall().getId().getNomeId();

        SymbolRecord symbolRecord = lookup(nomeID);

        if(symbolRecord != null){
            codeGeneratorC += nomeID + "(" + funCallNode.accept(this) + ")";
        }
    }

    private void visitFunCallStatNode(FunCallStatNode node) {
        FunCallNode funCallNode = node.getFunCall();
        String nomeID = node.getFunCall().getId().getNomeId();

        SymbolRecord symbolRecord = lookup(nomeID);

        if(symbolRecord != null){
            codeGeneratorC += nomeID + "(" + funCallNode.accept(this) + "); \n";
        }
    }

    private void visitIfStatNode(IfStatNode node) {
        StringBuilder sb = new StringBuilder();

        ExprNode exprNode = node.getExpr();
        BodyNode bodyNode = node.getBody();

        stackScope.push(node.getSymbolTable());

        sb.append("if(").append(exprNode.accept(this)).append("){\n");
        sb.append("\t").append(bodyNode.accept(this)).append("}\n");

        if(node.getElseStat() != null){
            sb.append("else").append("{\n");
            sb.append("\t").append(node.getElseStat().accept(this)).append("}\n");
        }

        codeGeneratorC = sb.toString();
        stackScope.pop();
    }

    private void visitElseNode(ElseNode node) {
        StringBuilder sb = new StringBuilder();
        BodyNode bodyNode = node.getBody();

        stackScope.push(node.getSymbolTable());

        sb.append(bodyNode.accept(this));

        codeGeneratorC = sb.toString();
        stackScope.pop();
    }

    private void visitForStatNode(ForStatNode node) {
        StringBuilder sb = new StringBuilder();

        String intConst1 = node.getIntConst1().getValue();
        String intConst2 = node.getIntConst2().getValue();
        BodyNode bodyNode = node.getBody();
        String id = node.getId().getNomeId();

        stackScope.push(node.getSymbolTable());

        if(Integer.parseInt(intConst1) > Integer.parseInt(intConst2)) {
            sb.append("\tint ").append(id).append(";\n");
            sb.append("\tfor(").append(id).append(" = ").append(intConst1).append(";").append(" ").append(id).append(" >= ").append(intConst2).append(";").append(id).append("--").append("){\n");
            sb.append("\t\t").append(bodyNode.accept(this));
            sb.append("\t}\n");
        } else {
            sb.append("\tint ").append(id).append(";\n");
            sb.append("\tfor(").append(id).append(" = ").append(intConst1).append(";").append(" ").append(id).append(" <= ").append(intConst2).append(";").append(id).append("++").append("){\n");
            sb.append("\t\t").append(bodyNode.accept(this));
            sb.append("\t}\n");
        }

        codeGeneratorC = sb.toString();
        stackScope.pop();
    }

    private void visitWhileStatNode(WhileStatNode node) {
        StringBuilder sb = new StringBuilder();

        ExprNode exprNode = node.getExpr();
        BodyNode bodyNode = node.getBody();

        stackScope.push(node.getSymbolTable());

        sb.append("while(").append(exprNode.accept(this)).append("){\n");
        sb.append(bodyNode.accept(this)).append("}\n");

        codeGeneratorC = sb.toString();
        stackScope.pop();
    }

    private void visitReadStatNode(ReadStatNode node) {
        StringBuilder sb = new StringBuilder();

        ArrayList<IdInitNode> idInitNodeList = node.getIdList();
        ConstNode constNode = node.getStringConst();

        //Stampa il valore della const nel caso di a <-- "inserisci un intero:";
        if (constNode != null) {
            sb.append("\tprintf(\"").append(constNode.getValue()).append("\\n\");").append("\n");
        }

        codeGeneratorC += sb.toString();

        for(IdInitNode idElement: idInitNodeList){

            if(idElement.getAstType() == sym.STRING){
                //Prima di prendere da input le stringhe dichiaro una malloc(256)
                sb.append(idElement.getId().getNomeId())
                        .append(" = ")
                        .append("malloc(256)")
                        .append(";\n");

                sb.append("\tscanf(")
                        .append(formatOut(idElement.getAstType(), "scanf"))
                        .append(",")
                        .append(idElement.getId().getNomeId())
                        .append(");\n");
            } else{
                sb.append("\tscanf(")
                        .append(formatOut(idElement.getAstType(), "scanf"))
                        .append(",&")
                        .append(idElement.getId().getNomeId())
                        .append(");\n");
            }
        }

        codeGeneratorC = sb.toString();
    }

    private void visitWriteStatNode(WriteStatNode node) {
        StringBuilder sb = new StringBuilder();

        ArrayList<ExprNode> exprNodeList = node.getExprList();
        String typeWrite = node.getTypeWrite();

        Collections.reverse(exprNodeList);

        //Ogni espressione viene stampata senza a capo: (a, b) --> scrive "ab"
        for(ExprNode exprElement: exprNodeList){
            String value = exprElement.accept(this);
            //I booleani sono interi in C: si stampano come "true"/"false"
            if(exprElement.getAstType() != null && exprElement.getAstType() == sym.BOOL){
                value = "boolToString(" + value + ")";
            }
            sb.append("\tprintf(")
                    .append(formatOut(exprElement.getAstType(), "inline"))
                    .append(",")
                    .append(value)
                    .append(");\n");
        }

        //-->! (WRITELN) va a capo una sola volta dopo tutte le espressioni; --> (WRITE) no
        if(typeWrite.equals("WRITELN")){
            sb.append("\tprintf(\"\\n\");\n");
        }

        codeGeneratorC = sb.toString();
    }

    private void visitAssignStatNode(AssignStatNode node) {
        StringBuilder sb = new StringBuilder();

        ArrayList<IdInitNode> idInitNodeList = node.getIdList();
        ArrayList<ExprNode> exprNodeList = node.getExprList();

        Collections.reverse(exprNodeList);

        if(exprNodeList.size() == 1){
            for(IdInitNode idElement: idInitNodeList){
                sb.append(idElement.getId().accept(this)).append(" = ").append(exprNodeList.get(0).accept(this)).append(";\n");
            }
        } else {
            for (int i = 0; i < idInitNodeList.size(); i++) {
                sb.append(idInitNodeList.get(i).getId().getNomeId()).append(" = ");
                sb.append(exprNodeList.get(i).accept(this)).append(";\n");
            }
        }

        codeGeneratorC = sb.toString();
    }

    private void visitReturnStatNode(ReturnStatNode node) {
        StringBuilder sb = new StringBuilder();

        if(node.getExpr() != null) {
            sb.append("return ")
                    .append(node.getExpr().accept(this))
                    .append(";\n");

            codeGeneratorC = sb.toString();
        }
    }

    private void visitBiVarExprNode(BiVarExprNode node) {
        StringBuilder sb = new StringBuilder();

        ExprNode expr1 = node.getExprNode1();
        ExprNode expr2 = node.getExprNode2();
        String opExpr = node.getModeExpr();

        sb.append(checkBiVarExpr(expr1, expr2, opExpr));

        codeGeneratorC = sb.toString();
    }

    private void visitUniVarExprNode(UniVarExprNode node) {
        StringBuilder sb = new StringBuilder();

        sb.append(checkUniVarExpr(node.getModeExpr())).append(node.getExprNode().accept(this));

        codeGeneratorC = sb.toString();
    }

    private void visitConstNode(ConstNode node) {
        StringBuilder sb = new StringBuilder();
        if(node.getModeExpr().equals("ConstString")) {
            sb.append("\"").append(node.getValue()).append("\"");
        } else if (node.getModeExpr().equals("ConstChar")){
            sb.append("'").append(node.getValue()).append("'");
        } else if (MyTypeChecker.getInferenceType(node.getModeExpr()) == sym.BOOL) {
            //Il C standard non ha true/false senza stdbool.h: i booleani sono interi (1/0)
            sb.append(node.getValue().equalsIgnoreCase("true") ? "1" : "0");
        } else {
            sb.append(node.getValue());
        }

        codeGeneratorC += sb.toString();
    }

    private void visitIdNode(IdNode node) {
        StringBuilder sb = new StringBuilder();

        String nomeID = node.getNomeId();

        SymbolRecord symbolRecord = lookup(nomeID);

        if(symbolRecord.isPointer()){
            sb.append("*").append(nomeID);
        }
        else{
            sb.append(nomeID);
        }

        codeGeneratorC += sb.toString();
    }

    public String typeConverter(String typeConverter){
        return switch (typeConverter) {
            case "STRING" -> "char*";
            case "REAL" -> "float" ;
            case "INTEGER", "BOOL" -> "int";
            case "CHAR" -> "char";
            case "VOID" -> "void";
            default -> "ERROR";
        };
    }

    private String numericToString(int numericType){
        return switch (numericType) {
            case sym.INTEGER -> "INTEGER";
            case sym.REAL -> "REAL" ;
            case sym.BOOL -> "BOOL";
            case sym.STRING -> "STRING";
            case sym.CHAR -> "CHAR";
            case sym.VOID -> "VOID";
            default -> "ERROR";
        };
    }

    private void genericVarElement (IdInitNode elObbl, StringBuilder sb){
        String typeC = typeConverter(numericToString(elObbl.getId().getAstType()));

        //Dichiaro la variabile prendendo il tipo dal typeConverter
        sb.append(typeC)
                .append(" ")
                .append(elObbl.getId().getNomeId())
                .append(" = ")
                .append(elObbl.getConstant().accept(this))
                .append(";\n");
    }

    private String formatOut(int type, String mode){
        if(mode.equals("printf")){
            return switch (type) {
                case sym.INTEGER -> "\"%d\\n\"";
                case sym.REAL -> "\"%f\\n\"";
                case sym.BOOL, sym.STRING -> "\"%s\\n\"";
                case sym.CHAR -> "\"%c\\n\"";
                default -> "ERROR";
            };
        } else {
            return switch (type) {
                case sym.INTEGER -> "\"%d\"";
                case sym.REAL -> "\"%f\"";
                case sym.BOOL, sym.STRING -> "\"%s\"";
                case sym.CHAR -> "\"%c\"";
                default -> "ERROR";
            };
        }

    }

    private String includeLibraries(){
        return "#include <stdio.h>\n" +
                "#include <stdlib.h>\n" +
                "#include <string.h>\n" +
                "#include <math.h>\n";
                //"#include <malloc.h>\n";
    }

    private String converterFunctions(){
        return """
                char* intToString(int var){
                    char* int_str = malloc(256);
                    sprintf(int_str, "%d", var);
                    return int_str;
                }
            
                char* doubleToString(double var){
                    char* double_str = malloc(256);
                    sprintf(double_str, "%f", var);
                    return double_str;
                }
            
                char* boolToString(int var){
                    if (var == 1){
                        return "true";
                    }
                    if (var == 0){
                        return "false";
                    }
                    return "";
                }
            
                char* concat(char* s1, char* i) {
                    char* s = malloc(256);
                    sprintf(s, "%s%s", s1, i);
                    return s;
                }
                """;
    }

    public SymbolRecord lookup(String item){
        SymbolRecord found = null;

        for (SymbolTable current : stackScope){
            found = current.get(item);
            if(found != null)
                break;
        }
        return found;
    }

    private String checkUniVarExpr(String modeExpr) {
        return switch (modeExpr) {
            case "MINUS" -> "-";
            case "NOT" -> " !" ;
            default -> "ERROR";
        };
    }

    private String checkBiVarExpr(ExprNode expr1, ExprNode expr2, String opExpr) {
        switch(opExpr){
            case "EQUALS", "NE" -> {
                if((expr1.getAstType() == sym.STRING || expr1.getAstType() == sym.CHAR)
                        && (expr2.getAstType() == sym.STRING || expr2.getAstType() == sym.CHAR)){
                    if(opExpr.equals("EQUALS")){
                        return "!strcmp(" + expr1.accept(this)+ "," + expr2.accept(this) + ")";
                    }
                    else{
                        return "strcmp(" + expr1.accept(this)+ "," + expr2.accept(this) + ")";
                    }
                } else {
                    if(opExpr.equals("EQUALS")){
                        return expr1.accept(this) + " == " + expr2.accept(this);
                    } else {
                        return expr1.accept(this) + " != " + expr2.accept(this);
                    }
                }
            }
            case "STR_CONCAT" -> {
                return "concat(" + strconcatService(expr1) + "," + strconcatService(expr2) + ")";
            }
            case "POW" -> {
                return "pow((float)(" + expr1.accept(this) + "), (float)(" + expr2.accept(this) + "))";
            }
            case "PLUS" -> {
                return expr1.accept(this) + " + " + expr2.accept(this);
            }
            case "MINUS" -> {
                return expr1.accept(this) + " - " + expr2.accept(this);
            }
            case "TIMES" -> {
                return expr1.accept(this) + " * " + expr2.accept(this);
            }
            case "DIV" -> {
                return expr1.accept(this) + " / " + expr2.accept(this);
            }
            case "GT" -> {
                return expr1.accept(this) + " > " + expr2.accept(this);
            }
            case "GE" -> {
                return expr1.accept(this) + " >= " + expr2.accept(this);
            }
            case "LT" -> {
                return expr1.accept(this) + " < " + expr2.accept(this);
            }
            case "LE" -> {
                return expr1.accept(this) + " <= " + expr2.accept(this);
            }
            case "AND" -> {
                return expr1.accept(this) + " && " + expr2.accept(this);
            }
            case "OR" -> {
                return expr1.accept(this) + " || " + expr2.accept(this);
            }
            default -> {
                return "ERROR";
            }
        }

    }

    private String strconcatService(ExprNode exprNode){
        return switch (numericToString(exprNode.getAstType())) {
            case "INTEGER" -> "intToString(" + exprNode.accept(this) + ")";
            case "REAL" -> "doubleToString(" + exprNode.accept(this) + ")";
            case "BOOL" -> "boolToString(" + exprNode.accept(this) + ")";
            default -> exprNode.accept(this);
        };
    }

    private static void mainDefaultValueGen(StringBuilder sb, ParDeclNode parDeclNode) {
        switch (parDeclNode.getAstType()) {
            case sym.INTEGER -> sb.append('0');
            case sym.REAL -> sb.append("0.0");
            case sym.STRING -> sb.append("\"\"");
            case sym.CHAR -> sb.append("''");
            case sym.BOOL -> sb.append("0");

            default -> throw new UnsupportedOperationException("Tipo non supportato: " + parDeclNode.getAstType());
        }
    }

    private void generateFunction(StringBuilder sb, FunDeclNode funDeclNode) {
        codeGeneratorC += typeConverter(funDeclNode.getTypeOrVoid()) + " " + funDeclNode.getId().getNomeId() + "(";

        ArrayList<ParDeclNode> parDeclNodeList = funDeclNode.getParDeclList();
        String code = codeGeneratorC;
        sb.setLength(0);

        //Generazione dei parametri
        if (parDeclNodeList != null) {
            for (ParDeclNode parElement : parDeclNodeList) {
                sb.append(parElement.accept(this));
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        codeGeneratorC = code + sb + "){\n";
        codeGeneratorC += funDeclNode.getBody().accept(this);
        codeGeneratorC += "}\n";
    }

    private static void paramGeneration(StringBuilder sb, FunDeclNode funDeclNode) {
        if(funDeclNode.getParDeclList() != null){
            for (ParDeclNode parDeclNode : funDeclNode.getParDeclList()) {
                int sizeParInput = parDeclNode.getIdList().size();

                for (int i = 0; i < sizeParInput; i++) {
                    mainDefaultValueGen(sb, parDeclNode);

                    if (i < sizeParInput - 1) {
                        sb.append(',');
                    }
                }
            }
        }
    }

}
