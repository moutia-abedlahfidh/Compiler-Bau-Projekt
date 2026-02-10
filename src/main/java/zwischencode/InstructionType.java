package zwischencode;

public enum InstructionType {
    LOAD_CONST,   // Zahl laden
    LOAD_VAR,     // Variable laden
    STORE_VAR,    // Variable speichern
    ADD,          // +
    SUB,          // -
    MUL,          // *
    DIV,          // /
    NEG,          // fuer unaires Minus
    PRINT,        // Ausgabe
    POP           // Stack-Top verwerfen (z.B. Ausdruck-Statements)
    , JUMP_IF_FALSE
    , JUMP
    // Vergleichs-Operatoren
    , LT, GT, LE, GE, EQ, NEQ
    // Funktions- und Aufruf-Operatoren
    , FUNCTION_DEF, FUNCTION_END, CALL, RET
}
