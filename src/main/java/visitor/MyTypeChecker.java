package visitor;

import esercitazione5.sym;

public class MyTypeChecker {

    public static int getInferenceType(String type) {
        return switch (type) {
            case "INTEGER", "INTEGER_CONST", "ConstInteger" -> sym.INTEGER;
            case "REAL", "REAL_CONST", "ConstReal" -> sym.REAL;
            case "TRUE", "FALSE", "ConstTrue", "ConstFalse", "BOOL", "BOOLEAN" -> sym.BOOL;
            case "STRING", "STRING_CONST", "ConstString" -> sym.STRING;
            case "CHAR", "CHAR_CONST", "ConstChar" -> sym.CHAR;
            case "VOID" -> sym.VOID;
            default -> sym.error;
        };
    }

    public static int unaryOperations(String operation, int operator) {
        return switch (operation) {
            case "MINUS" -> (operator == sym.INTEGER) ? sym.INTEGER :
                    (operator == sym.REAL) ? sym.REAL :
                            sym.error;
            case "NOT" -> (operator == sym.BOOL) ? sym.BOOL : sym.error;
            default -> sym.error;
        };
    }

    public static int binaryOperations(String operation, int operator1, int operator2) {
        switch (operation){
            case "PLUS", "MINUS", "TIMES", "DIV", "POW" -> {
                if (operator1 == sym.INTEGER && operator2 == sym.INTEGER) {
                    return sym.INTEGER;
                } else if (operator1 == sym.INTEGER && operator2 == sym.REAL) {
                    return sym.REAL;
                } else if (operator1 == sym.REAL && operator2 == sym.REAL) {
                    return sym.REAL;
                } else if (operator1 == sym.REAL && operator2 == sym.INTEGER) {
                    return sym.REAL;
                }
                return sym.error;
            }
            case "STR_CONCAT" -> {
                if (operator1 != sym.VOID && operator2 != sym.VOID) {
                    return sym.STRING;
                }
                return sym.error;
            }
            case "AND", "OR" -> {
                if (operator1 == sym.BOOL && operator2 == sym.BOOL) {
                    return sym.BOOL;
                }
                return sym.error;
            }
            case "EQUALS", "NE", "LT", "LE", "GT", "GE" -> {
                if((operator1 == sym.INTEGER || operator1 == sym.REAL) && (operator2 == sym.INTEGER || operator2 == sym.REAL)) {
                    return sym.BOOL;
                } else if ((operator1 == sym.STRING || operator1 == sym.CHAR) && (operator2 == sym.STRING || operator2 == sym.CHAR)) {
                    return sym.BOOL;
                }
                return sym.error;
            }
            default -> {
                return sym.error;
            }
        }
    }

    public static int AssignOperations(int operator1, int operator2) {
        if ((operator1 == sym.INTEGER && operator2 == sym.INTEGER) ||
                (operator1 == sym.REAL && operator2 == sym.REAL) ||
                (operator1 == sym.INTEGER && operator2 == sym.REAL) ||
                (operator1 == sym.REAL && operator2 == sym.INTEGER) ||
                (operator1 == sym.BOOL && operator2 == sym.BOOL) ||
                (operator1 == sym.INTEGER && operator2 == sym.BOOL) ||
                (operator1 == sym.BOOL && operator2 == sym.INTEGER) ||
                (operator1 == sym.STRING && operator2 == sym.STRING) ||
                (operator1 == sym.CHAR && operator2 == sym.CHAR)) {
            return sym.VOID;
        }
        return sym.error;
    }

    public static boolean returnChecker(int operator1, int operator2) {
        return (operator1 == operator2) ||
                (operator1 == sym.INTEGER && operator2 == sym.REAL) ||
                (operator1 == sym.REAL && operator2 == sym.INTEGER) ||
                (operator1 == sym.CHAR && operator2 == sym.STRING) ||
                (operator1 == sym.STRING && operator2 == sym.CHAR);
    }



}
