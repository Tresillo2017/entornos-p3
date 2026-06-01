package com.dungeoncrawler.core;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gestiona el desbloqueo de logros durante la partida.
 * Comprueba condiciones de consecución y premia al jugador con XP extra.
 * AVANZADA 2 (segunda parte): Sistema de Logros/Recompensas.
 */
public class SistemaLogros {

    private static final int XP_ELIMINAR_TODOS  = 100;
    private static final int XP_RECOLECTAR_5    = 50;
    private static final int XP_NIVEL_10        = 200;
    private static final int XP_SUPERVIVIR_10   = 75;

    private final Set<String> logrosDesbloqueados;
    private int monedasRecolectadas;

    public SistemaLogros() {
        this.logrosDesbloqueados = new LinkedHashSet<>();
        this.monedasRecolectadas = 0;
    }

    /** Notifica que se recogió una moneda para el seguimiento del logro correspondiente. */
    public void registrarMonedaRecolectada() {
        monedasRecolectadas++;
    }

    /**
     * Evalúa las condiciones de logro en cada tick del juego.
     * Si se cumple una condición nueva, desbloquea el logro y premia al jugador.
     *
     * @param jugador   Referencia al jugador para conceder XP
     * @param enemigosVivos Número de enemigos todavía vivos
     * @param ticks     Ticks transcurridos desde el inicio de la partida
     */
    public void verificarLogros(Jugador jugador, int enemigosVivos, long ticks) {
        if (jugador == null) return;

        if (enemigosVivos == 0) {
            desbloquear("ELIMINAR_TODOS_ENEMIGOS", jugador, XP_ELIMINAR_TODOS);
        }
        if (monedasRecolectadas >= 5) {
            desbloquear("RECOLECTAR_5_MONEDAS", jugador, XP_RECOLECTAR_5);
        }
        if (jugador.getNivel() >= 10) {
            desbloquear("NIVEL_10", jugador, XP_NIVEL_10);
        }
        if (ticks >= 10) {
            desbloquear("SUPERVIVIR_10_TURNOS", jugador, XP_SUPERVIVIR_10);
        }
    }

    /** @return copia del conjunto de logros ya desbloqueados */
    public Set<String> getLogrosDesbloqueados() {
        return new LinkedHashSet<>(logrosDesbloqueados);
    }

    private void desbloquear(String id, Jugador jugador, int xpBonus) {
        if (!logrosDesbloqueados.contains(id)) {
            logrosDesbloqueados.add(id);
            jugador.ganarExperiencia(xpBonus);
            System.out.println("[LOGRO] ¡Logro desbloqueado: " + id
                    + "! +" + xpBonus + " XP");
        }
    }
}
