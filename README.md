# Projekt: Kleiner Ausdrucks-Interpreter

Kurze Anleitung, wie du die Java-Quellen kompilierst und das Programm ausführst. Die Beispiele unten sind für PowerShell (Windows).

##  Klassen in `out` schreiben
Das erzeugt alle `.class`-Dateien unter `out` und hält den Quellbaum sauber.

1) Kompilieren in das `out`-Verzeichnis:

```powershell
# Alle .java Dateien sammeln und direkt an javac übergeben
$files = Get-ChildItem -Path src\main\java -Recurse -Filter *.java | ForEach-Object { $_.FullName }

javac -d out $files

```

3) Programm starten:

```powershell
java -cp out Main
```
