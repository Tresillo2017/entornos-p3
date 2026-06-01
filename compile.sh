#!/usr/bin/env bash
# Compila y ejecuta el motor del juego o la suite de tests.
# Uso:
#   ./compile.sh          — compila y lanza el juego
#   ./compile.sh test     — compila y ejecuta los tests

SRC_MAIN="src/main/java"
SRC_TEST="src/test/java"
BIN_DIR="bin"
MAIN_CLASS="com.dungeoncrawler.Main"
TEST_CLASS="com.dungeoncrawler.TestRunner"

echo "=== DUNGEON CRAWLER 2D ==="
mkdir -p "$BIN_DIR"

echo "Compilando..."
javac -d "$BIN_DIR" \
    "$SRC_MAIN"/com/dungeoncrawler/core/*.java \
    "$SRC_MAIN"/com/dungeoncrawler/Main.java \
    "$SRC_TEST"/com/dungeoncrawler/TestRunner.java

if [ $? -ne 0 ]; then
    echo "Error en la compilación."
    exit 1
fi

echo "Compilación exitosa."

if [ "$1" = "test" ]; then
    echo "Ejecutando tests..."
    java -cp "$BIN_DIR" "$TEST_CLASS"
else
    echo "Iniciando juego..."
    java -cp "$BIN_DIR" "$MAIN_CLASS"
fi
