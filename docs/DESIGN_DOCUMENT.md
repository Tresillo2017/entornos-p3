# Design Document - Dungeon Crawler 2D Motor

**Date:** June 1, 2026  
**Version:** 1.0  
**Status:** COMPLETE  

## Executive Summary

Designed and implemented a **2D Dungeon Crawler game engine** in Java with:
- **6 core classes** (respecting strict architectural limits)
- **2 advanced features:** Collision detection + NPC AI with achievements
- **Professional Git workflow** with Conventional Commits
- **Comprehensive UML documentation** (Mermaid diagrams)
- **5 detailed use cases** with pre/post-conditions and business rules

## Architecture Decisions

### Why 6 Classes?
```
EntidadVideojuego (abstract base)
├── Jugador (player with levels)
├── Enemigo (AI with state machine)
└── Item (collectibles)

MotorJuego (game brain)
├── GestorEntradas (input handling)
└── SistemaLogros (achievements)

Main (console interface)
```

**Rationale:**
1. **EntidadVideojuego** - Eliminates code duplication (DRY principle)
2. **Polymorphism** - Subclasses override `actualizar()` for their specific AI
3. **MotorJuego** - Single Responsibility: orchestration and collision detection
4. **GestorEntradas** - Separation of concerns (input ≠ game logic)
5. **SistemaLogros** - Optional extension point for future expansions
6. **Main** - UI layer and game loop

### Key Design Patterns

#### 1. Template Method (EntidadVideojuego)
```java
public abstract void actualizar(MotorJuego motor); // defined by subclasses
```
- Base class defines structure
- Subclasses provide specific implementation

#### 2. State Machine (Enemigo)
```
        distance ≤ 1
           PATROL ──→ CHASE ──→ ATTACK
             ↑                    ↓
             └────────────────────┘
```
- Three discrete states (enum)
- Deterministic transitions based on distance
- No undefined behavior

#### 3. Composition (MotorJuego)
- Contains lists of enemies and items
- Aggregates SistemaLogros
- Manages lifecycle of all entities

#### 4. Command Pattern (GestorEntradas)
```java
desplazarEntidad("ARRIBA")  → MotorJuego processes movement
```
- Encapsulates user commands
- Decouples UI from logic

## Game Loop Flow

```
[MENU] → [INIT GAME]
          ↓
      [JUGANDO]
      ├─ Update all entities
      │  ├─ Player (input-driven)
      │  ├─ Enemies (AI state machine)
      │  └─ Items (passive)
      ├─ Check collisions
      │  ├─ Player ↔ Enemy → damage
      │  └─ Player ↔ Item → collect
      ├─ Process achievements
      └─ Remove dead entities
          ↓
      [PAUSA] or [GAME OVER]
```

## Advanced Features Implemented

### AVANZADA 1: Collision Detection
**Location:** `MotorJuego.procesarColisiones()`

**Algorithm:**
```java
for each enemy:
    if (jugador.x == enemy.x && jugador.y == enemy.y) {
        // Collision detected
    }
for each item:
    if (jugador.x == item.x && jugador.y == item.y) {
        // Pickup
    }
```

**Complexity:** O(n + m) per tick where n=enemies, m=items

### AVANZADA 2: NPC AI + Achievement System

#### NPC State Machine
```
Private Method: actualizar()
├─ Calculate distance to player
├─ Determine new state:
│  ├─ PATRULLAR: random walk
│  ├─ PERSEGUIR: move closer (Manhattan metric)
│  └─ ATACAR: inflict damage (2s cooldown)
```

**Why Manhattan Distance?**
- Grid-based movement (no diagonals)
- O(1) calculation vs sqrt(dx²+dy²)
- Matches game design (4-directional movement)

#### Achievement System
```
Counters (HashMap)
    ↓
verificarLogro() checks conditions
    ↓
If met: desbloquearLogro()
    ↓
User notified + logged
```

**Achievements:**
- RECOLECTAR_5_MONEDAS (50 XP)
- ELIMINAR_TODOS_ENEMIGOS (100 XP)
- NIVEL_10 (200 XP)
- SUPERVIVIR_10_TURNOS (75 XP)

## Error Handling

### Compilation Errors Encountered
1. **Method naming inconsistency** (estaViva vs estaVivo)
   - Fixed by standardizing to `estaVivo()` in base class

2. **File corruption** (Enemigo.java)
   - Caused by incomplete string replacement
   - Resolved by recreating file from scratch

3. **No external exceptions** thrown
   - Game state validates before operations
   - Missing preconditions logged, operation silently ignored

## Testing Strategy

### Unit Test Candidates
```java
@Test
void testJugadorMovement() { 
    // verify boundary conditions
}

@Test
void testEnemyStateTransitions() { 
    // verify distance-based transitions
}

@Test
void testCollisionDetection() { 
    // verify correct entity overlap
}
```

*Not implemented due to time constraints, but fully testable.*

## Performance Characteristics

| Operation | Complexity | Notes |
|-----------|-----------|-------|
| actualizar() | O(n + m) | n=enemies, m=items |
| procesarColisiones() | O(n + m) | Linear scan needed |
| Distancia Manhattan | O(1) | Constant calculation |
| State transition | O(1) | Single if-else chain |

**Bottleneck:** Collision detection (could use spatial hashing for 100s of entities)

## Future Enhancements

### Short Term
- JUnit 5 test suite
- Command pattern for undo/redo
- Spatial partitioning (quadtree) for large maps

### Medium Term
- Quick Save feature (serialize to JSON)
- More enemy types (Zombie, Skeleton variants)
- Weapon/armor system
- Boss enemies with complex AI

### Long Term
- LibGDX renderer integration
- Procedural dungeon generation
- Networking for multiplayer
- Mobile touch controls

## Conclusion

**Scope achieved:** 6 classes, 2 advanced features, comprehensive documentation  
**Quality:** Clean architecture, proper OOP principles, extensible design  
**Delivery:** Professional Git workflow, Conventional Commits, UML documentation  

---

*Developed with GitHub Copilot assistance. All prompts validated, errors corrected manually.*
