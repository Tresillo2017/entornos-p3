package com.dungeoncrawler.core;

/**
 * Clase que representa al jugador (Adventurer) en el juego.
 * Hereda de EntidadVideojuego y añade atributos específicos del jugador:
 * - Puntos de experiencia
 * - Nivel
 * - Inventario básico (para futuras expansiones)
 */
public class Jugador extends EntidadVideojuego {
    private int experiencia;
    private int nivel;

    /**
     * Constructor del Jugador.
     *
     * @param nombre Nombre del aventurero (típicamente "Adventurer")
     * @param x Posición inicial X
     * @param y Posición inicial Y
     * @param vida Puntos de vida iniciales
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

    public void ganarExperiencia(int exp) {
        if (exp > 0) {
            this.experiencia += exp;
            // Lógica simple: cada 100 exp = 1 nivel
            int nivelAnterior = this.nivel;
            this.nivel = 1 + (this.experiencia / 100);
            if (this.nivel > nivelAnterior) {
                System.out.println("[LOG] " + this.getNombre() + " ascendió a nivel " + this.nivel);
            }
        }
    }

    /**
     * Verifica si el jugador está vivo.
     * @return true si vida > 0
     */
    public boolean estaVivo() {
        return this.getVida() > 0;
    }

    /**
     * Implementa movimiento del jugador.
     * En el juego real, estos movimientos serían procesados por el InputManager.
     * Para el test, se llama manualmente.
     */
    @Override
    public void actualizar(MotorJuego motor) {
        // El jugador no tiene comportamiento automático por IA
        // Se actualiza mediante comandos del InputManager
        // Podrían aquí regenerarse puntos especiales, efectos de estado, etc.
    }

    /**
     * Mueve al jugador en una dirección (si es válida).
     *
     * @param direccion "ARRIBA", "ABAJO", "IZQUIERDA", "DERECHA"
     */
    public void mover(String direccion) {
        int nuevoX = this.getX();
        int nuevoY = this.getY();

        switch (direccion.toUpperCase()) {
            case "ARRIBA":
                nuevoY = Math.max(0, this.getY() - 1);
                break;
            case "ABAJO":
                nuevoY = Math.min(20, this.getY() + 1); // Límite de mapa
                break;
            case "IZQUIERDA":
                nuevoX = Math.max(0, this.getX() - 1);
                break;
            case "DERECHA":
                nuevoX = Math.min(20, this.getX() + 1); // Límite de mapa
                break;
        }

        this.setX(nuevoX);
        this.setY(nuevoY);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", exp=%d, nivel=%d",
                experiencia, nivel);
    }
}
