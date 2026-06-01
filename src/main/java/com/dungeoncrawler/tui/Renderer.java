package com.dungeoncrawler.tui;

import com.dungeoncrawler.core.*;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.List;

/**
 * Renders the game state as an ASCII TUI using JLine3 attributed strings.
 * All output goes through the terminal's writer so ANSI codes work on Windows.
 */
public class Renderer {

    // ── Map dimensions ────────────────────────────────────────────────────────
    private static final int MAP_SIZE   = 21; // 0..20 inclusive
    private static final int CELL_W     = 2;  // chars per cell

    // ── Sprites (ASCII) ───────────────────────────────────────────────────────
    private static final String SPRITE_PLAYER  = "@";
    private static final String SPRITE_GOBLIN  = "g";
    private static final String SPRITE_ORC     = "O";
    private static final String SPRITE_SKEL    = "s";
    private static final String SPRITE_ENEMY   = "E";
    private static final String SPRITE_COIN    = "$";
    private static final String SPRITE_POTION  = "+";
    private static final String SPRITE_FLOOR   = "·";
    private static final String SPRITE_EMPTY   = " ";

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final AttributedStyle S_PLAYER  = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold();
    private static final AttributedStyle S_GOBLIN  = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
    private static final AttributedStyle S_ORC     = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold();
    private static final AttributedStyle S_SKEL    = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
    private static final AttributedStyle S_ENEMY   = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
    private static final AttributedStyle S_COIN    = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_POTION  = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA).bold();
    private static final AttributedStyle S_FLOOR   = AttributedStyle.DEFAULT.foreground(AttributedStyle.BLACK + 8); // bright black = dark grey
    private static final AttributedStyle S_BORDER  = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
    private static final AttributedStyle S_TITLE   = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_LABEL   = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
    private static final AttributedStyle S_VALUE   = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN);
    private static final AttributedStyle S_HP_OK   = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold();
    private static final AttributedStyle S_HP_LOW  = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold();
    private static final AttributedStyle S_LOG     = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
    private static final AttributedStyle S_LOG_DMG = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
    private static final AttributedStyle S_LOG_LVL = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_LOG_ITM = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA);
    private static final AttributedStyle S_PAUSE   = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_HINT    = AttributedStyle.DEFAULT.foreground(AttributedStyle.BLACK + 8);

    private final Terminal terminal;

    public Renderer(Terminal terminal) {
        this.terminal = terminal;
    }

    // ── Public render entry points ────────────────────────────────────────────

    public void renderMenu() {
        clearScreen();
        println(titleBanner());
        println("");
        println(centered("┌─────────────────────────┐", 60));
        println(centered("│     MENÚ PRINCIPAL      │", 60));
        println(centered("├─────────────────────────┤", 60));
        println(centered("│  [1]  Nueva Partida     │", 60));
        println(centered("│  [2]  Salir             │", 60));
        println(centered("└─────────────────────────┘", 60));
        println("");
        printAttributed(new AttributedStringBuilder()
                .style(S_HINT).append("  Usa las teclas de movimiento durante el juego:")
                .toAttributedString());
        println("");
        printAttributed(new AttributedStringBuilder()
                .style(S_HINT).append("  W/A/S/D ó ↑←↓→  Mover    |  ESPACIO  Atacar    |  P  Pausa    |  Q  Menú")
                .toAttributedString());
        println("");
    }

    public void renderGame(MotorJuego motor, GameLog log, int animTick) {
        clearScreen();

        Jugador jugador     = motor.getJugador();
        List<Enemigo> enems = motor.getEnemigos();
        List<Item>    items = motor.getItems();

        // ── Build map grid ────────────────────────────────────────────────────
        // Each cell: [style, sprite]
        AttributedStyle[][]  cellStyle  = new AttributedStyle[MAP_SIZE][MAP_SIZE];
        String[][]           cellSprite = new String[MAP_SIZE][MAP_SIZE];

        for (int y = 0; y < MAP_SIZE; y++)
            for (int x = 0; x < MAP_SIZE; x++) {
                cellStyle[y][x]  = S_FLOOR;
                cellSprite[y][x] = SPRITE_FLOOR;
            }

        // Place items
        for (Item item : items) {
            if (!item.estaVivo()) continue;
            int ix = clamp(item.getX());
            int iy = clamp(item.getY());
            if (item.getTipoItem() == Item.TipoItem.MONEDA) {
                cellStyle[iy][ix]  = S_COIN;
                cellSprite[iy][ix] = animCoin(animTick);
            } else {
                cellStyle[iy][ix]  = S_POTION;
                cellSprite[iy][ix] = SPRITE_POTION;
            }
        }

        // Place enemies
        for (Enemigo e : enems) {
            if (!e.estaVivo()) continue;
            int ex = clamp(e.getX());
            int ey = clamp(e.getY());
            String name = e.getNombre().toLowerCase();
            if (name.startsWith("goblin")) {
                cellStyle[ey][ex]  = S_GOBLIN;
                cellSprite[ey][ex] = animEnemy(SPRITE_GOBLIN, e.getEstadoIA(), animTick);
            } else if (name.startsWith("orc")) {
                cellStyle[ey][ex]  = S_ORC;
                cellSprite[ey][ex] = animEnemy(SPRITE_ORC, e.getEstadoIA(), animTick);
            } else if (name.startsWith("skeleton") || name.startsWith("skel")) {
                cellStyle[ey][ex]  = S_SKEL;
                cellSprite[ey][ex] = animEnemy(SPRITE_SKEL, e.getEstadoIA(), animTick);
            } else {
                cellStyle[ey][ex]  = S_ENEMY;
                cellSprite[ey][ex] = SPRITE_ENEMY;
            }
        }

        // Place player (last, so it's on top)
        if (jugador != null && jugador.estaVivo()) {
            int px = clamp(jugador.getX());
            int py = clamp(jugador.getY());
            cellStyle[py][px]  = S_PLAYER;
            cellSprite[py][px] = animPlayer(animTick);
        }

        // ── Render map + sidebar side by side ─────────────────────────────────
        // Map outer width: border + cells
        // map line = "│ " + 21*(cell+space) + "│"
        int sidebarX = MAP_SIZE * (CELL_W + 1) + 4;

        // Top border
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append("╔");
        for (int x = 0; x < MAP_SIZE * (CELL_W + 1) + 1; x++) ab.append("═");
        ab.append("╗");
        println(ab.toAttributedString());

        // Title row
        String mapTitle = " DUNGEON CRAWLER 2D ";
        int mapLineLen = MAP_SIZE * (CELL_W + 1) + 1;
        int pad = (mapLineLen - mapTitle.length()) / 2;
        ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append("║");
        ab.style(S_TITLE).append(" ".repeat(pad)).append(mapTitle).append(" ".repeat(mapLineLen - pad - mapTitle.length()));
        ab.style(S_BORDER).append("║");
        println(ab.toAttributedString());

        // Map sep
        ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append("╠");
        for (int x = 0; x < MAP_SIZE * (CELL_W + 1) + 1; x++) ab.append("═");
        ab.append("╣");
        println(ab.toAttributedString());

        // Map rows
        for (int y = 0; y < MAP_SIZE; y++) {
            ab = new AttributedStringBuilder();
            ab.style(S_BORDER).append("║ ");
            for (int x = 0; x < MAP_SIZE; x++) {
                ab.style(cellStyle[y][x]).append(cellSprite[y][x]);
                ab.style(S_FLOOR).append(" ");
            }
            ab.style(S_BORDER).append("║");

            // Sidebar content appended inline for rows 0..N
            String sidebar = sidebarLine(y, jugador, enems, motor, log, animTick);
            ab.style(AttributedStyle.DEFAULT).append("  ").append(sidebar);

            println(ab.toAttributedString());
        }

        // Bottom border
        ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append("╚");
        for (int x = 0; x < MAP_SIZE * (CELL_W + 1) + 1; x++) ab.append("═");
        ab.append("╝");
        println(ab.toAttributedString());

        // Controls hint
        println("");
        printAttributed(new AttributedStringBuilder()
                .style(S_HINT)
                .append("  W/A/S/D ó flechas: Mover  |  ESPACIO/F: Atacar  |  P: Pausa  |  Q: Menú")
                .toAttributedString());
    }

    public void renderPaused() {
        println("");
        printAttributed(new AttributedStringBuilder()
                .style(S_PAUSE).append("  ══ JUEGO PAUSADO ══  Pulsa P para reanudar")
                .toAttributedString());
    }

    public void renderVictory(MotorJuego motor) {
        clearScreen();
        println(victoryBanner());
        renderEndStats(motor);
        println("");
        printAttributed(new AttributedStringBuilder()
                .style(S_HINT).append("  Pulsa ENTER para volver al menú...")
                .toAttributedString());
    }

    public void renderGameOver(MotorJuego motor) {
        clearScreen();
        println(gameOverBanner());
        renderEndStats(motor);
        println("");
        printAttributed(new AttributedStringBuilder()
                .style(S_HINT).append("  Pulsa ENTER para volver al menú...")
                .toAttributedString());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void renderEndStats(MotorJuego motor) {
        Jugador j = motor.getJugador();
        println("");
        stat("Jugador",     j != null ? j.getNombre() : "?");
        if (j != null) {
            stat("Vida final",  j.getVida() + "/" + j.getVidaMaxima());
            stat("Nivel",       String.valueOf(j.getNivel()));
            stat("Experiencia", String.valueOf(j.getExperiencia()));
        }
        stat("Puntuación",  String.valueOf(motor.getPuntuacion()));
        stat("Turnos",      String.valueOf(motor.getTicks()));
        stat("Enemigos",    String.valueOf(motor.getEnemigos().size()) + " restantes");
        if (motor.getSistemaLogros() != null &&
                !motor.getSistemaLogros().getLogrosDesbloqueados().isEmpty()) {
            stat("Logros", motor.getSistemaLogros().getLogrosDesbloqueados().toString());
        }
    }

    /** Returns the sidebar line for the given map row index. */
    private String sidebarLine(int row, Jugador j, List<Enemigo> enems,
                                MotorJuego motor, GameLog log, int animTick) {
        // We build attributed lines; since we're mixing into a parent builder
        // we return plain strings padded to fixed width for alignment.
        // Rows 0-1: player stats header
        // Rows 2-N: enemy list, items, log messages
        final int W = 38;
        switch (row) {
            case 0:  return pad("╔══ ESTADÍSTICAS ═══════════════════╗", W);
            case 1:  return pad("║ " + (j != null ? j.getNombre() : "---") + " " .repeat(2) + stateTag(motor), W) + "  ║";
            case 2: {
                if (j == null) return pad("║ HP: ???", W) + "             ║";
                String bar = hpBar(j.getVida(), j.getVidaMaxima(), 18);
                return pad("║ HP " + bar + " " + j.getVida() + "/" + j.getVidaMaxima(), W) + " ║";
            }
            case 3: {
                if (j == null) return pad("║ XP: 0  LVL: 1", W) + "         ║";
                return pad("║ XP:" + j.getExperiencia() + "  LVL:" + j.getNivel() + "  PTS:" + motor.getPuntuacion(), W) + "   ║";
            }
            case 4:  return pad("╠══ ENEMIGOS ════════════════════════╣", W);
            case 5:
            case 6:
            case 7: {
                int idx = row - 5;
                if (idx < enems.size()) {
                    Enemigo e = enems.get(idx);
                    String ia = iaTag(e.getEstadoIA());
                    String nm = e.getNombre().length() > 10 ? e.getNombre().substring(0, 10) : e.getNombre();
                    String hp = e.getVida() + "hp";
                    return pad("║ " + ia + " " + nm + " " + hp, W) + " ║";
                }
                return pad("║", W) + "                                   ║";
            }
            case 8:  return pad("╠══ LOG ══════════════════════════════╣", W);
            default: {
                int logIdx = row - 9;
                List<String> msgs = log.getMessages();
                int msgRow = msgs.size() - (MAP_SIZE - 9) + logIdx;
                if (msgRow >= 0 && msgRow < msgs.size()) {
                    String msg = msgs.get(msgRow);
                    if (msg.length() > W - 3) msg = msg.substring(0, W - 3);
                    return pad("║ " + msg, W) + " ║";
                }
                return pad("║", W) + "                                   ║";
            }
        }
    }

    // ── Animation helpers ─────────────────────────────────────────────────────

    private String animPlayer(int tick) {
        return (tick % 4 < 2) ? "@" : "§";
    }

    private String animCoin(int tick) {
        String[] frames = {"$", "¢", "o", "¢"};
        return frames[tick % 4];
    }

    private String animEnemy(String sprite, Enemigo.Estado estado, int tick) {
        if (estado == Enemigo.Estado.ATACAR) {
            return (tick % 2 == 0) ? sprite.toUpperCase() : "!";
        } else if (estado == Enemigo.Estado.PERSEGUIR) {
            return (tick % 3 == 0) ? sprite : ">";
        }
        return sprite;
    }

    // ── Banner art ────────────────────────────────────────────────────────────

    private AttributedString titleBanner() {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_TITLE);
        ab.append("\n");
        ab.append("  ██████╗ ██╗   ██╗███╗   ██╗ ██████╗ ███████╗ ██████╗ ███╗   ██╗\n");
        ab.append("  ██╔══██╗██║   ██║████╗  ██║██╔════╝ ██╔════╝██╔═══██╗████╗  ██║\n");
        ab.append("  ██║  ██║██║   ██║██╔██╗ ██║██║  ███╗█████╗  ██║   ██║██╔██╗ ██║\n");
        ab.append("  ██║  ██║██║   ██║██║╚██╗██║██║   ██║██╔══╝  ██║   ██║██║╚██╗██║\n");
        ab.append("  ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝███████╗╚██████╔╝██║ ╚████║\n");
        ab.append("  ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝\n");
        ab.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE));
        ab.append("                     C R A W L E R   2 D   v1.2\n");
        return ab.toAttributedString();
    }

    private AttributedString victoryBanner() {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold());
        ab.append("\n");
        ab.append("  ██╗   ██╗██╗ ██████╗████████╗ ██████╗ ██████╗ ██╗ █████╗ ██╗\n");
        ab.append("  ██║   ██║██║██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗██║██╔══██╗██║\n");
        ab.append("  ██║   ██║██║██║        ██║   ██║   ██║██████╔╝██║███████║██║\n");
        ab.append("  ╚██╗ ██╔╝██║██║        ██║   ██║   ██║██╔══██╗██║██╔══██║╚═╝\n");
        ab.append("   ╚████╔╝ ██║╚██████╗   ██║   ╚██████╔╝██║  ██║██║██║  ██║██╗\n");
        ab.append("    ╚═══╝  ╚═╝ ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═╝\n");
        return ab.toAttributedString();
    }

    private AttributedString gameOverBanner() {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold());
        ab.append("\n");
        ab.append("   ██████╗  █████╗ ███╗   ███╗███████╗     ██████╗ ██╗   ██╗███████╗██████╗ \n");
        ab.append("  ██╔════╝ ██╔══██╗████╗ ████║██╔════╝    ██╔═══██╗██║   ██║██╔════╝██╔══██╗\n");
        ab.append("  ██║  ███╗███████║██╔████╔██║█████╗      ██║   ██║██║   ██║█████╗  ██████╔╝\n");
        ab.append("  ██║   ██║██╔══██║██║╚██╔╝██║██╔══╝      ██║   ██║╚██╗ ██╔╝██╔══╝  ██╔══██╗\n");
        ab.append("  ╚██████╔╝██║  ██║██║ ╚═╝ ██║███████╗    ╚██████╔╝ ╚████╔╝ ███████╗██║  ██║\n");
        ab.append("   ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝     ╚═════╝   ╚═══╝  ╚══════╝╚═╝  ╚═╝\n");
        return ab.toAttributedString();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String hpBar(int hp, int max, int width) {
        int filled = (max > 0) ? (int) ((double) hp / max * width) : 0;
        char fill = hp > max / 2 ? '█' : (hp > max / 4 ? '▓' : '░');
        return "[" + String.valueOf(fill).repeat(filled)
                + " ".repeat(width - filled) + "]";
    }

    private String stateTag(MotorJuego motor) {
        switch (motor.getEstado()) {
            case JUGANDO: return "▶ JUGANDO";
            case PAUSA:   return "⏸ PAUSA  ";
            case GAME_OVER: return "✖ GAME OVER";
            case VICTORIA:  return "★ VICTORIA";
            default: return "       ";
        }
    }

    private String iaTag(Enemigo.Estado e) {
        switch (e) {
            case ATACAR:    return "⚔";
            case PERSEGUIR: return "►";
            default:        return "~";
        }
    }

    private String pad(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }

    private String centered(String s, int width) {
        int p = Math.max(0, (width - s.length()) / 2);
        return " ".repeat(p) + s;
    }

    private void stat(String label, String value) {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_LABEL).append("  " + label + ": ");
        ab.style(S_VALUE).append(value);
        println(ab.toAttributedString());
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(MAP_SIZE - 1, v));
    }

    private void clearScreen() {
        terminal.writer().print("\033[H\033[2J");
        terminal.writer().flush();
    }

    private void println(String s) {
        terminal.writer().println(s);
        terminal.writer().flush();
    }

    private void println(AttributedString s) {
        terminal.writer().println(s.toAnsi(terminal));
        terminal.writer().flush();
    }

    private void printAttributed(AttributedString s) {
        terminal.writer().println(s.toAnsi(terminal));
        terminal.writer().flush();
    }
}
