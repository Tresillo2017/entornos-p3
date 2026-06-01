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

    private static final int FRAME_MS = 300;

    // Synthetic key codes for arrow keys
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
            menuLoop();
        } finally {
            terminal.writer().print("\033[?25h");
            terminal.writer().flush();
            terminal.close();
        }
    }

    // ── Menu loop ─────────────────────────────────────────────────────────────

    private void menuLoop() throws IOException {
        while (true) {
            renderer.renderMenu();
            int key = readKey();
            if (key == '1') {
                gameLoop();
            } else if (key == '2' || key == 'q' || key == 'Q') {
                terminal.writer().println("\n  ¡Hasta luego!");
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

        terminal.writer().print("\033[?25l"); // hide cursor
        terminal.writer().flush();

        int  animTick   = 0;
        long lastFrame  = System.currentTimeMillis();
        boolean running = true;

        while (running) {
            MotorJuego.EstadoJuego estado = motor.getEstado();

            // Terminal conditions
            if (estado == MotorJuego.EstadoJuego.VICTORIA) {
                renderer.renderVictory(motor);
                waitAnyKey();
                running = false;
                break;
            }
            if (estado == MotorJuego.EstadoJuego.GAME_OVER) {
                renderer.renderGameOver(motor);
                waitAnyKey();
                running = false;
                break;
            }

            // Render (pause overlay is handled inside renderGame)
            renderer.renderGame(motor, log, animTick);

            // Read key within remaining frame time
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

            // Advance frame
            long now = System.currentTimeMillis();
            if (now - lastFrame >= FRAME_MS) {
                animTick++;
                lastFrame = now;
                if (motor.getEstado() == MotorJuego.EstadoJuego.JUGANDO) {
                    motor.actualizar();
                }
            }
        }

        terminal.writer().print("\033[?25h"); // restore cursor
        terminal.writer().flush();
    }

    // ── Key dispatch ─────────────────────────────────────────────────────────

    private void handleGameKey(int key) {
        MotorJuego.EstadoJuego estado = motor.getEstado();

        switch (key) {
            case 'w': case 'W': case KEY_UP:
                if (estado == MotorJuego.EstadoJuego.JUGANDO)
                    gestorInput.desplazarEntidad("ARRIBA");
                break;
            case 's': case 'S': case KEY_DOWN:
                if (estado == MotorJuego.EstadoJuego.JUGANDO)
                    gestorInput.desplazarEntidad("ABAJO");
                break;
            case 'a': case 'A': case KEY_LEFT:
                if (estado == MotorJuego.EstadoJuego.JUGANDO)
                    gestorInput.desplazarEntidad("IZQUIERDA");
                break;
            case 'd': case 'D': case KEY_RIGHT:
                if (estado == MotorJuego.EstadoJuego.JUGANDO)
                    gestorInput.desplazarEntidad("DERECHA");
                break;
            case ' ': case 'f': case 'F':
                if (estado == MotorJuego.EstadoJuego.JUGANDO)
                    gestorInput.pulsarBotonAccion();
                break;
            case 'p': case 'P':
                if (estado == MotorJuego.EstadoJuego.JUGANDO)
                    gestorInput.pausarJuego();
                else if (estado == MotorJuego.EstadoJuego.PAUSA)
                    gestorInput.reanudarJuego();
                break;
        }
    }

    // ── JLine3 key reading ────────────────────────────────────────────────────

    private int readKey() throws IOException {
        int c = terminal.reader().read();
        return resolveEscape(c);
    }

    private int readKeyTimeout(long timeoutMs) throws IOException {
        int c = terminal.reader().read(timeoutMs);
        if (c == -1) return -1;
        return resolveEscape(c);
    }

    /** Consume ESC [ A/B/C/D sequences and return synthetic key codes. */
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

    private void waitAnyKey() throws IOException {
        terminal.writer().flush();
        terminal.reader().read();
    }
}
