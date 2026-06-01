#!/usr/bin/env bash
# Script para compilar y ejecutar el motor del juego

SRC_DIR="src/main/java"
BIN_DIR="bin"
MAIN_CLASS="com.dungeoncrawler.Main"

echo "=== COMPILANDO DUNGEON CRAWLER ==="

# Crear directorio de salida
mkdir -p "$BIN_DIR"

# Compilar
echo "Compilando archivos Java..."
javac -d "$BIN_DIR" "$SRC_DIR"/com/dungeoncrawler/*.java "$SRC_DIR"/com/dungeoncrawler/core/*.java

if [ $? -eq 0 ]; then
    echo "Compilación exitosa"
    
    # Ejecutar
    echo "Iniciando juego..."
    java -cp "$BIN_DIR" "$MAIN_CLASS"
else
    echo "Error en la compilación"
    exit 1
fi
