package visitor;

import esercitazione5.sym;
import exceptions.AlreadyDeclaredVariableException;
import nodes.*;
import table.ParInitialize;
import table.SymbolRecord;
import table.SymbolTable;

import java.util.ArrayList;
import java.util.Stack;

public class MyScopeVisitor implements MyVisitor{

    private Stack<SymbolTable> stackScope;

    private SymbolTable symbolTable;

    public MyScopeVisitor() {
        this.stackScope = new Stack<SymbolTable>();
    }

    @Override
    public String visit(ASTNode node) {
        switch (node.getClass().getSimpleName()) {
            case "ProgramNode" -> visitProgramNode((ProgramNode) node);
            case "BodyNode" -> visitBodyNode((BodyNode) node);
            case "VarDeclNode" -> visitVarDeclNode((VarDeclNode) node);
            case "FunDeclNode" -> visitFunDeclNode((FunDeclNode) node);
            case "ParDeclNode" -> visitParDeclNode((ParDeclNode) node);
            case "IfStatNode" -> visitIfStatNode((IfStatNode) node);
            case "ElseNode" -> visitElseNode((ElseNode) node);
            case "ForStatNode" -> visitForStatNode((ForStatNode) node);
            case "WhileStatNode" -> visitWhileStatNode((WhileStatNode) node);
        }

        return null;
    }

    private void visitProgramNode(ProgramNode node) {
        symbolTable = new SymbolTable("Global");
        stackScope.push(symbolTable);
        node.setSymbolTable(symbolTable);


        ArrayList<VarDeclNode> varDeclListNode = node.getVarDeclList();
        visitNodeList(varDeclListNode);

        ArrayList<FunDeclNode> funDeclListNode = node.getFunDeclList();
        visitNodeList(funDeclListNode);

        System.out.println(node.getSymbolTable().toString());

        stackScope.pop();

        node.setAstType(sym.VOID);
    }

    private void visitNodeList(ArrayList<? extends ASTNode> nodeList) {
        if (nodeList != null) {
            for (int i = 0; i < nodeList.size(); i++){
                nodeList.get(i).accept(this);
            }
        }
    }

    private void visitBodyNode(BodyNode node) {
        ArrayList<VarDeclNode> varDeclListNode = node.getVarDeclList();
        for (VarDeclNode varDecl : varDeclListNode) {
            if (varDecl != null) {
                varDecl.accept(this);
            }
        }

        ArrayList<StatNode> statList = node.getStatList();
        for (StatNode stat : statList) {
            if (stat != null) {
                stat.accept(this);
            }
        }

        node.setAstType(sym.VOID);
    }

    private void visitVarDeclNode(VarDeclNode node) {
        ArrayList<IdInitNode> idInitList = node.getIdInitList();
        ArrayList<IdInitNode> idInitObblList = node.getIdInitObblList();

        if(node.isVar() && idInitObblList != null) {

            for (IdInitNode idElement : idInitObblList) {
                String nomeID = idElement.getId().getNomeId();
                System.out.println("MyScopeVisitor: inside visitVarDeclNode --- FOR " + nomeID);

                if (!stackScope.peek().containsKey(nomeID)) {
                    int typeCheck = MyTypeChecker.getInferenceType(idElement.getConstant().getModeExpr());

                    stackScope.peek().put(nomeID, new SymbolRecord(nomeID, "var", typeCheck));

                    node.setAstType(typeCheck);
                } else {
                    System.out.println("MyScopeVisitor: inside visitVarDeclNode --- FOR->ELSE is error");

                    node.setAstType(sym.error);
                    throw new AlreadyDeclaredVariableException("Identifier is already declared within the scope: " + nomeID);
                }
            }
        } else if (!node.isVar() && idInitList != null){
            int typeCheck = MyTypeChecker.getInferenceType(node.getType());

            for (IdInitNode idElement : idInitList) {
                String nomeID = idElement.getId().getNomeId();

                if (!stackScope.peek().containsKey(nomeID)) {
                    stackScope.peek().put(nomeID, new SymbolRecord(nomeID, "var", typeCheck));
                }
                else {
                    throw new AlreadyDeclaredVariableException("Identifier is already declared within the scope: " + nomeID);
                }
            }
            node.setAstType(typeCheck);
        }
    }

    private void visitFunDeclNode(FunDeclNode node) {
        FunDeclNode funDeclNode;

        int returnTypeCheck = 0;

        if(node.isMain()){
            funDeclNode = node.getFunDecl();
            returnTypeCheck = MyTypeChecker.getInferenceType("VOID");
        } else {
            funDeclNode = node;
            returnTypeCheck = MyTypeChecker.getInferenceType(funDeclNode.getTypeOrVoid());
        }

        String nomeID = "";

        if(funDeclNode.getId() != null){
            nomeID = funDeclNode.getId().getNomeId();
        }

        System.out.println("MyScopeVisitor: inside visitFunDeclNode check variable funDeclNode " + funDeclNode.getTypeOrVoid() + " " + funDeclNode.getName());

        SymbolTable symbolTableGlobal = stackScope.peek();

        if(!stackScope.peek().containsKey(nomeID)){
            symbolTable = new SymbolTable("FUN", nomeID);
            stackScope.push(symbolTable);

            ArrayList<ParDeclNode> parDeclList = node.getParDeclList();

            ArrayList<Integer> parTypesList = new ArrayList<>();
            ArrayList<Boolean> parOutList = new ArrayList<>();

            ParInitialize parInitialize = new ParInitialize(parTypesList, parOutList);

            if (parDeclList != null) {
                for (ParDeclNode parElement : parDeclList) {
                    parElement.accept(this);
                    for (int i = 0; i < parElement.getIdList().size(); i++) {
                        int parTypeCheck = MyTypeChecker.getInferenceType(parElement.getTypeVar());

                        parInitialize.addParamsTypeList(parTypeCheck);
                        parInitialize.addParamsOutList(parElement.getOut());
                    }
                }
            }

            symbolTableGlobal.put(nomeID, new SymbolRecord(nomeID, "FUN", parInitialize, returnTypeCheck));

            funDeclNode.getBody().accept(this);

        } else {
            throw new AlreadyDeclaredVariableException("Identifier of function is already declared within the scope: " + nomeID);
        }

        funDeclNode.setAstType(returnTypeCheck);
        node.setAstType(returnTypeCheck);
        funDeclNode.setSymbolTable(stackScope.peek());
        System.out.println("MyScopeVisitor end of visitFunDeclNode" + funDeclNode.getSymbolTable().toString());
        stackScope.pop();
    }


    private void visitParDeclNode(ParDeclNode node) {
        ArrayList<IdInitNode> idInitNodeList = node.getIdList();
        int typeCheck = MyTypeChecker.getInferenceType(node.getTypeVar());

        for(IdInitNode idElement: idInitNodeList){
            String nomeID = idElement.getId().getNomeId();

            if (!stackScope.peek().containsKey(nomeID)) {
                if(node.getOut().equals(Boolean.TRUE)) {
                    stackScope.peek().put(nomeID, new SymbolRecord(nomeID, "var", typeCheck, true));
                } else {
                    stackScope.peek().put(nomeID, new SymbolRecord(nomeID, "var", typeCheck));
                }
            }
            else {
                throw new AlreadyDeclaredVariableException("Identifier is already declared within the scope: " + nomeID);
            }

            idElement.setAstType(sym.VOID);
        }

        node.setAstType(typeCheck);
    }


    private void visitIfStatNode(IfStatNode node) {
        symbolTable = new SymbolTable("IF");
        stackScope.push(symbolTable);
        node.setSymbolTable(symbolTable);

        System.out.println(node.getSymbolTable().toString());

        node.getBody().accept(this);

        stackScope.pop();

        if(node.getElseSymbolTable() != null){
            symbolTable = new SymbolTable("ELSE");
            stackScope.push(symbolTable);
            node.setSymbolTable(symbolTable);

            System.out.println(node.getSymbolTable().toString());

            node.getBody().accept(this);
            stackScope.pop();
        }

        node.setAstType(sym.VOID);
    }

    private void visitElseNode(ElseNode node) {
        symbolTable = new SymbolTable("ELSE");
        stackScope.push(symbolTable);
        node.setSymbolTable(symbolTable);

        System.out.println(node.getSymbolTable().toString());

        node.getBody().accept(this);

        node.setAstType(sym.VOID);

        stackScope.pop();
    }

    private void visitForStatNode(ForStatNode node) {
        symbolTable = new SymbolTable("FOR");
        stackScope.push(symbolTable);

        String i = node.getId().getNomeId();
        int typeCheck = sym.INTEGER;
        stackScope.peek().put(i, new SymbolRecord(i, "var", typeCheck));

        node.setSymbolTable(symbolTable);

        System.out.println(node.getSymbolTable().toString());

        node.getBody().accept(this);

        node.setAstType(sym.VOID);

        stackScope.pop();
    }

    private void visitWhileStatNode(WhileStatNode node) {
        symbolTable = new SymbolTable("WHILE");
        stackScope.push(symbolTable);
        node.setSymbolTable(symbolTable);

        System.out.println(node.getSymbolTable().toString());

        node.getBody().accept(this);

        node.setAstType(sym.VOID);

        stackScope.pop();
    }

}
