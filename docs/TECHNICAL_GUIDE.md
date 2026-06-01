# Technical Implementation Guide

## 1. Core Algorithms

### 1.1 Enemy AI State Machine

```
PSEUDOCODE:
function Enemigo.actualizar(motor):
    if not this.estaVivo():
        return
    
    jugador = motor.getJugador()
    if jugador == null or not jugador.estaVivo():
        return
    
    distancia = this.distanciaA(jugador.x, jugador.y)
    
    if distancia <= 1:
        this.estado = ATACAR
        this.atacarJugador(jugador)
    else if distancia <= this.rangoDeteccion:
        this.estado = PERSEGUIR
        this.perseguirJugador(jugador)
    else:
        this.estado = PATRULLAR
        this.patrullar()
```

### 1.2 Collision Detection

```
PSEUDOCODE:
function MotorJuego.procesarColisiones():
    // Jugador-Enemy collisions
    for each enemy in enemigos:
        if not enemy.estaVivo():
            continue
        
        if enemy.x == jugador.x and enemy.y == jugador.y:
            // En combate directo (daño ya infligido por enemy.actualizar)
            log("COLISION: Jugador y " + enemy.nombre)
    
    // Jugador-Item collisions
    for each item in items:
        if item.recolectado:
            continue
        
        if item.x == jugador.x and item.y == jugador.y:
            procesarRecoleccion(item)
```

### 1.3 Movement Validation

```
PSEUDOCODE:
function Jugador.mover(direccion):
    nuevoX = this.x
    nuevoY = this.y
    
    switch (direccion.toUpperCase()):
        case "ARRIBA":    nuevoY = max(0, this.y - 1)
        case "ABAJO":     nuevoY = min(20, this.y + 1)
        case "IZQUIERDA": nuevoX = max(0, this.x - 1)
        case "DERECHA":   nuevoX = min(20, this.x + 1)
    
    this.x = nuevoX
    this.y = nuevoY
```

### 1.4 Achievement Verification

```
PSEUDOCODE:
function SistemaLogros.verificarLogro(idLogro):
    if idLogro in logrosDesbloqueados:
        return false  // Already unlocked
    
    desbloqueado = false
    switch (idLogro):
        case "RECOLECTAR_5_MONEDAS":
            if contadores.get("MONEDAS_RECOLECTADAS") >= 5:
                desbloqueado = true
        
        case "NIVEL_10":
            desbloqueado = true  // Verified in caller
        
        case "SUPERVIVIR_10_TURNOS":
            desbloqueado = true
        
        case "ELIMINAR_TODOS_ENEMIGOS":
            desbloqueado = true
    
    if desbloqueado:
        desbloquearLogro(idLogro)
        return true
    
    return false
```

## 2. Data Structures

### 2.1 Entity Hierarchy

```
EntidadVideojuego (abstract)
    attributes:
        - nombre: String
        - tipo: String  // "JUGADOR", "ENEMIGO", "ITEM"
        - x, y: int     // Grid position (0-20)
        - w, h: int     // Dimensions (typically 1x1)
        - vida: int     // Current health
        - vidaMaxima: int
        - sprite: String // For future rendering
    
    methods:
        + recibirDanio(int)
        + curar(int)
        + estaVivo(): boolean
        # actualizar(MotorJuego)*  // Abstract

    ├─ Jugador
    │   attributes:
    │       - experiencia: int
    │       - nivel: int  // Increases every 100 XP
    │   methods:
    │       + mover(String)
    │       + ganarExperiencia(int)
    │
    ├─ Enemigo
    │   attributes:
    │       - estado: Estado (PATRULLAR|PERSEGUIR|ATACAR)
    │       - rangoDeteccion: int
    │       - danioAtaque: int
    │       - ultimoAtaque: long  // For cooldown
    │   methods:
    │       + getEstado(): Estado
    │       - patrullar()
    │       - perseguirJugador(Jugador)
    │       - atacarJugador(Jugador)
    │
    └─ Item
        attributes:
            - tipoItem: TipoItem (MONEDA|POCION|LLAVE)
            - valor: int  // Gold/HP amount
            - recolectado: boolean
        methods:
            + getTipoItem(): TipoItem
            + recolectar()
```

### 2.2 Motor State Machine

```
EstadoJuego:
    MENU
        ↓ (Jugador elige "Iniciar")
    JUGANDO
        ↓ (actualizar() called each tick)
    PAUSA (si comando PAUSA)
        ↓ (comando PAUSA reanuda)
    GAME_OVER (jugador.vida <= 0)
        ↓ (no regreso)
    FIN
```

### 2.3 Collections in MotorJuego

```
- jugador: Jugador (null inicialmente)
- enemigos: List<Enemigo> (ArrayList)
- items: List<Item> (ArrayList)
- sistemaLogros: SistemaLogros (singleton)
- estado: EstadoJuego
- pausa: boolean
- puntuacion: int (accumulated)
- ticks: long (frame counter)
```

## 3. Input Processing Flow

```
Main.bucleJuego()
    │
    ├─ input.desplazarEntidad("ARRIBA")
    │   └─ GestorEntradas.desplazarEntidad()
    │       └─ Jugador.mover()
    │           └─ actualiza (x,y)
    │
    ├─ input.pulsarBotonAccion()
    │   └─ GestorEntradas.pulsarBotonAccion()
    │       └─ Notifica interacción (futuro)
    │
    ├─ input.pausarJuego()
    │   └─ MotorJuego.estado = PAUSA
    │
    └─ motor.actualizar()  // Game tick
        ├─ Jugador.actualizar(motor)
        ├─ for Enemigo: enemy.actualizar(motor)
        ├─ MotorJuego.procesarColisiones()
        │   ├─ Jugador-Enemy collisions
        │   └─ Jugador-Item collisions
        ├─ Remove dead enemies
        ├─ Remove collected items
        └─ Return to input prompt
```

## 4. State Transitions (Detailed)

### 4.1 Jugador State During Tick

```
START tick()
    │
    ├─ Is jugador alive? (vida > 0)
    │   │
    │   ├─ YES: continue
    │   │
    │   └─ NO: → GAME_OVER
    │
    ├─ Process movement command (if any)
    │   └─ Update (x, y)
    │
    └─ (No health regeneration, passive until hit)
```

### 4.2 Enemigo State During Tick

```
START enemy.actualizar(motor)
    │
    ├─ Is enemy alive? (vida > 0)
    │   │
    │   ├─ NO: return (skip this turn)
    │   │
    │   └─ YES: continue
    │
    ├─ Get player position from motor
    │
    ├─ Calculate distance (Manhattan)
    │
    ├─ Determine new state:
    │   │
    │   ├─ dist ≤ 1
    │   │   └─ State → ATACAR
    │   │       └─ attackCooldown < 2000ms? skip : deal damage
    │   │
    │   ├─ 1 < dist ≤ rangoDeteccion
    │   │   └─ State → PERSEGUIR
    │   │       └─ Move 1 step closer (reduce distance)
    │   │
    │   └─ dist > rangoDeteccion
    │       └─ State → PATRULLAR
    │           └─ Random move in 3x3 area
    │
    └─ END
```

## 5. Collision Resolution

```
FUNCTION procesarRecoleccion(item):
    item.recolectar()  // Mark as collected
    
    switch item.tipoItem:
        case MONEDA:
            this.puntuacion += item.valor
            log("[COLISION] Recogida moneda")
            sistemaLogros.incrementarContador(
                "MONEDAS_RECOLECTADAS", 
                item.valor
            )
            sistemaLogros.verificarLogro(
                "RECOLECTAR_5_MONEDAS"
            )
        
        case POCION:
            jugador.curar(item.valor)
            log("[COLISION] Bebida poción")
        
        case LLAVE:
            log("[COLISION] Obtenida llave")
            // Future: unlock doors
    
    // Item removed by:
    items.removeIf(Item::esRecolectado)
```

## 6. Performance Analysis

### Time Complexity per Tick

| Operation | Time | Reason |
|-----------|------|--------|
| Actualizar Jugador | O(1) | No iteration |
| Actualizar Enemigos | O(n) | n = number of enemies |
| Actualizar Items | O(1) | Passive objects |
| Procesar Colisiones | O(n+m) | Linear scan both lists |
| Limpiar Entidades | O(n+m) | removeIf traverses |
| **Total por tick** | **O(n+m)** | n=enemies, m=items |

### Space Complexity

| Structure | Space | Notes |
|-----------|-------|-------|
| Enemigos list | O(n) | Dynamic array |
| Items list | O(m) | Dynamic array |
| SistemaLogros | O(1) | Max 4 achievements |
| **Total** | **O(n+m)** | Linear with entities |

### Bottleneck Analysis

**Current:** Linear collision check O(n*m) worst case
```java
// Naive implementation:
for (Enemy e : enemigos)
    for (Item i : items)
        if (e.x == i.x && e.y == i.y)
            // Unlikely in sparse map
```

**Actual:** O(n+m) because separate loops
```java
for (Enemy e : enemigos)
    if (e.x == j.x && e.y == j.y) {...}  // O(n)
for (Item i : items)
    if (i.x == j.x && i.y == j.y) {...}  // O(m)
```

**Future optimization:** Spatial hash table for large maps (100+ entities)

## 7. Error Conditions and Handling

### Precondition Violations

```
Condition: "desplazarEntidad() called when paused"
Behavior: Log warning, ignore movement
Code: if (motor.getEstado() != JUGANDO) { return; }

Condition: "Movement outside bounds"
Behavior: Clamp to [0, 20]
Code: nuevoX = Math.max(0, Math.min(20, nuevoX))

Condition: "Enemy target dies mid-transition"
Behavior: Return to PATRULLAR
Code: if (jugador == null || !jugador.estaVivo()) { return; }
```

### No Exception Throwing
- Design decision: Game continues, invalid operations silently ignored
- Rationale: Robust simulated game (console input)
- Alternative: Throw `GameStateException` if more formal error handling needed

## 8. Extensibility Points

### How to Add New Logros
1. Add entry to `LOGROS_DISPONIBLES` map in `SistemaLogros`
2. Add condition check in `verificarLogro(String)`
3. Call `incrementarContador()` where event occurs
4. Call `verificarLogro()` after event

**Example:**
```java
// In SistemaLogros (static block):
LOGROS_DISPONIBLES.put("MATAR_BOSS", 
    new Logro("MATAR_BOSS", "Derrota al jefe", 500)
);

// In MotorJuego when boss dies:
sistemaLogros.verificarLogro("MATAR_BOSS");
```

### How to Add Enemy Types
1. Extend `Enemigo` or `EntidadVideojuego`
2. Override `actualizar()` with new AI logic
3. Add instance to `motor.enemigos` in `iniciarPartida()`

**Example:**
```java
public class Dragon extends Enemigo {
    @Override
    public void actualizar(MotorJuego motor) {
        // Flying behavior, area attacks, etc.
    }
}
```

### How to Add Item Types
1. Add to `Item.TipoItem` enum
2. Extend Item or add handling in `procesarRecoleccion()`

## Conclusion

The architecture prioritizes **simplicity, clarity, and extensibility** while maintaining strict constraints (6 classes). All algorithms are deterministic, collision-safe, and ready for GUI integration.
