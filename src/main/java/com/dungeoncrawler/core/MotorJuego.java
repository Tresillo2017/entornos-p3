package com.dungeoncrawler.core;

import java.util.*;

/**
 * Clase cerebro del motor: controla el estado general del juego,
 * gestiona la colección de entidades, simula el bucle de juego y procesa colisiones.
 *
 * Responsabilidades:
 * 1. Mantener estado (Menú, Jugando, Pausa, GameOver)
 * 2. Almacenar y actualizar todas las entidades
 * 3. Simular el bucle de juego (actualizar, procesar colisiones, eliminar entidades muertas)
 * 4. Gestionar sistema de logros/recompensas
 * 5. Procesar colisiones (AVANZADA 1)
 */
public class MotorJuego {
    public enum EstadoJuego {
        MENU, JUGANDO, PAUSA, GAME_OVER
    }

    // Estado del juego
    private EstadoJuego estado;
    private boolean pausa;
    private int puntuacion;
    private long ticks;

    // Entidades
    private Jugador jugador;
    private List<Enemigo> enemigos;
    private List<Item> items;

    // Sistema de logros (AVANZADA 2)
    private SistemaLogros sistemaLogros;

    /**
     * Constructor del MotorJuego.
     * Inicializa el motor en estado MENU.
     */
    public MotorJuego() {
        this.estado = EstadoJuego.MENU;
        this.pausa = false;
        this.puntuacion = 0;
        this.ticks = 0;
        this.enemigos = new ArrayList<>();
        this.items = new ArrayList<>();
        this.sistemaLogros = new SistemaLogros();
    }

    // Getters
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

    /**
     * Setters controlados
     */
    public void setPausa(boolean pausa) {
        if (this.estado == EstadoJuego.JUGANDO) {
            this.pausa = pausa;
            this.estado = pausa ? EstadoJuego.PAUSA : EstadoJuego.JUGANDO;
        }
    }

    /**
     * Inicia una nueva partida.
     * Crea al jugador, enemigos e items, y cambia a estado JUGANDO.
     */
    public void iniciarPartida() {
        System.out.println("[MOTOR] Inicializando nueva partida...");
        
        // Crear jugador
        this.jugador = new Jugador("Adventurer", 10, 10, 20);
        
        // Crear enemigos
        this.enemigos.clear();
        this.enemigos.add(new Enemigo("Goblin_1", 3, 3, 5, 2, 5));
        this.enemigos.add(new Enemigo("Orc_1", 15, 15, 8, 3, 6));
        this.enemigos.add(new Enemigo("Skeleton_1", 7, 18, 4, 1, 4));
        
        // Crear items
        this.items.clear();
        this.items.add(new Item("Coin_1", Item.TipoItem.MONEDA, 5, 5, 10));
        this.items.add(new Item("Coin_2", Item.TipoItem.MONEDA, 12, 12, 10));
        this.items.add(new Item("HealthPotion_1", Item.TipoItem.POCION, 18, 5, 5));
        
        // Reiniciar sistema de logros
        this.sistemaLogros.reiniciar();
        this.puntuacion = 0;
        this.ticks = 0;
        
        this.estado = EstadoJuego.JUGANDO;
        this.pausa = false;
        System.out.println("[MOTOR] Partida iniciada. Jugador: " + this.jugador);
    }

    /**
     * Pausa/reanuda la partida manualmente.
     */
    public void pausar() {
        if (this.estado == EstadoJuego.JUGANDO) {
            this.pausa = true;
            this.estado = EstadoJuego.PAUSA;
            System.out.println("[MOTOR] Juego pausado");
        }
    }

    public void reanudar() {
        if (this.estado == EstadoJuego.PAUSA) {
            this.pausa = false;
            this.estado = EstadoJuego.JUGANDO;
            System.out.println("[MOTOR] Juego reanudado");
        }
    }

    /**
     * Fuerza un Game Over.
     */
    public void gameOver() {
        System.out.println("[MOTOR] GAME OVER");
        this.estado = EstadoJuego.GAME_OVER;
    }

    /**
     * Bucle principal de actualización del juego.
     * Llamado cada frame (simulado por tick).
     * Actualiza todas las entidades, procesa colisiones y elimina entidades muertas.
     */
    public void actualizar() {
        if (this.estado != EstadoJuego.JUGANDO || this.pausa) {
            return;
        }

        this.ticks++;

        // Actualizar entidades
        if (this.jugador != null && this.jugador.estaVivo()) {
            this.jugador.actualizar(this);
        } else if (this.jugador != null) {
            gameOver();
            return;
        }

        for (Enemigo enemigo : new ArrayList<>(enemigos)) {
            if (enemigo.estaVivo()) {
                enemigo.actualizar(this);
            }
        }

        // Procesar colisiones
        procesarColisiones();

        // Eliminar entidades muertas
        enemigos.removeIf(e -> !e.estaViva());
        items.removeIf(Item::esRecolectado);

        // Verificar condiciones de victoria
        if (enemigos.isEmpty()) {
            System.out.println("[MOTOR] ¡Todos los enemigos han sido derrotados!");
            sistemaLogros.verificarLogro("ELIMINAR_TODOS_ENEMIGOS");
        }
    }

    /**
     * AVANZADA 1: Detector de Colisiones Simple
     * Verifica colisiones entre:
     * - Jugador con Enemigos: reduce vida del jugador
     * - Jugador con Items: recolecta el item y suma puntos
     */
    private void procesarColisiones() {
        if (this.jugador == null || !this.jugador.estaVivo()) {
            return;
        }

        int jX = this.jugador.getX();
        int jY = this.jugador.getY();

        // Colisión con enemigos
        for (Enemigo enemigo : enemigos) {
            if (!enemigo.estaViva()) continue;
            
            if (enemigo.getX() == jX && enemigo.getY() == jY) {
                // Colisión directa: el enemigo ya causó daño en su actualización
                System.out.println("[COLISION] Jugador y " + enemigo.getNombre() + " ocupan la misma posición");
            }
        }

        // Colisión con items
        for (Item item : items) {
            if (item.esRecolectado()) continue;
            
            if (item.getX() == jX && item.getY() == jY) {
                procesarRecoleccion(item);
            }
        }
    }

    /**
     * Procesa la recolección de un item.
     * Suma puntos y aplica el efecto del item.
     */
    private void procesarRecoleccion(Item item) {
        item.recolectar();
        
        switch (item.getTipoItem()) {
            case MONEDA:
                this.puntuacion += item.getValor();
                System.out.println("[COLISION] " + this.jugador.getNombre() + " recolectó " 
                        + item.getNombre() + ". Puntuación: " + this.puntuacion);
                sistemaLogros.incrementarContador("MONEDAS_RECOLECTADAS", item.getValor());
                sistemaLogros.verificarLogro("RECOLECTAR_5_MONEDAS");
                break;
                
            case POCION:
                this.jugador.curar(item.getValor());
                System.out.println("[COLISION] " + this.jugador.getNombre() + " usó " 
                        + item.getNombre() + ". Vida: " + this.jugador.getVida());
                break;
                
            case LLAVE:
                System.out.println("[COLISION] " + this.jugador.getNombre() + " obtuvo " 
                        + item.getNombre());
                break;
        }
    }

    /**
     * Agrega una entidad enemiga al juego.
     *
     * @param enemigo El enemigo a agregar
     */
    public void agregarEnemigo(Enemigo enemigo) {
        if (enemigo != null) {
            this.enemigos.add(enemigo);
            System.out.println("[MOTOR] Enemigo agregado: " + enemigo.getNombre());
        }
    }

    /**
     * Elimina una entidad enemiga del juego.
     *
     * @param enemigo El enemigo a eliminar
     */
    public void eliminarEnemigo(Enemigo enemigo) {
        if (this.enemigos.remove(enemigo)) {
            System.out.println("[MOTOR] Enemigo eliminado: " + enemigo.getNombre());
        }
    }

    /**
     * Imprime el estado actual del juego en consola.
     */
    public void mostrarEstado() {
        System.out.println("\n=== ESTADO DEL JUEGO ===");
        System.out.println("Estado: " + this.estado);
        System.out.println("Ticks: " + this.ticks);
        System.out.println("Puntuación: " + this.puntuacion);
        
        if (this.jugador != null) {
            System.out.println("Jugador: " + this.jugador);
        }
        
        System.out.println("Enemigos (" + this.enemigos.size() + "):");
        for (Enemigo e : this.enemigos) {
            System.out.println("  - " + e);
        }
        
        System.out.println("Items (" + this.items.size() + "):");
        for (Item item : this.items) {
            if (!item.esRecolectado()) {
                System.out.println("  - " + item);
            }
        }
        
        System.out.println("Logros desbloqueados: " + this.sistemaLogros.getLogrosDesbloqueados());
        System.out.println("========================\n");
    }
}
