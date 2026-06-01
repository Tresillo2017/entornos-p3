package com.dungeoncrawler.core;

/**
 * Representa un item coleccionable en el mapa del juego.
 * Al ocupar la misma casilla que el jugador, aplica su efecto y desaparece.
 * Clase extra opcional para la funcionalidad avanzada de colisiones con items.
 */
public class Item extends EntidadVideojuego {

    public enum TipoItem {
        MONEDA, POCION
    }

    private final TipoItem tipoItem;
    private final int valor;
    private boolean recolectado;

    /**
     * @param nombre     Identificador del item (ej: "Moneda_1")
     * @param x          Posición X en el mapa
     * @param y          Posición Y en el mapa
     * @param tipo       MONEDA (suma puntos) o POCION (restaura vida)
     * @param valor      Puntos a sumar o puntos de vida a restaurar
     */
    public Item(String nombre, int x, int y, TipoItem tipo, int valor) {
        super(nombre, "ITEM", x, y, 1, 1, 1,
                tipo == TipoItem.MONEDA ? "coin_sprite" : "potion_sprite");
        this.tipoItem = tipo;
        this.valor = valor;
        this.recolectado = false;
    }

    public TipoItem getTipoItem() {
        return tipoItem;
    }

    public int getValor() {
        return valor;
    }

    public boolean isRecolectado() {
        return recolectado;
    }

    /** Marca el item como recogido. Tras esto, estaVivo() retorna false. */
    public void recolectar() {
        recolectado = true;
    }

    /** Un item "muere" al ser recolectado, no cuando su vida llega a 0. */
    @Override
    public boolean estaVivo() {
        return !recolectado;
    }

    @Override
    public void actualizar(MotorJuego motor) {
        // Los items son pasivos; la recolección ocurre en MotorJuego.procesarColisiones()
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", tipo=%s, valor=%d, recolectado=%b",
                tipoItem, valor, recolectado);
    }
}
