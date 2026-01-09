**Projekt-Änderungen**

- **Datum:** 2025-11-27
- **Kurzfassung:** Erweiterung der kleinen Sprache um Kontrollfluss (if/else/while/for), Block-Syntax, Kommentar-Support (`#` und `//`), Funktionen (definition, call, return, Rekursion).

Wesentliche Änderungen (Dateien)

- `lexer/Tokenizer.java`: `#` als Zeilenkommentar hinzugefügt (wird bis Zeilenende übersprungen). Außerdem bereits vorhandene Kommentartypen (`//`, `/* ... */`) bleiben aktiv.
- `lexer/TokenType.java`: neue Token-Typen: `IF`, `ELSE`, `WHILE`, `FOR`, `FUNCTION`, `RETURN`, `LBRACE`, `RBRACE`, `COMMA` (Keywords und Satzzeichen als eigene Typen).
- `parser/Parser.java`: Erweiterungen für Statements und Block-Syntax `{ ... }`; Parsing von `if/else`, `while`, `for`, Funktionsdeklarationen `function name(params) { body }`, `return`, und Aufrufe mit Argumentlisten (Kommas korrekt geparst).
- `ast/`: Neue / erweiterte Knoten:
  - `IfNode`, `WhileNode`, `ForNode` — Kontrollflussknoten
  - `FunctionNode` — Funktionsdeklaration (registriert sich in `FunctionRegistry` beim `eval`)
  - `CallNode` — Funktionsaufruf (evaluierte Argumente, lokales Environment)
  - `ReturnNode`, `ReturnException` — Implementierung von `return` mittels Ausnahme-Wicklung
  - `FunctionRegistry` — einfaches globales Mapping `name -> FunctionNode`
- `zwischencode/InstructionType.java`: neue Instruktionstypen: `FUNCTION_DEF`, `FUNCTION_END`, `CALL`, `RET` sowie bereits vorhandene Sprung- und Vergleichs-Operatoren (`JUMP`, `JUMP_IF_FALSE`, `LT`, `GT`, `LE`, `GE`, `EQ`, `NEQ`).
- `zwischencode/CodeGenerator.java`: Code-Emission erweitert, um Funktionsdefinitionen, Aufrufe und `return`-Instruktionen auszugeben; bestehende Unterstützung für arithmetische und Vergleichsoperatoren sowie Sprünge bleibt erhalten.
- `Main.java`: liest `src/main/java/input.txt`, ignoriert Kommentar-Zeilen (`#` und `//`) ohne Ausgabe; druckt AST, Ausführungsergebnis (via `ast.eval`) und den generierten Zwischencode zur Inspektion.


**Verifikation / Testergebnisse**

- Manuelle Ausführung (`Main`) über `src/main/java/input.txt`

**Design-/Implementationshinweise**

- Die Zwischencode-Ausgabe dient derzeit nur zur Inspektion/Debug; es existiert noch kein Interpreter/VM, der diese Instruktionen ausführt. Die tatsächliche Programmausführung erfolgt weiterhin über `AST.eval`.
- Scoping ist einfach gehalten: Funktionsnamen werden in einer globalen `FunctionRegistry` registriert; Parameter werden für einzelne Aufruf-Frames in einem neuen `Map<String,Double>` gebunden. Es gibt noch keine geschachtelten Funktionen oder Closures.

