package com.dungeoncrawler.core;

import java.util.*;

/**
 * AVANZADA 2: Sistema de Logros/Recompensas
 * 
 * Gestiona condiciones de logros (ej: "Eliminar 3 enemigos", "Recolectar 5 monedas")
 * y dispara eventos cuando se desbloquean.
 * 
 * Logros soportados:
 * - ELIMINAR_TODOS_ENEMIGOS: Se dispara cuando todos los enemigos mueren
 * - RECOLECTAR_5_MONEDAS: Se dispara cuando se han recolectado 5+ monedas
 * - NIVEL_10: Se dispara cuando el jugador llega a nivel 10
 */
public class SistemaLogros {
    
    public static class Logro {
        public final String id;
        public final String descripcion;
        public final int recompensa; // Puntos de experiencia

        public Logro(String id, String descripcion, int recompensa) {
            this.id = id;
            this.descripcion = descripcion;
            this.recompensa = recompensa;
        }

        @Override
        public String toString() {
            return String.format("Logro[%s: %s, +%d XP]", id, descripcion, recompensa);
        }
    }

    // Catálogo de logros disponibles
    private static final Map<String, Logro> LOGROS_DISPONIBLES = new LinkedHashMap<>();
    static {
        LOGROS_DISPONIBLES.put("ELIMINAR_TODOS_ENEMIGOS", 
            new Logro("ELIMINAR_TODOS_ENEMIGOS", "Derrota a todos los enemigos", 100));
        LOGROS_DISPONIBLES.put("RECOLECTAR_5_MONEDAS", 
            new Logro("RECOLECTAR_5_MONEDAS", "Recolecta 5 monedas", 50));
        LOGROS_DISPONIBLES.put("NIVEL_10", 
            new Logro("NIVEL_10", "Alcanza nivel 10", 200));
        LOGROS_DISPONIBLES.put("SUPERVIVIR_10_TURNOS", 
            new Logro("SUPERVIVIR_10_TURNOS", "Sobrevive 10 ticks sin morir", 75));
    }

    // Logros desbloqueados en la partida actual
    private Set<String> logrosDesbloqueados;
    
    // Contadores para logros condicionales
    private Map<String, Integer> contadores;

    public SistemaLogros() {
        this.logrosDesbloqueados = new HashSet<>();
        this.contadores = new HashMap<>();
    }

    /**
     * Reinicia el sistema para una nueva partida.
     */
    public void reiniciar() {
        this.logrosDesbloqueados.clear();
        this.contadores.clear();
    }

    /**
     * Incrementa un contador que se usa para verificar logros.
     *
     * @param contador Nombre del contador (ej: "MONEDAS_RECOLECTADAS")
     * @param valor Cantidad a incrementar
     */
    public void incrementarContador(String contador, int valor) {
        this.contadores.put(contador, this.contadores.getOrDefault(contador, 0) + valor);
        System.out.println("[LOGROS] Contador '" + contador + "' = " 
                + this.contadores.get(contador));
    }

    /**
     * Verifica si se ha cumplido una condición de logro.
     * Si es así, lo desbloquea y lo anota.
     *
     * @param idLogro ID del logro a verificar
     * @return true si se desbloqueó en esta llamada
     */
    public boolean verificarLogro(String idLogro) {
        if (this.logrosDesbloqueados.contains(idLogro)) {
            return false; // Ya estaba desbloqueado
        }

        boolean desbloqueado = false;
        
        switch (idLogro) {
            case "RECOLECTAR_5_MONEDAS":
                if (this.contadores.getOrDefault("MONEDAS_RECOLECTADAS", 0) >= 5) {
                    desbloqueado = true;
                }
                break;
                
            case "NIVEL_10":
                // Se verifica desde el contexto del jugador
                desbloqueado = true;
                break;
                
            case "SUPERVIVIR_10_TURNOS":
                // Se verifica desde el motor
                desbloqueado = true;
                break;
                
            case "ELIMINAR_TODOS_ENEMIGOS":
                desbloqueado = true;
                break;
        }

        if (desbloqueado) {
            desbloquearLogro(idLogro);
            return true;
        }

        return false;
    }

    /**
     * Desbloquea un logro y muestra un mensaje.
     */
    private void desbloquearLogro(String idLogro) {
        this.logrosDesbloqueados.add(idLogro);
        
        Logro logro = LOGROS_DISPONIBLES.get(idLogro);
        if (logro != null) {
            System.out.println("\n*** LOGRO DESBLOQUEADO ***");
            System.out.println("  " + logro);
            System.out.println("**********************\n");
        }
    }

    /**
     * Obtiene la lista de logros desbloqueados.
     */
    public Set<String> getLogrosDesbloqueados() {
        return new HashSet<>(this.logrosDesbloqueados);
    }

    /**
     * Obtiene un valor de un contador.
     */
    public int getContador(String contador) {
        return this.contadores.getOrDefault(contador, 0);
    }

    /**
     * Devuelve información de un logro específico.
     */
    public Logro obtenerLogro(String idLogro) {
        return LOGROS_DISPONIBLES.get(idLogro);
    }
}
