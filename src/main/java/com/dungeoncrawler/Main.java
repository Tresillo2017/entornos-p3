package com.dungeoncrawler;

import com.dungeoncrawler.core.GestorEntradas;
import com.dungeoncrawler.core.Jugador;
import com.dungeoncrawler.core.MotorJuego;
import java.util.Scanner;

/**
 * Clase conductora: simula el bucle de juego y la entrada del usuario por consola.
 * Punto de entrada de la aplicación.
 */
public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);

    private static MotorJuego motor;
    private static GestorEntradas input;

    public static void main(String[] args) {
        System.out.println("=== DUNGEON CRAWLER 2D - MOTOR DE JUEGO ===");
        System.out.println("Simulación del núcleo de un videojuego 2D por consola\n");

        motor = new MotorJuego();
        input = new GestorEntradas(motor);

        menuPrincipal();
    }

    private static void menuPrincipal() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Iniciar Nueva Partida");
        System.out.println("2. Salir");
        System.out.print("Selecciona opción: ");

        String opcion = leerLinea().trim();

        switch (opcion) {
            case "1":
                motor.iniciarPartida();
                bucleJuego();
                break;
            case "2":
                System.out.println("¡Hasta luego!");
                SCANNER.close();
                System.exit(0);
                break;
            default:
                System.out.println("Opción no válida. Intenta de nuevo.");
                menuPrincipal();
        }
    }

    private static void bucleJuego() {
        System.out.println("\n--- COMENZÓ LA PARTIDA ---");
        System.out.println("Comandos: ARRIBA | ABAJO | IZQUIERDA | DERECHA | ACCION | PAUSA | ESTADO | MENU\n");

        while (true) {
            motor.actualizar();

            MotorJuego.EstadoJuego estadoActual = motor.getEstado();
            if (estadoActual == MotorJuego.EstadoJuego.GAME_OVER
                    || estadoActual == MotorJuego.EstadoJuego.VICTORIA) {
                mostrarFinPartida();
                break;
            }

            if (estadoActual == MotorJuego.EstadoJuego.PAUSA) {
                System.out.println("[PAUSA] El juego está pausado. Escribe PAUSA para reanudar.");
            }

            System.out.print("> ");
            String comando = leerLinea().trim().toUpperCase();
            procesarComando(comando);
        }
    }

    private static void procesarComando(String comando) {
        switch (comando) {
            case "ARRIBA":
            case "ABAJO":
            case "IZQUIERDA":
            case "DERECHA":
                input.desplazarEntidad(comando);
                break;
            case "ACCION":
                input.pulsarBotonAccion();
                break;
            case "PAUSA":
                if (motor.getEstado() == MotorJuego.EstadoJuego.JUGANDO) {
                    input.pausarJuego();
                } else if (motor.getEstado() == MotorJuego.EstadoJuego.PAUSA) {
                    input.reanudarJuego();
                }
                break;
            case "ESTADO":
                motor.mostrarEstado();
                break;
            case "MENU":
                System.out.println("Volviendo al menú principal...");
                menuPrincipal();
                break;
            default:
                System.out.println("Comando no reconocido: '" + comando + "'");
        }
    }

    private static void mostrarFinPartida() {
        Jugador jugador = motor.getJugador();
        System.out.println("\n╔══════════════════════════╗");

        if (motor.getEstado() == MotorJuego.EstadoJuego.VICTORIA) {
            System.out.println("║        ¡VICTORIA!        ║");
        } else {
            System.out.println("║        GAME OVER         ║");
            System.out.println("║  El jugador fue derrotado║");
        }
        System.out.println("╚══════════════════════════╝");

        if (jugador != null) {
            System.out.println("Jugador:      " + jugador.getNombre());
            System.out.println("Vida final:   " + jugador.getVida() + "/" + jugador.getVidaMaxima());
            System.out.println("Nivel:        " + jugador.getNivel());
            System.out.println("Experiencia:  " + jugador.getExperiencia());
        }
        System.out.println("Puntuación:   " + motor.getPuntuacion());
        System.out.println("Turnos:       " + motor.getTicks());
        System.out.println("Enemigos res: " + motor.getEnemigos().size());

        if (motor.getSistemaLogros() != null
                && !motor.getSistemaLogros().getLogrosDesbloqueados().isEmpty()) {
            System.out.println("Logros:       " + motor.getSistemaLogros().getLogrosDesbloqueados());
        }

        System.out.println();
        menuPrincipal();
    }

    private static String leerLinea() {
        return SCANNER.hasNextLine() ? SCANNER.nextLine() : "";
    }
}
