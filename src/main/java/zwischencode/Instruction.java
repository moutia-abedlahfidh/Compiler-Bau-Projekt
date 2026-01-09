package zwischencode;

public class Instruction {
    InstructionType type;
    Object operand; 

    public Instruction(InstructionType type, Object name) {
        this.type = type;
        this.operand = name;
    }

    @Override
    public String toString() {
        return type + (operand != null ? " " + operand : "");
    }
}
