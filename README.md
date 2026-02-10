# Projekt: Kleiner Ausdrucks-Interpreter

Kurze Anleitung, wie du die Java-Quellen kompilierst und das Programm ausführst. Die Beispiele unten sind für PowerShell (Windows).

## Voraussetzungen

- Java (JDK) installiert
- PowerShell (Windows)

## Kompilieren
### Klassen in `out` schreiben
Das erzeugt alle `.class`-Dateien unter `out` und hält den Quellbaum sauber.

1) Kompilieren in das `out`-Verzeichnis:

```powershell
# Alle .java Dateien sammeln und direkt an javac übergeben
$files = Get-ChildItem -Path src\main\java -Recurse -Filter *.java | ForEach-Object { $_.FullName }

javac -d out $files

```

2) Programm starten:

```powershell
java -cp out Main
```



## Architektur (Zusammenspiel)

1) **Tokenizer** erzeugt eine Token-Liste mit Positionen (Zeile/Spalte).
2) **Parser** liest die Tokens und baut daraus den AST (rekursiver Abstieg).
3) **Type-Checker** laeuft ueber den AST und prueft Typregeln (number/bool).
4) **CodeGenerator** traversiert den AST und erzeugt Stack-basierten Zwischencode.
5) **VM** interpretiert den Zwischencode und liefert das Ergebnis.

Der Ablauf ist strikt linear: Tokenizer → Parser → Type-Checker → CodeGenerator → VM.


## Wichtige Dateien

- Parser: `src/main/java/parser/Parser.java`
- Lexer: `src/main/java/lexer/Tokenizer.java`
- AST: `src/main/java/ast/*`
- Typecheck: `src/main/java/typecheck/*`
- Zwischencode: `src/main/java/zwischencode/*`
- Beispiele: `src/main/java/input.txt`
- Grammatik: `GRAMMAR.md`
