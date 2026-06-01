#!/usr/bin/env bash
# Compila y ejecuta el motor del juego o la suite de tests.
# Uso:
#   ./compile.sh          — compila y lanza el juego (TUI con JLine3)
#   ./compile.sh tui      — igual que el anterior
#   ./compile.sh classic  — compila y lanza la versión clásica por consola
#   ./compile.sh test     — compila y ejecuta los tests
#   ./compile.sh build    — empaqueta el fat JAR con Maven (requiere Maven)

MAVEN="/c/Users/322173/Downloads/maven/apache-maven-3.9.16/bin/mvn"
JAR="target/dungeon-crawler-2d-1.2.0.jar"

SRC_MAIN="src/main/java"
SRC_TEST="src/test/java"
BIN_DIR="bin"
MAIN_CLASSIC="com.dungeoncrawler.Main"
MAIN_TUI="com.dungeoncrawler.MainTUI"
TEST_CLASS="com.dungeoncrawler.TestRunner"

echo "=== DUNGEON CRAWLER 2D ==="

case "$1" in
    build)
        echo "Empaquetando fat JAR con Maven..."
        "$MAVEN" package -q
        if [ $? -ne 0 ]; then echo "Error en el build Maven."; exit 1; fi
        echo "JAR generado: $JAR"
        ;;

    classic)
        echo "Compilando (clásico)..."
        mkdir -p "$BIN_DIR"
        javac -d "$BIN_DIR" \
            "$SRC_MAIN"/com/dungeoncrawler/core/*.java \
            "$SRC_MAIN"/com/dungeoncrawler/Main.java \
            "$SRC_TEST"/com/dungeoncrawler/TestRunner.java
        if [ $? -ne 0 ]; then echo "Error en la compilación."; exit 1; fi
        echo "Compilación exitosa. Iniciando juego clásico..."
        java -cp "$BIN_DIR" "$MAIN_CLASSIC"
        ;;

    test)
        echo "Compilando (tests)..."
        mkdir -p "$BIN_DIR"
        javac -d "$BIN_DIR" \
            "$SRC_MAIN"/com/dungeoncrawler/core/*.java \
            "$SRC_MAIN"/com/dungeoncrawler/Main.java \
            "$SRC_TEST"/com/dungeoncrawler/TestRunner.java
        if [ $? -ne 0 ]; then echo "Error en la compilación."; exit 1; fi
        echo "Ejecutando tests..."
        java -cp "$BIN_DIR" "$TEST_CLASS"
        ;;

    tui|"")
        # Build JAR if it doesn't exist yet
        if [ ! -f "$JAR" ]; then
            echo "JAR no encontrado, compilando con Maven..."
            "$MAVEN" package -q
            if [ $? -ne 0 ]; then echo "Error en el build Maven."; exit 1; fi
        fi
        echo "Iniciando TUI (JLine3)..."
        java --enable-native-access=ALL-UNNAMED \
             -Dorg.jline.terminal.disableDeprecatedProviderWarning=true \
             -jar "$JAR"
        ;;

    *)
        echo "Uso: ./compile.sh [tui|classic|test|build]"
        exit 1
        ;;
esac
