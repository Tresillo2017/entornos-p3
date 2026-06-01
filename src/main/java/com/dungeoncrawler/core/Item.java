package com.dungeoncrawler.core;

/**
 * Clase que representa items/objetos recolectables en el juego.
 * Hereda de EntidadVideojuego.
 * Ejemplos: monedas (Coin), pociones de vida (HealthPotion), llaves, etc.
 * 
 * Un item tiene:
 * - Tipo específico (MONEDA, POCION, LLAVE)
 * - Un valor asociado (ej: cantidad de oro, puntos de vida a recuperar)
 * - Estado de "recolectado" (si fue eliminado del mapa)
 */
public class Item extends EntidadVideojuego {
    public enum TipoItem {
        MONEDA, POCION, LLAVE
    }

    private final TipoItem tipoItem;
    private final int valor; // Para monedas: oro; para pociones: vida recuperada
    private boolean recolectado;

    /**
     * Constructor de Item.
     *
     * @param nombre Identificador único (ej: "Moneda_1", "Pocion_Health_5")
     * @param tipoItem Tipo específico (MONEDA, POCION, LLAVE)
     * @param x Posición X
     * @param y Posición Y
     * @param valor Valor asociado (oro, puntos de vida, etc.)
     */
    public Item(String nombre, TipoItem tipoItem, int x, int y, int valor) {
        super(nombre, "ITEM", x, y, 1, 1, 1, "item_" + tipoItem.name().toLowerCase());
        this.tipoItem = tipoItem;
        this.valor = valor;
        this.recolectado = false;
    }

    public TipoItem getTipoItem() {
        return tipoItem;
    }

    public int getValor() {
        return valor;
    }

    public boolean esRecolectado() {
        return recolectado;
    }

    /**
     * Marca el item como recolectado.
     * Después de esto, debe ser removido del juego.
     */
    public void recolectar() {
        this.recolectado = true;
        // Items no tienen actualización automática
    }

    /**
     * Items no tienen comportamiento autónomo.
     */
    @Override
    public void actualizar(MotorJuego motor) {
        // Sin lógica automática
    }

    @Override
    public String toString() {
        return String.format("Item[nombre=%s, tipo=%s, pos=(%d,%d), valor=%d, recolectado=%s]",
                this.getNombre(), tipoItem, this.getX(), this.getY(), valor, recolectado);
    }
}
