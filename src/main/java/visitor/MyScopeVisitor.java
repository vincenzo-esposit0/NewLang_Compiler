package visitor;

import exceptions.AlreadyDeclaredVariableException;
import nodes.*;
import esercitazione5.*;
import table.ParInitialize;
import table.SymbolRecord;
import table.SymbolTable;

import java.util.ArrayList;
import java.util.Stack;

public class MyScopeVisitor implements MyVisitor{

    private Stack<SymbolTable> stackScope;

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
        SymbolTable symbolTable = new SymbolTable("Global");
        stackScope.push(symbolTable);
        node.setSymbolTable(symbolTable);

        ArrayList<VarDeclNode> varDeclListNode = node.getVarDeclList();
        visitNodeList(varDeclListNode);

        ArrayList<FunDeclNode> funDeclListNode = node.getFunDeclList();
        visitNodeList(funDeclListNode);

        stackScope.pop();

        node.setAstType(sym.VOID);
    }

    private void visitBodyNode(BodyNode node) {
        ArrayList<VarDeclNode> varDeclListNode = node.getVarDeclList();
        ArrayList<StatNode> statList = node.getStatList();

        visitNodeList(varDeclListNode);
        visitNodeList(statList);

        node.setAstType(sym.VOID);
    }

    private void visitVarDeclNode(VarDeclNode node) {
        //Se la variabile è VAR assegno la lista IdInitObblList altrimenti assegno IdInitList
        ArrayList<IdInitNode> idInitList = node.isVar() ? node.getIdInitObblList() : node.getIdInitList();

        if (idInitList != null) {
            Integer declType = null;

            for (IdInitNode idElement : idInitList) {
                String nomeID = idElement.getId().getNomeId();

                //Se la variabile è VAR il tipo si inferisce dalla costante di QUESTO elemento
                //(es. var a << 1, b << 2.2: a è integer, b è real), altrimenti si usa il tipo dichiarato
                int typeCheck = node.isVar() ? MyTypeChecker.getInferenceType(idElement.getConstant().getModeExpr()) :
                        MyTypeChecker.getInferenceType(node.getType());

                if (stackScope.peek().containsKey(nomeID)) {
                    throw new AlreadyDeclaredVariableException("L'identificativo è gia presente all'interno dello scope: " + nomeID);
                }

                stackScope.peek().put(nomeID, new SymbolRecord(nomeID, "var", typeCheck));
                //Il traduttore C legge il tipo dal nodo dell'identificativo
                idElement.getId().setAstType(typeCheck);
                if (declType == null) declType = typeCheck;
            }

            node.setAstType(declType);
        }
    }

    private void visitFunDeclNode(FunDeclNode node) {
        FunDeclNode funDeclNode = node.isMain() ? node.getFunDecl() : node;

        int returnTypeCheck = MyTypeChecker.getInferenceType(funDeclNode.getTypeOrVoid());
        String nomeID = funDeclNode.getId() != null ? funDeclNode.getId().getNomeId() : "";
        SymbolTable symbolTableGlobal = stackScope.peek();

        if (stackScope.peek().containsKey(nomeID)) {
            throw new AlreadyDeclaredVariableException("La funzione " + nomeID + " ha un identificativo già dichiarato all'interno dello scope.");
        }

        SymbolTable symbolTable = new SymbolTable("FUN", nomeID);
        stackScope.push(symbolTable);

        ParInitialize parInitialize = processParameterDeclarations(funDeclNode);

        // Aggiunge la funzione alla tabella dei simboli globale
        symbolTableGlobal.put(nomeID, new SymbolRecord(nomeID, "FUN", parInitialize, returnTypeCheck));

        funDeclNode.getBody().accept(this);

        funDeclNode.setAstType(returnTypeCheck);
        node.setAstType(returnTypeCheck);
        funDeclNode.setSymbolTable(stackScope.peek());

        stackScope.pop();
    }

    private void visitParDeclNode(ParDeclNode node) {
        int typeCheck = MyTypeChecker.getInferenceType(node.getTypeVar());

        for (IdInitNode idElement : node.getIdList()) {
            String nomeID = idElement.getId().getNomeId();

            if (stackScope.peek().containsKey(nomeID)) {
                throw new AlreadyDeclaredVariableException("L'identificativo è già presente all'interno dello scope: " + nomeID);
            }

            stackScope.peek().put(nomeID, new SymbolRecord(nomeID, "var", typeCheck, node.getOut()));
            idElement.setAstType(sym.VOID);
        }

        node.setAstType(typeCheck);
    }

    private void visitIfStatNode(IfStatNode node) {
        SymbolTable symbolTable;
        String lastScopeFunName = stackScope.peek().getFunctionName();

        symbolTable = new SymbolTable("IF", lastScopeFunName);
        stackScope.push(symbolTable);
        node.setSymbolTable(symbolTable);

        node.getBody().accept(this);

        stackScope.pop();

        if(node.getElseStat() != null){
            node.getElseStat().accept(this);
        }

        node.setAstType(sym.VOID);
    }

    private void visitElseNode(ElseNode node) {
        SymbolTable symbolTable;
        String lastScopeFunName = stackScope.peek().getFunctionName();

        symbolTable = new SymbolTable("ELSE", lastScopeFunName);
        stackScope.push(symbolTable);
        node.setSymbolTable(symbolTable);

        node.getBody().accept(this);

        node.setAstType(sym.VOID);

        stackScope.pop();
    }

    private void visitForStatNode(ForStatNode node) {
        SymbolTable symbolTable;
        String lastScopeFunName = stackScope.peek().getFunctionName();

        symbolTable = new SymbolTable("FOR", lastScopeFunName);
        stackScope.push(symbolTable);

        String loopVar = node.getId().getNomeId();
        int typeCheck = sym.INTEGER;
        stackScope.peek().put(loopVar, new SymbolRecord(loopVar, "var", typeCheck));

        node.setSymbolTable(symbolTable);

        node.getBody().accept(this);

        node.setAstType(sym.VOID);

        stackScope.pop();
    }

    private void visitWhileStatNode(WhileStatNode node) {
        SymbolTable symbolTable;
        String lastScopeFunName = stackScope.peek().getFunctionName();

        symbolTable = new SymbolTable("WHILE", lastScopeFunName);
        stackScope.push(symbolTable);
        node.setSymbolTable(symbolTable);

        node.getBody().accept(this);

        node.setAstType(sym.VOID);

        stackScope.pop();
    }

    private void visitNodeList(ArrayList<? extends ASTNode> nodeList) {
        if (nodeList != null) {
            for (int i = 0; i < nodeList.size(); i++){
                nodeList.get(i).accept(this);
            }
        }
    }

    private ParInitialize processParameterDeclarations(FunDeclNode funDeclNode) {
        ArrayList<Integer> parTypesList = new ArrayList<>();
        ArrayList<Boolean> parOutList = new ArrayList<>();
        ParInitialize parInitialize = new ParInitialize(parTypesList, parOutList);

        ArrayList<ParDeclNode> parDeclList = funDeclNode.getParDeclList();
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

        return parInitialize;
    }

}
