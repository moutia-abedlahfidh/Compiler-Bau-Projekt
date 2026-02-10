package zwischencode;

public enum InstructionType {
    LOAD_CONST,   // Zahl laden
    LOAD_VAR,     // Variable laden
    STORE_VAR,    // Variable speichern
    ADD,          // +
    SUB,          // -
    MUL,          // *
    DIV,          // /
    NEG,          // for unary minus
    PRINT,        // Ausgabe
    POP           // Stack-Top verwerfen (z.B. Ausdruck-Statements)
    , JUMP_IF_FALSE
    , JUMP
    // comparison helpers 
    , LT, GT, LE, GE, EQ, NEQ
    // function / call helpers
    , FUNCTION_DEF, FUNCTION_END, CALL, RET
}
