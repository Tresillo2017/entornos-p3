package com.dungeoncrawler.core;

/**
 * Clase responsable de procesar comandos del jugador.
 * Simula la entrada táctil/teclado traduciendo comandos textuales en acciones.
 * Métodos como pulsarBotonAccion(), desplazarEntidad() son llamados por Main
 * para simular input del usuario.
 */
public class GestorEntradas {
    private final MotorJuego motor;

    /**
     * Constructor del GestorEntradas.
     *
     * @param motor Referencia al motor para procesar comandos
     */
    public GestorEntradas(MotorJuego motor) {
        this.motor = motor;
    }

    /**
     * Procesa un movimiento del jugador.
     * Los movimientos válidos son: ARRIBA, ABAJO, IZQUIERDA, DERECHA.
     * Si el juego no está en estado JUGANDO, el movimiento se ignora.
     *
     * @param direccion Dirección del movimiento (case-insensitive)
     */
    public void desplazarEntidad(String direccion) {
        if (motor.getEstado() != MotorJuego.EstadoJuego.JUGANDO) {
            System.out.println("[INPUT] No se puede mover: el juego no está en estado JUGANDO");
            return;
        }

        Jugador jugador = motor.getJugador();
        if (jugador == null) {
            System.out.println("[INPUT] No hay jugador en el juego");
            return;
        }

        String dir = direccion.toUpperCase().trim();
        if (dir.matches("ARRIBA|ABAJO|IZQUIERDA|DERECHA")) {
            jugador.mover(dir);
            System.out.println("[INPUT] Jugador se movió " + dir + " a (" 
                    + jugador.getX() + ", " + jugador.getY() + ")");
        } else {
            System.out.println("[INPUT] Dirección inválida: " + direccion);
        }
    }

    /**
     * Procesa un botón de acción (ej: atacar, interactuar).
     * En este juego, la acción principal es atacar/interactuar con entidades cercanas.
     * Simula una acción en un rango cercano al jugador.
     */
    public void pulsarBotonAccion() {
        if (motor.getEstado() != MotorJuego.EstadoJuego.JUGANDO) {
            System.out.println("[INPUT] No se puede actuar: el juego no está en estado JUGANDO");
            return;
        }

        Jugador jugador = motor.getJugador();
        if (jugador == null) {
            System.out.println("[INPUT] No hay jugador en el juego");
            return;
        }

        System.out.println("[INPUT] Botón de acción pulsado por " + jugador.getNombre());
        // La lógica de colisión/interacción se maneja en MotorJuego.procesarColisiones()
    }

    /**
     * Simula daño recibido por el jugador (evento externo, ej: trampa).
     *
     * @param cantidad Daño a aplicar
     */
    public void recibirDanioJugador(int cantidad) {
        Jugador jugador = motor.getJugador();
        if (jugador != null && jugador.estaVivo()) {
            jugador.recibirDanio(cantidad);
            System.out.println("[INPUT] Jugador recibió " + cantidad 
                    + " daño. Vida restante: " + jugador.getVida());
        }
    }

    /**
     * Pausa el juego (solo si está en estado JUGANDO).
     */
    public void pausarJuego() {
        if (motor.getEstado() == MotorJuego.EstadoJuego.JUGANDO) {
            motor.setPausa(true);
            System.out.println("[INPUT] Juego pausado");
        }
    }

    /**
     * Reanuda el juego (solo si está en estado PAUSA).
     */
    public void reanudarJuego() {
        if (motor.getEstado() == MotorJuego.EstadoJuego.PAUSA) {
            motor.setPausa(false);
            System.out.println("[INPUT] Juego reanudado");
        }
    }
}
