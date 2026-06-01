# compile.ps1 — Build y lanzador para Dungeon Crawler 2D
# Uso:
#   .\compile.ps1          — lanza el juego TUI (construye el JAR si no existe)
#   .\compile.ps1 tui      — igual que el anterior
#   .\compile.ps1 classic  — compila y lanza la versión clásica por consola
#   .\compile.ps1 test     — compila y ejecuta los tests
#   .\compile.ps1 build    — empaqueta el fat JAR con Maven

param(
    [string]$Mode = "tui"
)

$MAVEN    = "$env:USERPROFILE\Downloads\maven\apache-maven-3.9.16\bin\mvn.cmd"
$JAR      = "target\dungeon-crawler-2d-1.2.0.jar"
$SRC_MAIN = "src\main\java"
$SRC_TEST = "src\test\java"
$BIN_DIR  = "bin"
$MAIN_CLASSIC = "com.dungeoncrawler.Main"
$TEST_CLASS   = "com.dungeoncrawler.TestRunner"

Write-Host "=== DUNGEON CRAWLER 2D ===" -ForegroundColor Yellow

function Invoke-Maven {
    & $MAVEN package -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error en el build Maven." -ForegroundColor Red
        exit 1
    }
}

function Invoke-ClassicCompile {
    New-Item -ItemType Directory -Force -Path $BIN_DIR | Out-Null
    $sources = @(
        Get-ChildItem "$SRC_MAIN\com\dungeoncrawler\core\*.java" | Select-Object -ExpandProperty FullName
        "$SRC_MAIN\com\dungeoncrawler\Main.java"
        "$SRC_TEST\com\dungeoncrawler\TestRunner.java"
    )
    & javac -d $BIN_DIR $sources
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error en la compilacion." -ForegroundColor Red
        exit 1
    }
    Write-Host "Compilacion exitosa." -ForegroundColor Green
}

switch ($Mode.ToLower()) {

    "build" {
        Write-Host "Empaquetando fat JAR con Maven..."
        Invoke-Maven
        Write-Host "JAR generado: $JAR" -ForegroundColor Green
    }

    "classic" {
        Write-Host "Compilando (clasico)..."
        Invoke-ClassicCompile
        Write-Host "Iniciando juego clasico..."
        & java -cp $BIN_DIR $MAIN_CLASSIC
    }

    "test" {
        Write-Host "Compilando (tests)..."
        Invoke-ClassicCompile
        Write-Host "Ejecutando tests..."
        & java -cp $BIN_DIR $TEST_CLASS
    }

    { $_ -in "tui", "" } {
        if (-not (Test-Path $JAR)) {
            Write-Host "JAR no encontrado, compilando con Maven..."
            Invoke-Maven
        }
        Write-Host "Iniciando TUI (JLine3)..." -ForegroundColor Cyan
        & java --enable-native-access=ALL-UNNAMED `
               "-Dorg.jline.terminal.disableDeprecatedProviderWarning=true" `
               "-Dorg.fusesource.jansi.Ansi.disable=false" `
               -jar $JAR 2>$null
    }

    default {
        Write-Host "Uso: .\compile.ps1 [tui|classic|test|build]" -ForegroundColor Red
        exit 1
    }
}
