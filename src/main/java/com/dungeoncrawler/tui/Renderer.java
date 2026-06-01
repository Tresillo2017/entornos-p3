package com.dungeoncrawler.tui;

import com.dungeoncrawler.core.*;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Display;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the game state as an ASCII TUI using JLine3's Display for diff-based
 * updates: only lines that changed are rewritten on each frame.
 */
public class Renderer {

    // ── Map dimensions ────────────────────────────────────────────────────────
    private static final int MAP_SIZE = 21; // 0..20 inclusive

    // ── Sprites ───────────────────────────────────────────────────────────────
    private static final String SPRITE_GOBLIN  = "g";
    private static final String SPRITE_ORC     = "O";
    private static final String SPRITE_SKEL    = "s";
    private static final String SPRITE_ENEMY   = "E";
    private static final String SPRITE_FLOOR   = "·";

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final AttributedStyle S_PLAYER  = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold();
    private static final AttributedStyle S_GOBLIN  = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
    private static final AttributedStyle S_ORC     = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold();
    private static final AttributedStyle S_SKEL    = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
    private static final AttributedStyle S_ENEMY   = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
    private static final AttributedStyle S_COIN    = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_POTION  = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA).bold();
    private static final AttributedStyle S_FLOOR   = AttributedStyle.DEFAULT.foreground(8); // bright black
    private static final AttributedStyle S_BORDER  = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
    private static final AttributedStyle S_TITLE   = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_LABEL   = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
    private static final AttributedStyle S_VALUE   = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN);
    private static final AttributedStyle S_PAUSE   = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_HINT    = AttributedStyle.DEFAULT.foreground(8);

    private final Terminal terminal;
    private final Display  display;

    public Renderer(Terminal terminal) {
        this.terminal = terminal;
        this.display  = new Display(terminal, true);
        this.display.reset();
    }

    // ── Public render entry points ────────────────────────────────────────────

    public void renderMenu() {
        List<AttributedString> lines = new ArrayList<>();
        for (String l : titleBannerLines()) lines.add(styled(S_TITLE, l));
        lines.add(AttributedString.EMPTY);
        lines.add(centeredLine("┌─────────────────────────┐", 60));
        lines.add(centeredLine("│     MENÚ PRINCIPAL      │", 60));
        lines.add(centeredLine("├─────────────────────────┤", 60));
        lines.add(centeredLine("│  [1]  Nueva Partida     │", 60));
        lines.add(centeredLine("│  [2]  Salir             │", 60));
        lines.add(centeredLine("└─────────────────────────┘", 60));
        lines.add(AttributedString.EMPTY);
        lines.add(styled(S_HINT, "  W/A/S/D ó ↑←↓→  Mover  |  ESPACIO  Atacar  |  P  Pausa  |  Q  Menú"));
        lines.add(AttributedString.EMPTY);
        push(lines);
    }

    public void renderGame(MotorJuego motor, GameLog log, int animTick) {
        Jugador       jugador = motor.getJugador();
        List<Enemigo> enems   = motor.getEnemigos();
        List<Item>    items   = motor.getItems();

        // ── Build map grid ────────────────────────────────────────────────────
        AttributedStyle[][] cellStyle  = new AttributedStyle[MAP_SIZE][MAP_SIZE];
        String[][]          cellSprite = new String[MAP_SIZE][MAP_SIZE];

        for (int y = 0; y < MAP_SIZE; y++)
            for (int x = 0; x < MAP_SIZE; x++) {
                cellStyle[y][x]  = S_FLOOR;
                cellSprite[y][x] = SPRITE_FLOOR;
            }

        for (Item item : items) {
            if (!item.estaVivo()) continue;
            int ix = clamp(item.getX()), iy = clamp(item.getY());
            if (item.getTipoItem() == Item.TipoItem.MONEDA) {
                cellStyle[iy][ix]  = S_COIN;
                cellSprite[iy][ix] = animCoin(animTick);
            } else {
                cellStyle[iy][ix]  = S_POTION;
                cellSprite[iy][ix] = "+";
            }
        }

        for (Enemigo e : enems) {
            if (!e.estaVivo()) continue;
            int ex = clamp(e.getX()), ey = clamp(e.getY());
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

        if (jugador != null && jugador.estaVivo()) {
            int px = clamp(jugador.getX()), py = clamp(jugador.getY());
            cellStyle[py][px]  = S_PLAYER;
            cellSprite[py][px] = animPlayer(animTick);
        }

        // ── Assemble lines list ───────────────────────────────────────────────
        List<AttributedString> lines = new ArrayList<>();

        // Top border
        lines.add(mapBorder("╔", "═", "╗"));

        // Title
        lines.add(mapTitleLine());

        // Separator
        lines.add(mapBorder("╠", "═", "╣"));

        // Map rows + sidebar
        for (int y = 0; y < MAP_SIZE; y++) {
            AttributedStringBuilder ab = new AttributedStringBuilder();
            ab.style(S_BORDER).append("║ ");
            for (int x = 0; x < MAP_SIZE; x++) {
                ab.style(cellStyle[y][x]).append(cellSprite[y][x]);
                ab.style(S_FLOOR).append(" ");
            }
            ab.style(S_BORDER).append("║  ");
            ab.style(AttributedStyle.DEFAULT).append(sidebarLine(y, jugador, enems, motor, log));
            lines.add(ab.toAttributedString());
        }

        // Bottom border
        lines.add(mapBorder("╚", "═", "╝"));

        // Controls
        lines.add(AttributedString.EMPTY);
        lines.add(styled(S_HINT, "  W/A/S/D ó flechas: Mover  |  ESPACIO/F: Atacar  |  P: Pausa  |  Q: Menú"));

        // Pause overlay appended as extra line
        if (motor.getEstado() == MotorJuego.EstadoJuego.PAUSA) {
            lines.add(styled(S_PAUSE, "  ══ JUEGO PAUSADO ══  Pulsa P para reanudar"));
        } else {
            lines.add(AttributedString.EMPTY);
        }

        push(lines);
    }

    public void renderVictory(MotorJuego motor) {
        List<AttributedString> lines = new ArrayList<>();
        AttributedStyle gold = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
        for (String l : victoryBannerLines()) lines.add(styled(gold, l));
        lines.addAll(endStatLines(motor));
        lines.add(AttributedString.EMPTY);
        lines.add(styled(S_HINT, "  Pulsa cualquier tecla para volver al menú..."));
        push(lines);
    }

    public void renderGameOver(MotorJuego motor) {
        List<AttributedString> lines = new ArrayList<>();
        AttributedStyle red = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold();
        for (String l : gameOverBannerLines()) lines.add(styled(red, l));
        lines.addAll(endStatLines(motor));
        lines.add(AttributedString.EMPTY);
        lines.add(styled(S_HINT, "  Pulsa cualquier tecla para volver al menú..."));
        push(lines);
    }

    // ── Core display update ───────────────────────────────────────────────────

    /** Hands the full line list to Display; it diffs and only redraws changed lines. */
    private void push(List<AttributedString> lines) {
        int cols = terminal.getWidth();
        int rows = terminal.getHeight();
        if (cols <= 0) cols = 120;
        if (rows <= 0) rows = 40;
        display.resize(rows, cols);
        display.update(lines, -1);
    }

    // ── Line builders ─────────────────────────────────────────────────────────

    private AttributedString mapBorder(String l, String mid, String r) {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append(l);
        int width = MAP_SIZE * 2 + 1; // each cell is "X " = 2 chars, plus leading space
        for (int i = 0; i < width + 1; i++) ab.append(mid);
        ab.append(r);
        return ab.toAttributedString();
    }

    private AttributedString mapTitleLine() {
        String title  = " DUNGEON CRAWLER 2D ";
        int lineLen   = MAP_SIZE * 2 + 2;
        int leftPad   = (lineLen - title.length()) / 2;
        int rightPad  = lineLen - title.length() - leftPad;
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append("║");
        ab.style(S_TITLE).append(" ".repeat(leftPad)).append(title).append(" ".repeat(rightPad));
        ab.style(S_BORDER).append("║");
        return ab.toAttributedString();
    }

    private String sidebarLine(int row, Jugador j, List<Enemigo> enems,
                                MotorJuego motor, GameLog log) {
        final int W = 38;
        switch (row) {
            case 0: return pad("╔══ ESTADÍSTICAS ═══════════════════╗", W);
            case 1: return pad("║ " + (j != null ? j.getNombre() : "---") + "  " + stateTag(motor), W) + " ║";
            case 2: {
                if (j == null) return pad("║ HP: ???", W) + " ║";
                return pad("║ HP " + hpBar(j.getVida(), j.getVidaMaxima(), 18)
                        + " " + j.getVida() + "/" + j.getVidaMaxima(), W) + " ║";
            }
            case 3: {
                if (j == null) return pad("║ XP:0  LVL:1  PTS:0", W) + " ║";
                return pad("║ XP:" + j.getExperiencia()
                        + "  LVL:" + j.getNivel()
                        + "  PTS:" + motor.getPuntuacion(), W) + " ║";
            }
            case 4: return pad("╠══ ENEMIGOS ════════════════════════╣", W);
            case 5: case 6: case 7: {
                int idx = row - 5;
                if (idx < enems.size()) {
                    Enemigo e  = enems.get(idx);
                    String nm  = e.getNombre().length() > 10 ? e.getNombre().substring(0, 10) : e.getNombre();
                    return pad("║ " + iaTag(e.getEstadoIA()) + " " + nm + " " + e.getVida() + "hp", W) + " ║";
                }
                return pad("║", W) + " ║";
            }
            case 8: return pad("╠══ LOG ══════════════════════════════╣", W);
            default: {
                List<String> msgs   = log.getMessages();
                int visible         = MAP_SIZE - 9;
                int offset          = msgs.size() - visible + (row - 9);
                if (offset >= 0 && offset < msgs.size()) {
                    String msg = msgs.get(offset);
                    if (msg.length() > W - 3) msg = msg.substring(0, W - 3);
                    return pad("║ " + msg, W) + " ║";
                }
                return pad("║", W) + " ║";
            }
        }
    }

    private List<AttributedString> endStatLines(MotorJuego motor) {
        Jugador j = motor.getJugador();
        List<AttributedString> out = new ArrayList<>();
        out.add(AttributedString.EMPTY);
        out.add(statLine("Jugador",     j != null ? j.getNombre() : "?"));
        if (j != null) {
            out.add(statLine("Vida final",  j.getVida() + "/" + j.getVidaMaxima()));
            out.add(statLine("Nivel",       String.valueOf(j.getNivel())));
            out.add(statLine("Experiencia", String.valueOf(j.getExperiencia())));
        }
        out.add(statLine("Puntuación",  String.valueOf(motor.getPuntuacion())));
        out.add(statLine("Turnos",      String.valueOf(motor.getTicks())));
        out.add(statLine("Enemigos",    motor.getEnemigos().size() + " restantes"));
        if (motor.getSistemaLogros() != null
                && !motor.getSistemaLogros().getLogrosDesbloqueados().isEmpty()) {
            out.add(statLine("Logros", motor.getSistemaLogros().getLogrosDesbloqueados().toString()));
        }
        return out;
    }

    // ── Animation ─────────────────────────────────────────────────────────────

    private String animPlayer(int tick) {
        return (tick % 4 < 2) ? "@" : "§";
    }

    private String animCoin(int tick) {
        String[] frames = {"$", "¢", "o", "¢"};
        return frames[tick % 4];
    }

    private String animEnemy(String sprite, Enemigo.Estado estado, int tick) {
        if (estado == Enemigo.Estado.ATACAR)    return (tick % 2 == 0) ? sprite.toUpperCase() : "!";
        if (estado == Enemigo.Estado.PERSEGUIR) return (tick % 3 == 0) ? sprite : ">";
        return sprite;
    }

    // ── Banner lines ──────────────────────────────────────────────────────────

    private String[] titleBannerLines() {
        return new String[]{
            "",
            "  ██████╗ ██╗   ██╗███╗   ██╗ ██████╗ ███████╗ ██████╗ ███╗   ██╗",
            "  ██╔══██╗██║   ██║████╗  ██║██╔════╝ ██╔════╝██╔═══██╗████╗  ██║",
            "  ██║  ██║██║   ██║██╔██╗ ██║██║  ███╗█████╗  ██║   ██║██╔██╗ ██║",
            "  ██║  ██║██║   ██║██║╚██╗██║██║   ██║██╔══╝  ██║   ██║██║╚██╗██║",
            "  ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝███████╗╚██████╔╝██║ ╚████║",
            "  ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝",
            "                     C R A W L E R   2 D   v1.2",
        };
    }

    private String[] victoryBannerLines() {
        return new String[]{
            "",
            "  ██╗   ██╗██╗ ██████╗████████╗ ██████╗ ██████╗ ██╗ █████╗ ██╗",
            "  ██║   ██║██║██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗██║██╔══██╗██║",
            "  ██║   ██║██║██║        ██║   ██║   ██║██████╔╝██║███████║██║",
            "  ╚██╗ ██╔╝██║██║        ██║   ██║   ██║██╔══██╗██║██╔══██║╚═╝",
            "   ╚████╔╝ ██║╚██████╗   ██║   ╚██████╔╝██║  ██║██║██║  ██║██╗",
            "    ╚═══╝  ╚═╝ ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═╝",
        };
    }

    private String[] gameOverBannerLines() {
        return new String[]{
            "",
            "   ██████╗  █████╗ ███╗   ███╗███████╗     ██████╗ ██╗   ██╗███████╗██████╗ ",
            "  ██╔════╝ ██╔══██╗████╗ ████║██╔════╝    ██╔═══██╗██║   ██║██╔════╝██╔══██╗",
            "  ██║  ███╗███████║██╔████╔██║█████╗      ██║   ██║██║   ██║█████╗  ██████╔╝",
            "  ██║   ██║██╔══██║██║╚██╔╝██║██╔══╝      ██║   ██║╚██╗ ██╔╝██╔══╝  ██╔══██╗",
            "  ╚██████╔╝██║  ██║██║ ╚═╝ ██║███████╗    ╚██████╔╝ ╚████╔╝ ███████╗██║  ██║",
            "   ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝     ╚═════╝   ╚═══╝  ╚══════╝╚═╝  ╚═╝",
        };
    }

    // ── Small helpers ─────────────────────────────────────────────────────────

    private String hpBar(int hp, int max, int width) {
        int filled = max > 0 ? (int) ((double) hp / max * width) : 0;
        char fill  = hp > max / 2 ? '█' : (hp > max / 4 ? '▓' : '░');
        return "[" + String.valueOf(fill).repeat(filled) + " ".repeat(width - filled) + "]";
    }

    private String stateTag(MotorJuego motor) {
        switch (motor.getEstado()) {
            case JUGANDO:   return "▶ JUGANDO ";
            case PAUSA:     return "⏸ PAUSA   ";
            case GAME_OVER: return "✖ GAME OVER";
            case VICTORIA:  return "★ VICTORIA ";
            default:        return "           ";
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
        return s.length() >= width ? s : s + " ".repeat(width - s.length());
    }

    private AttributedString styled(AttributedStyle style, String text) {
        return new AttributedStringBuilder().style(style).append(text).toAttributedString();
    }

    private AttributedString centeredLine(String text, int width) {
        int p = Math.max(0, (width - text.length()) / 2);
        return styled(S_BORDER, " ".repeat(p) + text);
    }

    private AttributedString statLine(String label, String value) {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_LABEL).append("  " + label + ": ");
        ab.style(S_VALUE).append(value);
        return ab.toAttributedString();
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(MAP_SIZE - 1, v));
    }
}
