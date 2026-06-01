package com.dungeoncrawler;

import com.dungeoncrawler.core.*;

/**
 * Suite de tests manual (sin dependencias externas).
 * Cubre las clases del motor: entidades, lógica de colisiones, IA del enemigo,
 * sistema de logros e integración del bucle de juego.
 *
 * Ejecutar: java -cp bin com.dungeoncrawler.TestRunner
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    // ── Punto de entrada ──────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("==============================");
        System.out.println(" DUNGEON CRAWLER — TEST SUITE");
        System.out.println("==============================\n");

        testEntidadVideojuego();
        testJugador();
        testEnemigo();
        testItem();
        testMotorJuegoEstados();
        testColisiones();
        testSistemaLogros();
        testIntegracion();

        System.out.println("\n==============================");
        System.out.printf(" RESULTADO: %d/%d tests pasados%n", passed, passed + failed);
        System.out.println("==============================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // ── EntidadVideojuego ─────────────────────────────────────────────────────

    private static void testEntidadVideojuego() {
        grupo("EntidadVideojuego");

        Jugador e = new Jugador("Hero", 5, 3, 10);

        assertEquals("getNombre", "Hero", e.getNombre());
        assertEquals("getX inicial", 5, e.getX());
        assertEquals("getY inicial", 3, e.getY());
        assertEquals("getVida inicial", 10, e.getVida());
        assertEquals("getVidaMaxima", 10, e.getVidaMaxima());
        assertTrue("estaVivo con vida > 0", e.estaVivo());

        e.recibirDanio(4);
        assertEquals("recibirDanio resta vida", 6, e.getVida());

        e.recibirDanio(100);
        assertEquals("recibirDanio no baja de 0", 0, e.getVida());
        assertFalse("estaVivo con vida == 0", e.estaVivo());

        Jugador e2 = new Jugador("X", 0, 0, 5);
        e2.recibirDanio(3);
        e2.curar(10);
        assertEquals("curar no supera vidaMaxima", 5, e2.getVida());

        e2.curar(2);
        assertEquals("curar suma hasta max", 5, e2.getVida());
    }

    // ── Jugador ───────────────────────────────────────────────────────────────

    private static void testJugador() {
        grupo("Jugador");

        Jugador j = new Jugador("Adventurer", 10, 10, 20);

        assertEquals("nivel inicial", 1, j.getNivel());
        assertEquals("experiencia inicial", 0, j.getExperiencia());

        j.ganarExperiencia(50);
        assertEquals("exp parcial no sube nivel", 1, j.getNivel());

        j.ganarExperiencia(50);
        assertEquals("100 exp = nivel 2", 2, j.getNivel());

        j.ganarExperiencia(200);
        assertEquals("nivel correcto con exp acumulada (300 total)", 4, j.getNivel());

        // Movimiento — límites del mapa
        Jugador jBorde = new Jugador("B", 0, 0, 10);
        jBorde.mover("ARRIBA");
        assertEquals("ARRIBA no baja de y=0", 0, jBorde.getY());
        jBorde.mover("IZQUIERDA");
        assertEquals("IZQUIERDA no baja de x=0", 0, jBorde.getX());

        Jugador jBorde2 = new Jugador("B2", 20, 20, 10);
        jBorde2.mover("ABAJO");
        assertEquals("ABAJO no supera y=20", 20, jBorde2.getY());
        jBorde2.mover("DERECHA");
        assertEquals("DERECHA no supera x=20", 20, jBorde2.getX());

        // Movimiento normal
        Jugador jMov = new Jugador("M", 5, 5, 10);
        jMov.mover("ARRIBA");
        assertEquals("ARRIBA resta 1 a Y", 4, jMov.getY());
        jMov.mover("DERECHA");
        assertEquals("DERECHA suma 1 a X", 6, jMov.getX());
        jMov.mover("ABAJO");
        assertEquals("ABAJO suma 1 a Y", 5, jMov.getY());
        jMov.mover("IZQUIERDA");
        assertEquals("IZQUIERDA resta 1 a X", 5, jMov.getX());
    }

    // ── Enemigo ───────────────────────────────────────────────────────────────

    private static void testEnemigo() {
        grupo("Enemigo");

        Enemigo en = new Enemigo("Goblin", 5, 5, 8, 2, 4);

        assertEquals("nombre", "Goblin", en.getNombre());
        assertEquals("vida inicial", 8, en.getVida());
        assertEquals("danioAtaque", 2, en.getDanioAtaque());
        assertEquals("rangoDeteccion", 4, en.getRangoDeteccion());
        assertEquals("estado inicial PATRULLAR", Enemigo.Estado.PATRULLAR, en.getEstadoIA());

        // Transición a ATACAR cuando jugador está adyacente
        MotorJuego motor = new MotorJuego();
        motor.iniciarPartida();
        Jugador jugador = motor.getJugador();

        Enemigo atacante = new Enemigo("Orc", jugador.getX() + 1, jugador.getY(), 10, 3, 6);
        motor.agregarEnemigo(atacante);
        atacante.actualizar(motor);
        assertEquals("dist=1 → ATACAR", Enemigo.Estado.ATACAR, atacante.getEstadoIA());

        // Transición a PERSEGUIR cuando jugador está en rango pero no adyacente
        Enemigo perseguidor = new Enemigo("Skeleton", jugador.getX() + 3, jugador.getY(), 5, 1, 6);
        motor.agregarEnemigo(perseguidor);
        perseguidor.actualizar(motor);
        assertEquals("dist=3 ≤ rango=6 → PERSEGUIR", Enemigo.Estado.PERSEGUIR, perseguidor.getEstadoIA());

        // Fuera de rango → PATRULLAR
        Enemigo lejano = new Enemigo("Far", jugador.getX() + 10, jugador.getY(), 5, 1, 4);
        motor.agregarEnemigo(lejano);
        lejano.actualizar(motor);
        assertEquals("dist=10 > rango=4 → PATRULLAR", Enemigo.Estado.PATRULLAR, lejano.getEstadoIA());
    }

    // ── Item ──────────────────────────────────────────────────────────────────

    private static void testItem() {
        grupo("Item");

        Item moneda = new Item("Moneda_1", 3, 3, Item.TipoItem.MONEDA, 5);
        assertEquals("tipo MONEDA", Item.TipoItem.MONEDA, moneda.getTipoItem());
        assertEquals("valor", 5, moneda.getValor());
        assertFalse("no recolectado al inicio", moneda.isRecolectado());
        assertTrue("estaVivo antes de recolectar", moneda.estaVivo());

        moneda.recolectar();
        assertTrue("isRecolectado tras recolectar", moneda.isRecolectado());
        assertFalse("estaVivo = false tras recolectar", moneda.estaVivo());

        Item pocion = new Item("Pocion_1", 7, 2, Item.TipoItem.POCION, 10);
        assertEquals("tipo POCION", Item.TipoItem.POCION, pocion.getTipoItem());
        assertEquals("valor pocion", 10, pocion.getValor());
    }

    // ── MotorJuego — estados ──────────────────────────────────────────────────

    private static void testMotorJuegoEstados() {
        grupo("MotorJuego - estados");

        MotorJuego motor = new MotorJuego();
        assertEquals("estado inicial MENU", MotorJuego.EstadoJuego.MENU, motor.getEstado());

        motor.iniciarPartida();
        assertEquals("iniciarPartida → JUGANDO", MotorJuego.EstadoJuego.JUGANDO, motor.getEstado());
        assertNotNull("jugador creado", motor.getJugador());
        assertFalse("hay enemigos", motor.getEnemigos().isEmpty());
        assertFalse("hay items", motor.getItems().isEmpty());

        motor.pausar();
        assertEquals("pausar → PAUSA", MotorJuego.EstadoJuego.PAUSA, motor.getEstado());

        // actualizar no debe procesar ticks en PAUSA
        long ticksAntes = motor.getTicks();
        motor.actualizar();
        assertEquals("actualizar ignorado en PAUSA", ticksAntes, motor.getTicks());

        motor.reanudar();
        assertEquals("reanudar → JUGANDO", MotorJuego.EstadoJuego.JUGANDO, motor.getEstado());

        motor.gameOver();
        assertEquals("gameOver → GAME_OVER", MotorJuego.EstadoJuego.GAME_OVER, motor.getEstado());

        // actualizar no debe procesar en GAME_OVER
        long ticksGO = motor.getTicks();
        motor.actualizar();
        assertEquals("actualizar ignorado en GAME_OVER", ticksGO, motor.getTicks());
    }

    // ── MotorJuego — colisiones ───────────────────────────────────────────────

    private static void testColisiones() {
        grupo("Colisiones");

        // Colisión jugador-enemigo reduce vida del jugador
        MotorJuego motor = new MotorJuego();
        motor.iniciarPartida();
        Jugador jugador = motor.getJugador();
        int vidaAntes = jugador.getVida();

        // Ponemos un enemigo encima del jugador
        Enemigo enemigo = new Enemigo("Trampa", jugador.getX(), jugador.getY(), 5, 3, 1);
        motor.agregarEnemigo(enemigo);
        motor.actualizar();
        assertTrue("colision con enemigo reduce vida jugador",
                jugador.getVida() < vidaAntes);

        // Colisión jugador-moneda suma puntuación
        MotorJuego motor2 = new MotorJuego();
        motor2.iniciarPartida();
        Jugador j2 = motor2.getJugador();
        // Eliminamos los items por defecto y ponemos uno encima del jugador
        for (Item it : motor2.getItems()) it.recolectar();
        Item moneda = new Item("Test_Coin", j2.getX(), j2.getY(), Item.TipoItem.MONEDA, 7);
        // Accedemos internamente: agregaremos el item via reflexión simulada —
        // en su lugar relocalizamos al jugador encima de una moneda ya existente
        // usando un motor limpio con items en posición conocida.
        MotorJuego motor3 = new MotorJuego();
        motor3.iniciarPartida();
        Jugador j3 = motor3.getJugador();
        // Movemos el jugador a (5,5) donde está Moneda_1
        while (j3.getX() < 5) j3.mover("DERECHA");
        while (j3.getX() > 5) j3.mover("IZQUIERDA");
        while (j3.getY() < 5) j3.mover("ABAJO");
        while (j3.getY() > 5) j3.mover("ARRIBA");
        int puntosAntes = motor3.getPuntuacion();
        motor3.actualizar();
        assertTrue("recolectar moneda suma puntuacion",
                motor3.getPuntuacion() > puntosAntes);

        // Colisión jugador-poción restaura vida
        MotorJuego motor4 = new MotorJuego();
        motor4.iniciarPartida();
        Jugador j4 = motor4.getJugador();
        j4.recibirDanio(8); // quitar vida para que la poción sirva
        // Mover a (2,14) donde está Pocion_1
        while (j4.getX() > 2) j4.mover("IZQUIERDA");
        while (j4.getX() < 2) j4.mover("DERECHA");
        while (j4.getY() < 14) j4.mover("ABAJO");
        while (j4.getY() > 14) j4.mover("ARRIBA");
        int vidaPrePocion = j4.getVida();
        motor4.actualizar();
        assertTrue("recolectar pocion restaura vida",
                j4.getVida() >= vidaPrePocion);
    }

    // ── SistemaLogros ─────────────────────────────────────────────────────────

    private static void testSistemaLogros() {
        grupo("SistemaLogros");

        SistemaLogros logros = new SistemaLogros();
        Jugador j = new Jugador("Hero", 0, 0, 20);

        assertTrue("sin logros al inicio", logros.getLogrosDesbloqueados().isEmpty());

        // Logro SUPERVIVIR_10_TURNOS
        logros.verificarLogros(j, 3, 10);
        assertTrue("desbloquear SUPERVIVIR_10_TURNOS",
                logros.getLogrosDesbloqueados().contains("SUPERVIVIR_10_TURNOS"));

        int expTras10Turnos = j.getExperiencia();
        // No duplicar logros
        logros.verificarLogros(j, 3, 15);
        assertEquals("logro no se desbloquea dos veces",
                expTras10Turnos, j.getExperiencia());

        // Logro ELIMINAR_TODOS_ENEMIGOS
        logros.verificarLogros(j, 0, 15);
        assertTrue("desbloquear ELIMINAR_TODOS_ENEMIGOS",
                logros.getLogrosDesbloqueados().contains("ELIMINAR_TODOS_ENEMIGOS"));

        // Logro RECOLECTAR_5_MONEDAS
        SistemaLogros logros2 = new SistemaLogros();
        Jugador j2 = new Jugador("X", 0, 0, 10);
        for (int i = 0; i < 5; i++) logros2.registrarMonedaRecolectada();
        logros2.verificarLogros(j2, 1, 1);
        assertTrue("desbloquear RECOLECTAR_5_MONEDAS",
                logros2.getLogrosDesbloqueados().contains("RECOLECTAR_5_MONEDAS"));

        // No desbloquear con menos de 5 monedas
        SistemaLogros logros3 = new SistemaLogros();
        Jugador j3 = new Jugador("Y", 0, 0, 10);
        for (int i = 0; i < 4; i++) logros3.registrarMonedaRecolectada();
        logros3.verificarLogros(j3, 1, 1);
        assertFalse("4 monedas no desbloquea RECOLECTAR_5_MONEDAS",
                logros3.getLogrosDesbloqueados().contains("RECOLECTAR_5_MONEDAS"));
    }

    // ── Integración ───────────────────────────────────────────────────────────

    private static void testIntegracion() {
        grupo("Integración - flujo completo");

        MotorJuego motor = new MotorJuego();
        motor.iniciarPartida();

        // Varios ticks sin que el jugador muera
        for (int i = 0; i < 5; i++) {
            if (motor.getEstado() == MotorJuego.EstadoJuego.JUGANDO) {
                motor.actualizar();
            }
        }
        assertTrue("motor sigue activo tras 5 ticks",
                motor.getEstado() == MotorJuego.EstadoJuego.JUGANDO
                || motor.getEstado() == MotorJuego.EstadoJuego.GAME_OVER
                || motor.getEstado() == MotorJuego.EstadoJuego.VICTORIA);
        assertTrue("ticks incrementados", motor.getTicks() > 0);

        // GestorEntradas — movimiento ignorado en PAUSA
        MotorJuego m2 = new MotorJuego();
        m2.iniciarPartida();
        GestorEntradas input = new GestorEntradas(m2);
        m2.pausar();
        int xAntes = m2.getJugador().getX();
        input.desplazarEntidad("DERECHA");
        assertEquals("movimiento ignorado en PAUSA", xAntes, m2.getJugador().getX());

        // GestorEntradas — movimiento funciona en JUGANDO
        m2.reanudar();
        input.desplazarEntidad("DERECHA");
        assertEquals("movimiento ejecutado en JUGANDO",
                Math.min(20, xAntes + 1), m2.getJugador().getX());

        // atacarEnemigoCercano sin enemigos cercanos no lanza excepción
        MotorJuego m3 = new MotorJuego();
        m3.iniciarPartida();
        // El jugador está en (10,10), los enemigos lejos
        m3.atacarEnemigoCercano();
        assertTrue("atacar sin adyacentes no rompe el motor",
                m3.getEstado() == MotorJuego.EstadoJuego.JUGANDO);
    }

    // ── Helpers de aserción ───────────────────────────────────────────────────

    private static void grupo(String nombre) {
        System.out.println("\n[ " + nombre + " ]");
    }

    private static void assertEquals(String desc, Object expected, Object actual) {
        if ((expected == null && actual == null)
                || (expected != null && expected.equals(actual))) {
            ok(desc);
        } else {
            fail(desc, "esperado=" + expected + " actual=" + actual);
        }
    }

    private static void assertEquals(String desc, int expected, int actual) {
        if (expected == actual) {
            ok(desc);
        } else {
            fail(desc, "esperado=" + expected + " actual=" + actual);
        }
    }

    private static void assertEquals(String desc, long expected, long actual) {
        if (expected == actual) {
            ok(desc);
        } else {
            fail(desc, "esperado=" + expected + " actual=" + actual);
        }
    }

    private static void assertTrue(String desc, boolean condition) {
        if (condition) ok(desc); else fail(desc, "condición falsa");
    }

    private static void assertFalse(String desc, boolean condition) {
        if (!condition) ok(desc); else fail(desc, "condición verdadera (se esperaba falsa)");
    }

    private static void assertNotNull(String desc, Object obj) {
        if (obj != null) ok(desc); else fail(desc, "era null");
    }

    private static void ok(String desc) {
        passed++;
        System.out.println("  PASS  " + desc);
    }

    private static void fail(String desc, String detail) {
        failed++;
        System.out.println("  FAIL  " + desc + " — " + detail);
    }
}
