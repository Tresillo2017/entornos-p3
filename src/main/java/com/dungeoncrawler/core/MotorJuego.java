package com.dungeoncrawler.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase cerebro del motor: controla el estado general del juego,
 * gestiona la colección de entidades, simula el bucle de juego y procesa colisiones.
 *
 * Responsabilidades:
 * 1. Mantener estado (Menú, Jugando, Pausa, GameOver, Victoria)
 * 2. Almacenar y actualizar todas las entidades
 * 3. Simular el bucle de juego (actualizar, procesar colisiones, eliminar entidades muertas)
 * 4. Procesar colisiones (AVANZADA 1)
 * 5. Verificar logros mediante SistemaLogros (AVANZADA 2)
 */
public class MotorJuego {
    public enum EstadoJuego {
        MENU, JUGANDO, PAUSA, GAME_OVER, VICTORIA
    }

    private static final int PUNTOS_POR_ENEMIGO  = 10;
    private static final int PUNTOS_POR_MONEDA   = 5;
    private static final int DANIO_ATAQUE_JUGADOR = 3;
    private static final int RANGO_ATAQUE_JUGADOR = 1;

    private EstadoJuego estado;
    private int puntuacion;
    private long ticks;

    private Jugador jugador;
    private final List<Enemigo> enemigos;
    private final List<Item> items;
    private SistemaLogros sistemaLogros;

    public MotorJuego() {
        this.estado = EstadoJuego.MENU;
        this.puntuacion = 0;
        this.ticks = 0;
        this.enemigos = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public EstadoJuego getEstado() {
        return estado;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public List<Enemigo> getEnemigos() {
        return new ArrayList<>(enemigos);
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public long getTicks() {
        return ticks;
    }

    public SistemaLogros getSistemaLogros() {
        return sistemaLogros;
    }

    // ── Control de estado ────────────────────────────────────────────────────

    public void setPausa(boolean pausa) {
        if (pausa && estado == EstadoJuego.JUGANDO) {
            estado = EstadoJuego.PAUSA;
        } else if (!pausa && estado == EstadoJuego.PAUSA) {
            estado = EstadoJuego.JUGANDO;
        }
    }

    public void pausar() {
        if (estado == EstadoJuego.JUGANDO) {
            estado = EstadoJuego.PAUSA;
            System.out.println("[MOTOR] Juego pausado");
        }
    }

    public void reanudar() {
        if (estado == EstadoJuego.PAUSA) {
            estado = EstadoJuego.JUGANDO;
            System.out.println("[MOTOR] Juego reanudado");
        }
    }

    public void gameOver() {
        System.out.println("[MOTOR] GAME OVER");
        estado = EstadoJuego.GAME_OVER;
    }

    // ── Ciclo de vida de la partida ───────────────────────────────────────────

    /**
     * Inicia una nueva partida: crea jugador, enemigos e items.
     */
    public void iniciarPartida() {
        System.out.println("[MOTOR] Inicializando nueva partida...");

        jugador = new Jugador("Adventurer", 10, 10, 20);

        enemigos.clear();
        enemigos.add(new Enemigo("Goblin_1",   3,  3, 5, 2, 5));
        enemigos.add(new Enemigo("Orc_1",     15, 15, 8, 3, 6));
        enemigos.add(new Enemigo("Skeleton_1", 7, 18, 4, 1, 4));

        items.clear();
        items.add(new Item("Moneda_1",  5,  5, Item.TipoItem.MONEDA,  5));
        items.add(new Item("Moneda_2", 12,  8, Item.TipoItem.MONEDA,  5));
        items.add(new Item("Pocion_1",  2, 14, Item.TipoItem.POCION, 10));

        puntuacion = 0;
        ticks = 0;
        sistemaLogros = new SistemaLogros();
        estado = EstadoJuego.JUGANDO;

        System.out.println("[MOTOR] Partida iniciada. Jugador: " + jugador);
    }

    // ── Bucle principal ───────────────────────────────────────────────────────

    /**
     * Tick del bucle de juego.
     * Actualiza entidades, procesa colisiones, elimina entidades muertas
     * y verifica condiciones de victoria/derrota.
     */
    public void actualizar() {
        if (estado != EstadoJuego.JUGANDO) {
            return;
        }

        ticks++;

        if (jugador != null && jugador.estaVivo()) {
            jugador.actualizar(this);
        } else {
            gameOver();
            return;
        }

        for (Enemigo enemigo : new ArrayList<>(enemigos)) {
            if (enemigo.estaVivo()) {
                enemigo.actualizar(this);
            }
        }

        procesarColisiones();

        enemigos.removeIf(e -> !e.estaVivo());
        items.removeIf(i -> !i.estaVivo());

        sistemaLogros.verificarLogros(jugador, enemigos.size(), ticks);

        if (enemigos.isEmpty()) {
            System.out.println("[MOTOR] ¡Todos los enemigos han sido derrotados! VICTORIA");
            estado = EstadoJuego.VICTORIA;
        }
    }

    // ── Colisiones (AVANZADA 1) ───────────────────────────────────────────────

    /**
     * Detecta colisiones del jugador con enemigos e items.
     * Colisión jugador-enemigo: el enemigo inflige daño.
     * Colisión jugador-item: se aplica el efecto del item.
     */
    private void procesarColisiones() {
        if (jugador == null || !jugador.estaVivo()) return;

        int jx = jugador.getX();
        int jy = jugador.getY();

        for (Enemigo enemigo : enemigos) {
            if (!enemigo.estaVivo()) continue;
            if (enemigo.getX() == jx && enemigo.getY() == jy) {
                jugador.recibirDanio(enemigo.getDanioAtaque());
                System.out.println("[COLISION] " + jugador.getNombre()
                        + " colisionó con " + enemigo.getNombre()
                        + ". Vida: " + jugador.getVida());
                if (!jugador.estaVivo()) {
                    gameOver();
                    return;
                }
            }
        }

        for (Item item : items) {
            if (!item.isRecolectado() && item.getX() == jx && item.getY() == jy) {
                aplicarEfectoItem(item);
            }
        }
    }

    private void aplicarEfectoItem(Item item) {
        item.recolectar();
        if (item.getTipoItem() == Item.TipoItem.MONEDA) {
            puntuacion += item.getValor();
            sistemaLogros.registrarMonedaRecolectada();
            System.out.println("[ITEM] Moneda recogida. +" + item.getValor()
                    + " pts. Puntuación: " + puntuacion);
        } else {
            jugador.curar(item.getValor());
            System.out.println("[ITEM] Poción usada. +" + item.getValor()
                    + " vida. Vida: " + jugador.getVida());
        }
    }

    // ── Gestión de entidades ──────────────────────────────────────────────────

    public void agregarEnemigo(Enemigo enemigo) {
        if (enemigo != null) {
            enemigos.add(enemigo);
            System.out.println("[MOTOR] Enemigo agregado: " + enemigo.getNombre());
        }
    }

    public void eliminarEnemigo(Enemigo enemigo) {
        if (enemigos.remove(enemigo)) {
            System.out.println("[MOTOR] Enemigo eliminado: " + enemigo.getNombre());
        }
    }

    // ── Acción del jugador ────────────────────────────────────────────────────

    /**
     * El jugador ataca al enemigo adyacente más cercano (distancia Manhattan ≤ 1).
     */
    public void atacarEnemigoCercano() {
        if (estado != EstadoJuego.JUGANDO || jugador == null || !jugador.estaVivo()) return;

        Enemigo objetivo = null;
        int distanciaMin = Integer.MAX_VALUE;

        for (Enemigo enemigo : enemigos) {
            if (!enemigo.estaVivo()) continue;
            int dist = Math.abs(enemigo.getX() - jugador.getX())
                     + Math.abs(enemigo.getY() - jugador.getY());
            if (dist <= RANGO_ATAQUE_JUGADOR && dist < distanciaMin) {
                objetivo = enemigo;
                distanciaMin = dist;
            }
        }

        if (objetivo == null) {
            System.out.println("[MOTOR] No hay enemigos adyacentes para atacar");
            return;
        }

        objetivo.recibirDanio(DANIO_ATAQUE_JUGADOR);
        System.out.println("[MOTOR] " + jugador.getNombre() + " atacó a "
                + objetivo.getNombre() + ". Vida enemigo: " + objetivo.getVida());

        if (!objetivo.estaVivo()) {
            puntuacion += PUNTOS_POR_ENEMIGO;
            jugador.ganarExperiencia(PUNTOS_POR_ENEMIGO);
            System.out.println("[MOTOR] Enemigo derrotado. Puntuación: " + puntuacion);
        }
    }

    // ── Mostrar estado ────────────────────────────────────────────────────────

    public void mostrarEstado() {
        System.out.println("\n=== ESTADO DEL JUEGO ===");
        System.out.println("Estado:      " + estado);
        System.out.println("Ticks:       " + ticks);
        System.out.println("Puntuación:  " + puntuacion);
        if (jugador != null) {
            System.out.println("Jugador:     " + jugador);
        }
        System.out.println("Enemigos (" + enemigos.size() + "):");
        for (Enemigo e : enemigos) {
            System.out.println("  - " + e);
        }
        System.out.println("Items (" + items.size() + "):");
        for (Item i : items) {
            System.out.println("  - " + i);
        }
        if (sistemaLogros != null && !sistemaLogros.getLogrosDesbloqueados().isEmpty()) {
            System.out.println("Logros: " + sistemaLogros.getLogrosDesbloqueados());
        }
        System.out.println("========================\n");
    }
}
