package com.dungeoncrawler.core;

/**
 * Representa a un Enemigo con IA básica de tres estados.
 * AVANZADA 2 (primera parte): Comportamiento NPC del Enemigo.
 *
 * Transiciones de estado:
 *   distancia = 0 o 1  →  ATACAR
 *   distancia ≤ rango   →  PERSEGUIR
 *   distancia > rango   →  PATRULLAR
 */
public class Enemigo extends EntidadVideojuego {

    public enum Estado {
        PATRULLAR, PERSEGUIR, ATACAR
    }

    private static final int LIMITE_MAPA      = 20;
    private static final int COOLDOWN_ATAQUE  = 2;  // turnos entre ataques

    private Estado estadoIA;
    private final int rangoDeteccion;
    private final int danioAtaque;
    private int turnoUltimoAtaque;

    /**
     * @param nombre         Identificador (ej: "Goblin_1")
     * @param x              Posición inicial X
     * @param y              Posición inicial Y
     * @param vida           Puntos de vida
     * @param danioAtaque    Daño infligido por ataque
     * @param rangoDeteccion Distancia Manhattan máxima para detectar al jugador
     */
    public Enemigo(String nombre, int x, int y, int vida,
                   int danioAtaque, int rangoDeteccion) {
        super(nombre, "ENEMIGO", x, y, 1, 1, vida, "enemy_idle");
        this.estadoIA = Estado.PATRULLAR;
        this.danioAtaque = danioAtaque;
        this.rangoDeteccion = rangoDeteccion;
        this.turnoUltimoAtaque = -COOLDOWN_ATAQUE;
    }

    public Estado getEstadoIA() {
        return estadoIA;
    }

    public int getRangoDeteccion() {
        return rangoDeteccion;
    }

    public int getDanioAtaque() {
        return danioAtaque;
    }

    @Override
    public void actualizar(MotorJuego motor) {
        if (!estaVivo()) return;

        Jugador jugador = motor.getJugador();
        if (jugador == null || !jugador.estaVivo()) return;

        int distancia = distanciaA(jugador.getX(), jugador.getY());
        int tickActual = (int) motor.getTicks();

        if (distancia <= 1) {
            estadoIA = Estado.ATACAR;
            atacarJugador(jugador, tickActual);
        } else if (distancia <= rangoDeteccion) {
            estadoIA = Estado.PERSEGUIR;
            perseguirJugador(jugador);
        } else {
            estadoIA = Estado.PATRULLAR;
            patrullar();
        }
    }

    // ── Comportamientos ───────────────────────────────────────────────────────

    private void patrullar() {
        int[] delta = {-1, 0, 1};
        int dx = delta[(int) (Math.random() * delta.length)];
        int dy = delta[(int) (Math.random() * delta.length)];

        int nx = Math.max(0, Math.min(LIMITE_MAPA, getX() + dx));
        int ny = Math.max(0, Math.min(LIMITE_MAPA, getY() + dy));

        if (nx != getX() || ny != getY()) {
            setX(nx);
            setY(ny);
            System.out.println("[IA] " + getNombre() + " patrulla a (" + nx + "," + ny + ")");
        }
    }

    private void perseguirJugador(Jugador jugador) {
        int dx = Integer.compare(jugador.getX(), getX());
        int dy = Integer.compare(jugador.getY(), getY());

        int nx = Math.max(0, Math.min(LIMITE_MAPA, getX() + dx));
        int ny = Math.max(0, Math.min(LIMITE_MAPA, getY() + dy));

        setX(nx);
        setY(ny);
        System.out.println("[IA] " + getNombre() + " persigue al jugador → (" + nx + "," + ny + ")");
    }

    private void atacarJugador(Jugador jugador, int tickActual) {
        if (tickActual - turnoUltimoAtaque >= COOLDOWN_ATAQUE) {
            jugador.recibirDanio(danioAtaque);
            turnoUltimoAtaque = tickActual;
            System.out.println("[IA] " + getNombre() + " ataca a " + jugador.getNombre()
                    + " (" + danioAtaque + " daño). Vida jugador: " + jugador.getVida());
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private int distanciaA(int tx, int ty) {
        return Math.abs(getX() - tx) + Math.abs(getY() - ty);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", estado=%s, danio=%d, rango=%d",
                estadoIA, danioAtaque, rangoDeteccion);
    }
}
