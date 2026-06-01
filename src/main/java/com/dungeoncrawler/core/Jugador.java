package com.dungeoncrawler.core;

/**
 * Representa al jugador (Adventurer) en el juego.
 * Hereda de EntidadVideojuego y añade experiencia, nivel y movimiento controlado.
 */
public class Jugador extends EntidadVideojuego {

    private static final int LIMITE_MAPA    = 20;
    private static final int EXP_POR_NIVEL  = 100;

    private int experiencia;
    private int nivel;

    /**
     * @param nombre Nombre del aventurero (ej: "Adventurer")
     * @param x      Posición inicial X
     * @param y      Posición inicial Y
     * @param vida   Puntos de vida iniciales
     */
    public Jugador(String nombre, int x, int y, int vida) {
        super(nombre, "JUGADOR", x, y, 1, 1, vida, "adventurer_idle");
        this.experiencia = 0;
        this.nivel = 1;
    }

    public int getExperiencia() {
        return experiencia;
    }

    public int getNivel() {
        return nivel;
    }

    /**
     * Suma experiencia y recalcula el nivel.
     * Un nivel se obtiene cada {@value #EXP_POR_NIVEL} puntos de experiencia.
     */
    public void ganarExperiencia(int exp) {
        if (exp <= 0) return;
        experiencia += exp;
        int nivelAnterior = nivel;
        nivel = 1 + (experiencia / EXP_POR_NIVEL);
        if (nivel > nivelAnterior) {
            System.out.println("[LOG] " + getNombre() + " ascendió al nivel " + nivel);
        }
    }

    /**
     * Desplaza al jugador una casilla en la dirección indicada,
     * respetando los límites del mapa (0 – {@value #LIMITE_MAPA}).
     *
     * @param direccion "ARRIBA", "ABAJO", "IZQUIERDA" o "DERECHA"
     */
    public void mover(String direccion) {
        int nx = getX();
        int ny = getY();

        switch (direccion.toUpperCase()) {
            case "ARRIBA":    ny = Math.max(0, ny - 1);          break;
            case "ABAJO":     ny = Math.min(LIMITE_MAPA, ny + 1); break;
            case "IZQUIERDA": nx = Math.max(0, nx - 1);          break;
            case "DERECHA":   nx = Math.min(LIMITE_MAPA, nx + 1); break;
        }

        setX(nx);
        setY(ny);
    }

    @Override
    public void actualizar(MotorJuego motor) {
        // El jugador no tiene comportamiento automático; reacciona a comandos del GestorEntradas
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", exp=%d, nivel=%d", experiencia, nivel);
    }
}
