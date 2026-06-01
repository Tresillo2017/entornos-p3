# Dungeon Crawler 2D - Motor de Videojuego

**Versión:** 1.0  
**Autor:** Desarrollado con asistencia de IA (GitHub Copilot)  
**Fecha:** Junio 2026  
**Licencia:** MIT  

## 1. Descripción del Proyecto

**Dungeon Crawler 2D** es un juego tipo RPG/Dungeon Crawler implementado como un **motor de lógica** sin interfaz gráfica. El juego simula un aventurero (`Adventurer`) explorando una mazmorra cuadrícula 2D (20x20), donde se enfrenta a enemigos con IA básica, recolecta objetos valiosos y acumula puntuación mediante un sistema de logros.

### Temática
- **Tipo:** RPG/Dungeon Crawler (Grid-based)
- **Escenario:** Mazmorra mágica
- **Objetivo del Jugador:** Sobrevivir y derrotar enemigos, recolectar monedas y pociones
- **Mecánicas Core:**
  - Movimiento en grid 4-direcciones (arriba, abajo, izquierda, derecha)
  - Colisiones simples (daño de enemigos, recolección de items)
  - IA de enemigos con máquina de estados (patrullar, perseguir, atacar)
  - Sistema de experiencia y niveles
  - Logros desbloqueables

## 2. Arquitectura del Software

### Principios de Diseño
1. **Modularidad:** 6 clases máximo con responsabilidades bien definidas
2. **Encapsulación:** Atributos privados, getters/setters públicos
3. **Abstracción:** Clase base `EntidadVideojuego` para todas las entidades
4. **Herencia:** Jugador, Enemigo, Item heredan de EntidadVideojuego
5. **Composición:** MotorJuego contiene colecciones de entidades

### Clases Implementadas (Máx. 6)

#### 1. **EntidadVideojuego** (Clase Abstracta Base)
Defines the structure common to all game entities: position, size, health, and state.

#### 2. **Jugador**
Represents the player (Adventurer) with experience and level system.

#### 3. **Enemigo** ⭐ [AVANZADA 2]
Enemies with AI state machine (PATROL, CHASE, ATTACK). Implements behavior-based NPC logic.

#### 4. **Item**
Collectible objects (coins, potions, keys) on the map.

#### 5. **GestorEntradas** (InputManager)
Processes simulated player input commands (movement, actions).

#### 6. **MotorJuego** (Game Engine Orchestrator)
Brain of the game - controls flow, state, collision detection (AVANZADA 1), and achievements (AVANZADA 2).

#### 7. **SistemaLogros** (Achievement System)
Manages and tracks unlockable achievements with rewards.

#### 8. **Main**
Console interface for simulating the game loop.

## 3. Diagrama de Clases UML

\`\`\`mermaid
classDiagram
    class EntidadVideojuego {
        <<abstract>>
        -nombre: String
        -tipo: String
        -x: int
        -y: int
        -w: int
        -h: int
        -vida: int
        -vidaMaxima: int
        -sprite: String
        +getX(): int
        +getY(): int
        +getVida(): int
        +recibirDanio(int)
        +curar(int)
        +estaVivo(): boolean
        +actualizar(MotorJuego)*
    }

    class Jugador {
        -experiencia: int
        -nivel: int
        +mover(String)
        +ganarExperiencia(int)
        +actualizar(MotorJuego)
    }

    class Enemigo {
        -estado: Estado
        -rangoDeteccion: int
        -danioAtaque: int
        -ultimoAtaque: long
        +getEstado(): Estado
        +actualizar(MotorJuego)
        -patrullar()
        -perseguirJugador(Jugador)
        -atacarJugador(Jugador)
    }

    class Item {
        -tipoItem: TipoItem
        -valor: int
        -recolectado: boolean
        +getTipoItem(): TipoItem
        +recolectar()
        +actualizar(MotorJuego)
    }

    class MotorJuego {
        -estado: EstadoJuego
        -pausa: boolean
        -puntuacion: int
        -ticks: long
        -jugador: Jugador
        -enemigos: List~Enemigo~
        -items: List~Item~
        +iniciarPartida()
        +actualizar()
        +procesarColisiones()
    }

    class GestorEntradas {
        -motor: MotorJuego
        +desplazarEntidad(String)
        +pulsarBotonAccion()
    }

    class SistemaLogros {
        -logrosDesbloqueados: Set~String~
        +verificarLogro(String): boolean
        +getLogrosDesbloqueados(): Set~String~
    }

    EntidadVideojuego <|-- Jugador
    EntidadVideojuego <|-- Enemigo
    EntidadVideojuego <|-- Item
    MotorJuego "1" --> "1" Jugador
    MotorJuego "1" --> "*" Enemigo
    MotorJuego "1" --> "*" Item
    MotorJuego "1" --> "1" SistemaLogros
    GestorEntradas --> MotorJuego
\`\`\`

## 4. Diagrama de Casos de Uso

\`\`\`mermaid
graph LR
    Jugador[Jugador] 
    Motor[Sistema de Juego]
    
    Jugador -->|CU-01| IniciarPartida[CU-01: Iniciar Partida]
    Jugador -->|CU-02| MoverJugador[CU-02: Mover Jugador]
    Jugador -->|CU-03| RecolectarItem[CU-03: Recolectar Item]
    Jugador -->|CU-04| EncuentroEnemigo[CU-04: Encuentro con Enemigo]
    Jugador -->|CU-05| PausarJuego[CU-05: Pausar Juego]
    
    IniciarPartida --> Motor
    MoverJugador --> Motor
    RecolectarItem --> Motor
    EncuentroEnemigo --> Motor
    PausarJuego --> Motor
\`\`\`

## 5. Especificación de Casos de Uso

### CU-01: Iniciar Partida

| Campo | Descripción |
|-------|-------------|
| **Nombre** | CU-01 Iniciar Partida |
| **Objetivo** | Crear nuevo juego configurando estado inicial |
| **Actor Principal** | Jugador |
| **Precondiciones** | Juego en estado MENU |
| **Flujo Principal** | 1. Crear jugador en (10,10) con 20 vida<br>2. Crear 3 enemigos en posiciones aleatorias<br>3. Crear 3 items (2 monedas, 1 poción)<br>4. Cambiar estado a JUGANDO<br>5. Mostrar "Partida iniciada" |
| **Postcondiciones** | MotorJuego.estado = JUGANDO, Entidades en mapa |
| **Reglas de Negocio** | No iniciar si partida JUGANDO activa |

### CU-02: Mover Jugador

| Campo | Descripción |
|-------|-------------|
| **Nombre** | CU-02 Mover Jugador |
| **Objetivo** | Desplazar jugador en 4 direcciones |
| **Actor Principal** | Jugador |
| **Precondiciones** | Sistema en JUGANDO, jugador vivo |
| **Flujo Principal** | 1. Introducir comando (ARRIBA/ABAJO/IZQ/DER)<br>2. Validar dirección<br>3. Actualizar posición (x,y)<br>4. Procesar colisiones<br>5. Notificar "Jugador se movió..." |
| **Postcondiciones** | Posición cambia 1 casilla, colisiones procesadas |
| **Reglas de Negocio** | Solo 4 direcciones, mapa 20×20, sin diagonales |

### CU-03: Recolectar Item

| Campo | Descripción |
|-------|-------------|
| **Nombre** | CU-03 Recolectar Item |
| **Objetivo** | Recibir efecto de item |
| **Actor Principal** | Jugador |
| **Precondiciones** | Jugador e Item en misma posición |
| **Flujo Principal** | 1. Detectar colisión<br>2. Aplicar efecto (MONEDA: +pts, POCION: +vida)<br>3. Marcar recolectado<br>4. Verificar logros<br>5. Notificar recolección |
| **Postcondiciones** | Item desaparece, puntuación/vida actualizada |
| **Reglas de Negocio** | Un item solo recolectable una vez |

### CU-04: Encuentro con Enemigo

| Campo | Descripción |
|-------|-------------|
| **Nombre** | CU-04 Encuentro con Enemigo |
| **Objetivo** | Procesar interacción jugador-enemigo |
| **Actor Principal** | Jugador / Enemigo (IA) |
| **Precondiciones** | Enemigo vivo, distancia < rangoDeteccion |
| **Flujo Principal** | 1. Calcular distancia Manhattan<br>2. Transición: dist≤1→ATACAR, 1<dist≤rango→PERSEGUIR, dist>rango→PATRULLAR<br>3. Si ATACAR: infligir daño (cooldown 2s)<br>4. Si PERSEGUIR: acercarse al jugador<br>5. Si PATRULLAR: movimiento aleatorio |
| **Postcondiciones** | Vida jugador reducida (si atacado), enemigo movido |
| **Reglas de Negocio** | IA determinística por distancia, daño por turno |

### CU-05: Pausar Juego

| Campo | Descripción |
|-------|-------------|
| **Nombre** | CU-05 Pausar Juego |
| **Objetivo** | Detener temporalmente la ejecución |
| **Actor Principal** | Jugador |
| **Precondiciones** | Sistema en JUGANDO |
| **Flujo Principal** | 1. Introducir comando PAUSA<br>2. Cambiar estado a PAUSA<br>3. Detener actualización de entidades<br>4. Notificar "Juego pausado"<br>5. Comando PAUSA reanuda |
| **Postcondiciones** | Estado = PAUSA, bucle parado |
| **Reglas de Negocio** | Pausa reversible, sin timeout |

## 6. Funcionalidades Avanzadas Implementadas

### ⭐ AVANZADA 1: Detector de Colisiones Simple
**Ubicación:** `MotorJuego.procesarColisiones()`

Detecta:
- Jugador ↔ Enemigos → Daño automático
- Jugador ↔ Items → Recolección

**Implementación:** Comparación exacta de coordenadas (x, y)

### ⭐ AVANZADA 2: Comportamiento NPC + Sistema de Logros
**Ubicación:** `Enemigo.actualizar()` + `SistemaLogros`

**Enemigo - Máquina de Estados:**
- PATRULLAR: movimiento aleatorio
- PERSEGUIR: acercarse si distancia ≤ rangoDeteccion
- ATACAR: infligir daño si adyacente (cooldown 2s)

**Logros Desbloqueables:**
- ELIMINAR_TODOS_ENEMIGOS (+100 XP)
- RECOLECTAR_5_MONEDAS (+50 XP)
- NIVEL_10 (+200 XP)
- SUPERVIVIR_10_TURNOS (+75 XP)

## 7. Compilación y Ejecución

### Compilar
\`\`\`bash
javac -d bin src/main/java/com/dungeoncrawler/*.java src/main/java/com/dungeoncrawler/core/*.java
\`\`\`

### Ejecutar
\`\`\`bash
java -cp bin com.dungeoncrawler.Main
\`\`\`

### Dependencias
- Java 8+
- Sin dependencias externas

## 8. Bitácora del Uso de Inteligencia Artificial

### Herramienta Utilizada
**GitHub Copilot** - Asistente IA en VS Code

### Muestra de Prompts Exactos

#### Prompt 1: Arquitectura Base
\`\`\`
Crea una clase abstracta EntidadVideojuego en Java que represente 
cualquier entidad del juego. Debe tener atributos privados para:
- nombre, tipo, posición (x, y), tamaño (w, h)
- vida y vidaMaxima
- un sprite para la UI futura

Incluye getter/setter controlados y métodos públicos:
- recibirDanio(int): reduce vida
- curar(int): restaura vida
- estaVivo(): boolean

Haz que tenga un método abstracto actualizar(MotorJuego motor) 
para que las subclases implementen su lógica de IA o comportamiento.
\`\`\`

#### Prompt 2: IA de Enemigo
\`\`\`
Crea una clase Enemigo que extienda EntidadVideojuego.
Implementa una máquina de estados simple con 3 estados:
- PATRULLAR: movimiento aleatorio
- PERSEGUIR: acercarse al jugador si está en rango de detección
- ATACAR: infligir daño si es adyacente

Usa distancia Manhattan para determinar transiciones.
La lógica de transición debe ser determinística basada en 
distancia al jugador (obtenido via motor.getJugador()).

Implementa un cooldown de 2 segundos entre ataques.
\`\`\`

### Errores de la IA y Correcciones Aplicadas

#### Error 1: Inconsistencia en nombres de métodos
**Problema:** `estaViva()` vs `estaVivo()`

**Solución:** Estandaricé todos a `estaVivo()` en EntidadVideojuego y actualicé referencias en Jugador, Enemigo, MotorJuego, GestorEntradas.

#### Error 2: Archivo Enemigo.java corrompido
**Problema:** Reemplazo incompleto dejó métodos duplicados.

**Solución:** Eliminé y recreé el archivo desde cero.

### Reflexión Crítica

#### ✅ Ventajas
- Velocidad: completar arquitectura base en 2 horas vs. 4-5 manuales
- Reducción de boilerplate (getters, setters, toString)
- Sugerencias de arquitectura (máquina de estados, polimorfismo)
- Documentación automática

#### ⚠️ Peligros
- Errores silenciosos (inconsistencias de nombres)
- Over-engineering (clases extra innecesarias)
- Ilusión de comprensión (confiar ciegamente sin revisar)
- Deuda técnica acumulada

#### 🎯 Bajo Presión de Tiempo
- **Pro:** Arquitectura completa rápidamente
- **Contra:** Bugs sutiles requieren 30+ min de debug
- **Balance:** Mejor para plantillas, lógica compleja verificada manualmente

## 9. Estructura de Directorios

\`\`\`
entornos-p3/
├── README.md
├── LICENSE
├── compile.sh
├── bin/ (compilados)
├── src/main/java/
│   └── com/dungeoncrawler/
│       ├── Main.java
│       └── core/ (6 clases + SistemaLogros)
└── docs/
\`\`\`

## 10. Referencias

- Conventional Commits: https://www.conventionalcommits.org/
- Java Style Guide: Oracle Docs
- UML Diagrams: Mermaid.js
- Clean Code: Robert C. Martin

---

**Estado:** ✅ Completado  
**Última actualización:** 1 de junio de 2026