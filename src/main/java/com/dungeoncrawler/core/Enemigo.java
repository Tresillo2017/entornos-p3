package com.dungeoncrawler.core;

/**
 * Clase que representa a un Enemigo en el juego.
 * Hereda de EntidadVideojuego e implementa IA básica con 3 estados:
 * - PATRULLAR: Se mueve aleatoriamente dentro de un área
 * - PERSEGUIR: Sigue al jugador si está en rango de detección
 * - ATACAR: Está adyacente al jugador (simplemente reduce su vida)
 * 
 * AVANZADA 2: Comportamiento NPC del Enemigo.
 */
public class Enemigo extends EntidadVideojuego {
    public enum Estado {
        PATRULLAR, PERSEGUIR, ATACAR
    }

    private Estado estado;
    private int rangoDeteccion;
    private int danioAtaque;
    private long ultimoAtaque;

    /**
     * Constructor de Enemigo.
     *
     * @param nombre Identificador del enemigo (ej: "Goblin_1")
     * @param x Posición inicial X
     * @param y Posición inicial Y
     * @param vida Puntos de vida
     * @param danioAtaque Daño que inflige por ataque
     * @param rangoDeteccion Distancia a la que detecta al jugador
     */
    public Enemigo(String nombre, int x, int y, int vida, int danioAtaque, int rangoDeteccion) {
        super(nombre, "ENEMIGO", x, y, 1, 1, vida, "goblin_idle");
        this.estado = Estado.PATRULLAR;
        this.danioAtaque = danioAtaque;
        this.rangoDeteccion = rangoDeteccion;
        this.ultimoAtaque = 0;
    }

    public Estado getEstado() {
        return estado;
    }

    public int getRangoDeteccion() {
        return rangoDeteccion;
    }

    public int getDanioAtaque() {
        return danioAtaque;
    }

    private int distanciaA(int x, int y) {
        return Math.abs(this.getX() - x) + Math.abs(this.getY() - y);
    }

    @Override
    public void actualizar(MotorJuego motor) {
        if (!this.estaVivo()) {
            return;
        }

        Jugador jugador = motor.getJugador();
        if (jugador == null || !jugador.estaVivo()) {
            return;
        }

        int distancia = distanciaA(jugador.getX(), jugador.getY());

        if (distancia <= 1) {
            estado = Estado.ATACAR;
            atacarJugador(jugador);
        } else if (distancia <= rangoDeteccion) {
            estado = Estado.PERSEGUIR;
            perseguirJugador(jugador);
        } else {
            estado = Estado.PATRULLAR;
            patrullar();
        }
    }

    private void patrullar() {
        int[] direcciones = {-1, 0, 1};
        int dx = direcciones[(int) (Math.random() * 3)];
        int dy = direcciones[(int) (Math.random() * 3)];

        int nuevoX = Math.max(0, Math.min(20, this.getX() + dx));
        int nuevoY = Math.max(0, Math.min(20, this.getY() + dy));

        this.setX(nuevoX);
        this.setY(nuevoY);
    }

    private void perseguirJugador(Jugador jugador) {
        int dx = 0;
        int dy = 0;

        if (this.getX() < jugador.getX()) {
            dx = 1;
        } else if (this.getX() > jugador.getX()) {
            dx = -1;
        }

        if (this.getY() < jugador.getY()) {
            dy = 1;
        } else if (this.getY() > jugador.getY()) {
            dy = -1;
        }

        int nuevoX = Math.max(0, Math.min(20, this.getX() + dx));
        int nuevoY = Math.max(0, Math.min(20, this.getY() + dy));

        this.setX(nuevoX);
        this.setY(nuevoY);
    }

    private void atacarJugador(Jugador jugador) {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimoAtaque >= 2000) {
            jugador.recibirDanio(danioAtaque);
            ultimoAtaque = ahora;
            System.out.println("[LOG] " + this.getNombre() + " ataca a " + jugador.getNombre() 
                    + " causando " + danioAtaque + " daño. Vida jugador: " + jugador.getVida());
        }
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", estado=%s, danio=%d, rango=%d",
                estado, danioAtaque, rangoDeteccion);
    }
}
