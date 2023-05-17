package nodes;

import visitor.MyVisitor;

public class IdNode {

    public String value;

    public IdNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void accept(MyVisitor visitor) {
        visitor.visit(this);
    }
}
