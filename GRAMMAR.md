# Kontextfreie Grammatik (EBNF)

```
program     = { statement [ ";" ] } EOF ;

statement   = block
            | ifStmt
            | whileStmt
            | forStmt
            | functionDef
            | returnStmt
            | assignment
            | comparison
            ;

block       = "{" { statement [ ";" ] } "}" ;

ifStmt      = "if" "(" comparison ")" statement [ "else" statement ] ;
whileStmt   = "while" "(" comparison ")" statement ;
forStmt     = "for" "(" [ startStmt ] ";" [ comparison ] ";" [ startStmt ] ")" statement ;

functionDef = "function" IDENT "(" [ identList ] ")" statement ;
returnStmt  = "return" [ comparison ] ;

assignment = IDENT "=" comparison ;
startStmt  = ifStmt
          | whileStmt
          | forStmt
          | functionDef
          | returnStmt
          | assignment
          | comparison
          ;

comparison = expr { ("==" | "!=" | "<" | ">" | "<=" | ">=") expr } ;
expr       = term { ("+" | "-") term } ;
term       = factor { ("*" | "/") factor } ;
factor     = ("+" | "-") factor
          | NUMBER
          | IDENT [ "(" [ argList ] ")" ]
          | "(" comparison ")"
          ;

identList  = IDENT { "," IDENT } ;
argList    = comparison { "," comparison } ;
```

## Lexikalische Regeln (Tokenizer)

```
IDENT   = (letter | "_") { letter | digit | "_" } ;
NUMBER  = ( digit { digit } [ "." { digit } ] | "." digit { digit } )
          [ ("e" | "E") [ "+" | "-" ] digit { digit } ] ;

Keywords = "if" | "else" | "while" | "for" | "function" | "return" ;
Operators = "+" | "-" | "*" | "/" | "==" | "!=" | "<" | ">" | "<=" | ">=" | "=" ;
Delims    = "(" | ")" | "{" | "}" | ";" | "," ;

LineComment  = "#" { notNewline } | "//" { notNewline } ;
BlockComment = "/*" { anyCharExcept("*/") } "*/" ;
Whitespace   = space | tab | newline ;
```

Hinweise:
- Kommentare und Whitespace werden vom Tokenizer ignoriert.
- Semikolons sind Trennzeichen zwischen Statements; mehrere `;` sind erlaubt.

## Beispielabdeckung (input.txt)

- Ausdruecke: arithmetische Ausdruecke, Klammern, Unary +/-, Vergleiche.
- Statements: Zuweisung, if/else, while, for, Blocks, return, Funktionsdefinitionen.
- Funktionen: Aufrufe (auch geschachtelt), Rekursion, Arity-Fehler, leeres return.
- Fehlerfaelle: Parse-Fehler, Type-Fehler, Runtime-Fehler (z.B. Division durch 0).

## Semantik (kurz)

- Arithmetik arbeitet auf `number`.
- Vergleiche liefern `bool`, ausgefuehrt als `1.0` (true) und `0.0` (false).
- Bedingungen in `if/while/for` muessen `bool` sein.
- `return` beendet die aktuelle Funktion mit dem ausgewerteten Ausdruck.
