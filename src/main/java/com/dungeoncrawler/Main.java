package com.dungeoncrawler;

import com.dungeoncrawler.core.*;

/**
 * Clase Main: Conductor del juego.
 * Simula un bucle de juego básico y entra de usuario por consola.
 * Permite:
 * - Iniciar/pausar/reanudar el juego
 * - Mover al jugador
 * - Pulsar botones de acción
 * - Ver estado actual
 * 
 * Todo se prueba por consola en esta clase.
 */
public class Main {
    
    private static MotorJuego motor;
    private static GestorEntradas input;
    
    public static void main(String[] args) {
        System.out.println("=== DUNGEON CRAWLER 2D - MOTOR DE JUEGO ===");
        System.out.println("Simulación del núcleo de un videojuego 2D por consola\n");
        
        // Inicializar motor
        motor = new MotorJuego();
        input = new GestorEntradas(motor);
        
        // Menú principal
        menuPrincipal();
    }

    /**
     * Menú principal del juego.
     */
    private static void menuPrincipal() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Iniciar Nueva Partida");
        System.out.println("2. Salir");
        System.out.print("Selecciona opción: ");
        
        String opcion = readInput();
        
        switch (opcion.trim()) {
            case "1":
                iniciarJuego();
                break;
            case "2":
                System.out.println("¡Hasta luego!");
                System.exit(0);
                break;
            default:
                System.out.println("Opción no válida");
                menuPrincipal();
        }
    }

    /**
     * Flujo principal del juego una vez iniciado.
     */
    private static void iniciarJuego() {
        motor.iniciarPartida();
        bucleJuego();
    }

    /**
     * Bucle de juego: actualiza el motor y procesa comandos del usuario.
     */
    private static void bucleJuego() {
        System.out.println("\n--- COMENZÓ LA PARTIDA ---");
        System.out.println("Comandos disponibles:");
        System.out.println("  ARRIBA/ABAJO/IZQUIERDA/DERECHA - Mover jugador");
        System.out.println("  ACCION - Pulsar botón de acción");
        System.out.println("  PAUSA - Pausar/Reanudar");
        System.out.println("  ESTADO - Ver estado actual");
        System.out.println("  MENU - Volver al menú principal");
        
        while (true) {
            // Actualizar motor (simula un tick del juego)
            motor.actualizar();
            
            // Verificar condiciones de fin de juego
            if (motor.getEstado() == MotorJuego.EstadoJuego.GAME_OVER) {
                finPartida();
                break;
            }
            
            // Mostrar prompt y leer comando
            System.out.print("\n> ");
            String comando = readInput().trim().toUpperCase();
            
            procesarComando(comando);
        }
    }

    /**
     * Procesa los comandos del usuario.
     */
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
                System.out.println("Comando no reconocido: " + comando);
        }
    }

    /**
     * Pantalla de fin de partida.
     */
    private static void finPartida() {
        Jugador jugador = motor.getJugador();
        System.out.println("\n--- FIN DE LA PARTIDA ---");
        
        if (jugador != null && jugador.estaVivo()) {
            System.out.println("¡VICTORIA!");
            System.out.println("Jugador: " + jugador.getNombre());
            System.out.println("Vida final: " + jugador.getVida());
            System.out.println("Nivel: " + jugador.getNivel());
            System.out.println("Experiencia: " + jugador.getExperiencia());
        } else {
            System.out.println("DERROTA. El jugador ha sido eliminado.");
        }
        
        System.out.println("Puntuación final: " + motor.getPuntuacion());
        System.out.println("Ticks sobrevividos: " + motor.getTicks());
        System.out.println("Logros desbloqueados: " + motor.getSistemaLogros().getLogrosDesbloqueados().size());
        
        System.out.println("\nVolviendo al menú principal...");
        menuPrincipal();
    }

    /**
     * Lee una línea de entrada desde la consola.
     */
    private static String readInput() {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in));
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
