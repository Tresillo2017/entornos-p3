package com.dungeoncrawler.core;

/**
 * Clase abstracta base para todas las entidades del juego.
 * Define los atributos y comportamientos comunes: posición, tamaño, vida y estado.
 * 
 * Precondición: x, y, w, h deben ser no-negativos.
 * Invariante: vida >= 0.
 */
public abstract class EntidadVideojuego {
    // Atributos privados
    private final String nombre;
    private final String tipo;
    private int x;
    private int y;
    private final int w;
    private final int h;
    private int vida;
    private final int vidaMaxima;
    private String sprite; // Descripción de sprite/animación para UI futura

    /**
     * Constructor protegido para subclases.
     *
     * @param nombre Identificador de la entidad (ej: "Adventurer", "Goblin_1")
     * @param tipo Categoría (ej: "JUGADOR", "ENEMIGO", "ITEM")
     * @param x Posición X
     * @param y Posición Y
     * @param w Ancho de la entidad
     * @param h Alto de la entidad
     * @param vida Puntos de vida iniciales
     * @param sprite Referencia a sprite/animación
     */
    protected EntidadVideojuego(String nombre, String tipo, int x, int y, 
                               int w, int h, int vida, String sprite) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.sprite = sprite;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public String getSprite() {
        return sprite;
    }

    // Setters controlados
    public void setX(int x) {
        if (x >= 0) {
            this.x = x;
        }
    }

    public void setY(int y) {
        if (y >= 0) {
            this.y = y;
        }
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    /**
     * Reduce la vida de la entidad.
     * Simula daño recibido (ej: colisión con enemigo, impacto de ataque).
     *
     * @param danio Cantidad de daño a aplicar (positivo)
     */
    public void recibirDanio(int danio) {
        if (danio > 0) {
            this.vida = Math.max(0, this.vida - danio);
        }
    }

    /**
     * Restaura vida (ej: consumir poción).
     *
     * @param curacion Cantidad a restaurar
     */
    public void curar(int curacion) {
        if (curacion > 0) {
            this.vida = Math.min(this.vidaMaxima, this.vida + curacion);
        }
    }

    /**
     * Verifica si la entidad está viva.
     *
     * @return true si vida > 0
     */
    public boolean estaVivo() {
        return this.vida > 0;
    }

    /**
     * Método abstracto para comportamiento por tick (actualización por frame).
     * Subclases implementarán la lógica de movimiento, ataque, IA, etc.
     *
     * @param motor Referencia al motor para consultar estado del juego
     */
    public abstract void actualizar(MotorJuego motor);

    @Override
    public String toString() {
        return String.format("%s[nombre=%s, pos=(%d,%d), vida=%d/%d, sprite=%s]",
                this.getClass().getSimpleName(), nombre, x, y, vida, vidaMaxima, sprite);
    }
}
