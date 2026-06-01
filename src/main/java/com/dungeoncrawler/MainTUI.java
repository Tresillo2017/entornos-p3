package com.dungeoncrawler;

import com.dungeoncrawler.core.GestorEntradas;
import com.dungeoncrawler.core.MotorJuego;
import com.dungeoncrawler.tui.GameLog;
import com.dungeoncrawler.tui.LogInterceptor;
import com.dungeoncrawler.tui.Renderer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

/**
 * Entry point for the JLine3 TUI version of Dungeon Crawler 2D.
 *
 * Controls:
 *   W / ↑        Move up
 *   S / ↓        Move down
 *   A / ←        Move left
 *   D / →        Move right
 *   SPACE / F    Attack
 *   P            Pause / Resume
 *   Q            Return to main menu
 */
public class MainTUI {

    private static final int FRAME_MS = 200;

    private static final int KEY_UP    = 1001;
    private static final int KEY_DOWN  = 1002;
    private static final int KEY_RIGHT = 1003;
    private static final int KEY_LEFT  = 1004;

    private Terminal       terminal;
    private Renderer       renderer;
    private GameLog        log;
    private MotorJuego     motor;
    private GestorEntradas gestorInput;

    public static void main(String[] args) throws IOException {
        new MainTUI().run();
    }

    private void run() throws IOException {
        terminal = TerminalBuilder.builder()
                .system(true)
                .jansi(true)
                .dumb(false)
                .build();

        terminal.enterRawMode();

        log      = new GameLog();
        renderer = new Renderer(terminal);

        System.setOut(new LogInterceptor(System.out, log));

        try {
            renderer.clearScreen();
            menuLoop();
        } finally {
            renderer.clearScreen();
            terminal.writer().print("\033[?25h");
            terminal.writer().flush();
            terminal.close();
        }
    }

    // ── Menu loop (animated) ──────────────────────────────────────────────────

    private void menuLoop() throws IOException {
        int animTick = 0;
        while (true) {
            renderer.renderMenu(animTick++);
            int key = readKeyTimeout(FRAME_MS);
            if (key == '1') {
                renderer.clearScreen();
                gameLoop();
                renderer.clearScreen();
                animTick = 0;
            } else if (key == '2' || key == 'q' || key == 'Q') {
                renderer.clearScreen();
                terminal.writer().println("  ¡Hasta luego!");
                terminal.writer().flush();
                return;
            }
        }
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    private void gameLoop() throws IOException {
        motor       = new MotorJuego();
        gestorInput = new GestorEntradas(motor);
        log.clear();

        motor.iniciarPartida();

        renderer.clearScreen();
        terminal.writer().print("\033[?25l");
        terminal.writer().flush();

        int  animTick  = 0;
        long lastFrame = System.currentTimeMillis();
        boolean running = true;

        while (running) {
            MotorJuego.EstadoJuego estado = motor.getEstado();

            if (estado == MotorJuego.EstadoJuego.VICTORIA) {
                endScreenLoop(true, motor);
                running = false;
                break;
            }
            if (estado == MotorJuego.EstadoJuego.GAME_OVER) {
                endScreenLoop(false, motor);
                running = false;
                break;
            }

            renderer.renderGame(motor, log, animTick);

            long elapsed = System.currentTimeMillis() - lastFrame;
            long wait    = Math.max(1L, FRAME_MS - elapsed);
            int key = readKeyTimeout(wait);

            if (key != -1) {
                if (key == 'q' || key == 'Q') {
                    running = false;
                    break;
                }
                handleGameKey(key);
            }

            long now = System.currentTimeMillis();
            if (now - lastFrame >= FRAME_MS) {
                animTick++;
                lastFrame = now;
                if (motor.getEstado() == MotorJuego.EstadoJuego.JUGANDO) {
                    motor.actualizar();
                }
            }
        }

        terminal.writer().print("\033[?25h");
        terminal.writer().flush();
    }

    // ── Animated end screen loop ──────────────────────────────────────────────

    private void endScreenLoop(boolean victory, MotorJuego motor) throws IOException {
        renderer.clearScreen();
        int animTick = 0;
        while (true) {
            if (victory) renderer.renderVictory(motor, animTick++);
            else         renderer.renderGameOver(motor, animTick++);

            int key = readKeyTimeout(FRAME_MS);
            if (key != -1) break;
        }
    }

    // ── Key dispatch ──────────────────────────────────────────────────────────

    private void handleGameKey(int key) {
        MotorJuego.EstadoJuego estado = motor.getEstado();
        switch (key) {
            case 'w': case 'W': case KEY_UP:
                if (estado == MotorJuego.EstadoJuego.JUGANDO) gestorInput.desplazarEntidad("ARRIBA");
                break;
            case 's': case 'S': case KEY_DOWN:
                if (estado == MotorJuego.EstadoJuego.JUGANDO) gestorInput.desplazarEntidad("ABAJO");
                break;
            case 'a': case 'A': case KEY_LEFT:
                if (estado == MotorJuego.EstadoJuego.JUGANDO) gestorInput.desplazarEntidad("IZQUIERDA");
                break;
            case 'd': case 'D': case KEY_RIGHT:
                if (estado == MotorJuego.EstadoJuego.JUGANDO) gestorInput.desplazarEntidad("DERECHA");
                break;
            case ' ': case 'f': case 'F':
                if (estado == MotorJuego.EstadoJuego.JUGANDO) gestorInput.pulsarBotonAccion();
                break;
            case 'p': case 'P':
                if (estado == MotorJuego.EstadoJuego.JUGANDO)      gestorInput.pausarJuego();
                else if (estado == MotorJuego.EstadoJuego.PAUSA)   gestorInput.reanudarJuego();
                break;
        }
    }

    // ── JLine3 key reading ────────────────────────────────────────────────────

    private int readKey() throws IOException {
        return resolveEscape(terminal.reader().read());
    }

    private int readKeyTimeout(long timeoutMs) throws IOException {
        int c = terminal.reader().read(timeoutMs);
        if (c == -1) return -1;
        return resolveEscape(c);
    }

    private int resolveEscape(int first) throws IOException {
        if (first != 27) return first;
        int second = terminal.reader().read(50);
        if (second == '[') {
            int third = terminal.reader().read(50);
            switch (third) {
                case 'A': return KEY_UP;
                case 'B': return KEY_DOWN;
                case 'C': return KEY_RIGHT;
                case 'D': return KEY_LEFT;
            }
        }
        return first;
    }
}
