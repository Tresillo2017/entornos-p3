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
 * Renders all game screens using JLine3 Display (diff-based updates).
 * Layout adapts to terminal size; banners animate per tick.
 */
public class Renderer {

    private static final int MAP_SIZE    = 21;
    private static final int FALLBACK_W  = 120;
    private static final int FALLBACK_H  = 40;

    // ── Sprites ───────────────────────────────────────────────────────────────
    private static final String SPRITE_GOBLIN = "g";
    private static final String SPRITE_ORC    = "O";
    private static final String SPRITE_SKEL   = "s";
    private static final String SPRITE_ENEMY  = "E";
    private static final String SPRITE_FLOOR  = "·";

    // ── Colour cycle for title animation (yellow → cyan → magenta → white) ───
    private static final int[] TITLE_COLOURS = {
        AttributedStyle.YELLOW,
        AttributedStyle.CYAN,
        AttributedStyle.MAGENTA,
        AttributedStyle.WHITE,
        AttributedStyle.CYAN,
        AttributedStyle.YELLOW,
    };

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final AttributedStyle S_PLAYER = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold();
    private static final AttributedStyle S_GOBLIN = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
    private static final AttributedStyle S_ORC    = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold();
    private static final AttributedStyle S_SKEL   = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
    private static final AttributedStyle S_ENEMY  = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
    private static final AttributedStyle S_COIN   = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_POTION = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA).bold();
    private static final AttributedStyle S_FLOOR  = AttributedStyle.DEFAULT.foreground(8);
    private static final AttributedStyle S_BORDER = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
    private static final AttributedStyle S_LABEL  = AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE).bold();
    private static final AttributedStyle S_VALUE  = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN);
    private static final AttributedStyle S_PAUSE  = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
    private static final AttributedStyle S_HINT   = AttributedStyle.DEFAULT.foreground(8);

    private final Terminal terminal;
    private final Display  display;

    public Renderer(Terminal terminal) {
        this.terminal = terminal;
        this.display  = new Display(terminal, true);
    }

    /** Hard-clears the screen and resets the Display diff state. Call on screen transitions. */
    public void clearScreen() {
        terminal.writer().print("\033[H\033[2J\033[3J");
        terminal.writer().flush();
        display.reset();
    }

    // ── Terminal size (with fallback) ─────────────────────────────────────────

    private int cols() {
        int w = terminal.getWidth();
        if (w > 0) return w;
        // PowerShell / ConEmu sometimes expose this via env
        String c = System.getenv("COLUMNS");
        if (c != null) { try { return Integer.parseInt(c.trim()); } catch (NumberFormatException ignored) {} }
        return FALLBACK_W;
    }

    private int rows() {
        int h = terminal.getHeight();
        if (h > 0) return h;
        String r = System.getenv("LINES");
        if (r != null) { try { return Integer.parseInt(r.trim()); } catch (NumberFormatException ignored) {} }
        return FALLBACK_H;
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    public void renderMenu(int tick) {
        int w      = cols();
        int h      = rows();
        int innerW = w - 2; // chars between the two ║ border chars

        AttributedStyle boxStyle = AttributedStyle.DEFAULT
                .foreground(TITLE_COLOURS[tick % TITLE_COLOURS.length]).bold();

        // Right panel (fixed-width plain text), visible when terminal is wide enough.
        // Each string must be exactly PANEL_W chars wide.
        final int PANEL_W = 32;
        String[] panel = {
            "╔══════════════════════════════╗",
            "║        CONTROLES             ║",
            "╠══════════════════════════════╣",
            "║  W/↑   Mover arriba          ║",
            "║  S/↓   Mover abajo           ║",
            "║  A/←   Mover izquierda       ║",
            "║  D/→   Mover derecha         ║",
            "║  ESPACIO  Atacar             ║",
            "║  P     Pausa / Reanudar      ║",
            "║  Q     Volver al menu        ║",
            "╠══════════════════════════════╣",
            "║        ENEMIGOS              ║",
            "╠══════════════════════════════╣",
            "║  g  Goblin   ~ patrulla      ║",
            "║  O  Orc      > persigue      ║",
            "║  s  Skeleton ! ataca         ║",
            "╠══════════════════════════════╣",
            "║        ITEMS                 ║",
            "╠══════════════════════════════╣",
            "║  $  Moneda  +5 pts           ║",
            "║  +  Pocion  +10 vida         ║",
            "╚══════════════════════════════╝",
        };

        boolean showPanel = innerW >= PANEL_W + 40; // need room for banner too
        // leftW = area available for banner/menu content
        int leftW = showPanel ? innerW - PANEL_W : innerW;

        // Content rows (banner + menu + hint)
        String sub = (tick % 2 == 0)
                ? "~ DUNGEON CRAWLER 2D ~  "
                : "~ DUNGEON CRAWLER 2D ~ _";
        String[] menuBox = {
            "╔═══════════════════════════╗",
            "║      MENU PRINCIPAL       ║",
            "╠═══════════════════════════╣",
            "║   [1]  Nueva Partida      ║",
            "║   [2]  Salir              ║",
            "╚═══════════════════════════╝",
        };
        String hint = "W/A/S/D · flechas: Mover  |  ESPACIO: Atacar  |  P: Pausa  |  Q: Salir";

        // Build content rows as AttributedStrings of exactly leftW columns each
        List<AttributedString> content = new ArrayList<>();
        for (String bl : TITLE_LINES) content.add(animTitleCentered(bl, tick, leftW));
        content.add(centerInWidth(styled(S_HINT, sub), sub.length(), leftW));
        content.add(blankW(leftW));
        for (String ml : menuBox) content.add(centerInWidth(styled(boxStyle, ml), ml.length(), leftW));
        content.add(blankW(leftW));
        content.add(centerInWidth(styled(S_HINT, hint), hint.length(), leftW));

        // Vertical centering: pad above and below content
        int contentH = content.size();
        int totalInner = h - 2; // rows between top and bottom borders
        int topPad = Math.max(0, (totalInner - contentH) / 2);

        List<AttributedString> lines = new ArrayList<>();
        lines.add(hline("╔", "═", "╗", w));

        for (int r = 0; r < totalInner; r++) {
            int ci = r - topPad; // index into content
            AttributedString left = (ci >= 0 && ci < contentH) ? content.get(ci) : blankW(leftW);
            String panelLine = (showPanel && r < panel.length) ? panel[r] : spaces(PANEL_W);
            lines.add(menuRow(left, panelLine, w, showPanel, PANEL_W, boxStyle));
        }

        lines.add(hline("╚", "═", "╝", w));
        push(lines);
    }

    /**
     * Builds one full-width menu row:
     * ║ [left: exactly leftW cols] [panel: exactly panelW cols if showPanel] ║
     */
    private AttributedString menuRow(AttributedString left, String panelLine,
                                     int w, boolean showPanel, int panelW,
                                     AttributedStyle panelStyle) {
        int innerW = w - 2;
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append("║");
        ab.append(left); // already exactly leftW cols
        if (showPanel) {
            // fill gap between left area and panel (innerW - leftW - panelW spaces)
            int gap = innerW - left.columnLength() - panelLine.length();
            if (gap > 0) ab.append(spaces(gap));
            ab.style(panelStyle).append(panelLine);
        }
        ab.style(S_BORDER).append("║");
        return ab.toAttributedString();
    }

    /** Animated title line — each char gets a cycling colour, result is exactly areaW cols. */
    private AttributedString animTitleCentered(String text, int tick, int areaW) {
        int textLen = text.length(); // box-drawing chars are 1-col wide
        int lp = Math.max(0, (areaW - textLen) / 2);
        int rp = Math.max(0, areaW - textLen - lp);
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(AttributedStyle.DEFAULT).append(spaces(lp));
        for (int i = 0; i < textLen; i++) {
            int ci = (tick + i) % TITLE_COLOURS.length;
            ab.style(AttributedStyle.DEFAULT.foreground(TITLE_COLOURS[ci]).bold());
            ab.append(text.charAt(i));
        }
        ab.style(AttributedStyle.DEFAULT).append(spaces(rp));
        return ab.toAttributedString();
    }

    /** Centers an already-styled AttributedString inside areaW columns. */
    private AttributedString centerInWidth(AttributedString s, int textLen, int areaW) {
        int lp = Math.max(0, (areaW - textLen) / 2);
        int rp = Math.max(0, areaW - textLen - lp);
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.append(spaces(lp));
        ab.append(s);
        ab.append(spaces(rp));
        return ab.toAttributedString();
    }

    /** AttributedString of exactly w spaces. */
    private AttributedString blankW(int w) {
        return new AttributedString(spaces(w));
    }

    private String spaces(int n) {
        return n > 0 ? " ".repeat(n) : "";
    }

    /** Empty bordered row that fills the full width. */
    private AttributedString borderEmpty(int w) {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append("║").append(spaces(Math.max(0, w - 2))).append("║");
        return ab.toAttributedString();
    }

    // ── Game ──────────────────────────────────────────────────────────────────

    public void renderGame(MotorJuego motor, GameLog log, int tick) {
        int w = cols();

        Jugador       jugador = motor.getJugador();
        List<Enemigo> enems   = motor.getEnemigos();
        List<Item>    items   = motor.getItems();

        // Build map grid
        AttributedStyle[][] cs = new AttributedStyle[MAP_SIZE][MAP_SIZE];
        String[][]          ct = new String[MAP_SIZE][MAP_SIZE];
        for (int y = 0; y < MAP_SIZE; y++)
            for (int x = 0; x < MAP_SIZE; x++) { cs[y][x] = S_FLOOR; ct[y][x] = SPRITE_FLOOR; }

        for (Item item : items) {
            if (!item.estaVivo()) continue;
            int ix = clamp(item.getX()), iy = clamp(item.getY());
            if (item.getTipoItem() == Item.TipoItem.MONEDA) {
                cs[iy][ix] = S_COIN; ct[iy][ix] = animCoin(tick);
            } else {
                cs[iy][ix] = S_POTION; ct[iy][ix] = "+";
            }
        }
        for (Enemigo e : enems) {
            if (!e.estaVivo()) continue;
            int ex = clamp(e.getX()), ey = clamp(e.getY());
            String nm = e.getNombre().toLowerCase();
            if      (nm.startsWith("goblin"))              { cs[ey][ex] = S_GOBLIN; ct[ey][ex] = animEnemy(SPRITE_GOBLIN, e.getEstadoIA(), tick); }
            else if (nm.startsWith("orc"))                 { cs[ey][ex] = S_ORC;    ct[ey][ex] = animEnemy(SPRITE_ORC,    e.getEstadoIA(), tick); }
            else if (nm.startsWith("skel"))                { cs[ey][ex] = S_SKEL;   ct[ey][ex] = animEnemy(SPRITE_SKEL,   e.getEstadoIA(), tick); }
            else                                           { cs[ey][ex] = S_ENEMY;  ct[ey][ex] = SPRITE_ENEMY; }
        }
        if (jugador != null && jugador.estaVivo()) {
            int px = clamp(jugador.getX()), py = clamp(jugador.getY());
            cs[py][px] = S_PLAYER; ct[py][px] = animPlayer(tick);
        }

        // Map block width: "║ " + 21*(ch+" ") + "║" = 2 + 42 + 1 = 45
        int mapW   = 2 + MAP_SIZE * 2 + 1;   // 45
        int sideW  = Math.max(30, w - mapW - 3);

        List<AttributedString> lines = new ArrayList<>();

        // Top border spans full width
        lines.add(hline("╔", "═", "╗", w));
        lines.add(titleBar(" DUNGEON CRAWLER 2D ", w));
        lines.add(hline("╠", "═", "╣", w));

        for (int y = 0; y < MAP_SIZE; y++) {
            AttributedStringBuilder ab = new AttributedStringBuilder();
            ab.style(S_BORDER).append("║ ");
            for (int x = 0; x < MAP_SIZE; x++) {
                ab.style(cs[y][x]).append(ct[y][x]);
                ab.style(S_FLOOR).append(" ");
            }
            ab.style(S_BORDER).append("║ ");
            ab.style(AttributedStyle.DEFAULT).append(sidebarLine(y, jugador, enems, motor, log, sideW));
            lines.add(ab.toAttributedString());
        }

        lines.add(hline("╚", "═", "╝", w));
        lines.add(AttributedString.EMPTY);

        String hint = "  W/A/S/D · flechas: Mover  │  ESPACIO/F: Atacar  │  P: Pausa  │  Q: Menú";
        lines.add(styled(S_HINT, hint));

        if (motor.getEstado() == MotorJuego.EstadoJuego.PAUSA) {
            lines.add(styled(S_PAUSE, "  ══ JUEGO PAUSADO ══  Pulsa P para reanudar"));
        } else {
            lines.add(AttributedString.EMPTY);
        }

        push(lines);
    }

    // ── Victory ───────────────────────────────────────────────────────────────

    public void renderVictory(MotorJuego motor, int tick) {
        int w = cols();
        List<AttributedString> lines = new ArrayList<>();
        lines.add(AttributedString.EMPTY);
        for (String l : VICTORY_LINES) lines.add(animTitleCentered(l, tick, w));
        lines.add(AttributedString.EMPTY);

        String stars = starRow(tick, 10);
        String tag   = stars + "  VICTORIA  " + stars;
        lines.add(centerInWidth(styled(
            AttributedStyle.DEFAULT.foreground(TITLE_COLOURS[tick % TITLE_COLOURS.length]).bold(),
            tag), tag.length(), w));
        lines.add(AttributedString.EMPTY);

        lines.addAll(endStatLines(motor, w));
        lines.add(AttributedString.EMPTY);
        lines.add(centerInWidth(styled(S_HINT, "[ Pulsa cualquier tecla para continuar ]"), 42, w));
        push(lines);
    }

    // ── Game Over ─────────────────────────────────────────────────────────────

    public void renderGameOver(MotorJuego motor, int tick) {
        int w = cols();
        List<AttributedString> lines = new ArrayList<>();
        lines.add(AttributedString.EMPTY);
        for (String l : GAMEOVER_LINES) lines.add(animGameOverCentered(l, tick, w));
        lines.add(AttributedString.EMPTY);

        String skull = (tick % 2 == 0) ? "* *  Y O U   D I E D  * *" : "    Y O U   D I E D    ";
        lines.add(centerInWidth(styled(
            AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold(), skull),
            skull.length(), w));
        lines.add(AttributedString.EMPTY);

        lines.addAll(endStatLines(motor, w));
        lines.add(AttributedString.EMPTY);
        lines.add(centerInWidth(styled(S_HINT, "[ Pulsa cualquier tecla para continuar ]"), 42, w));
        push(lines);
    }

    // ── Display push ──────────────────────────────────────────────────────────

    private void push(List<AttributedString> lines) {
        int c = cols(), r = rows();
        display.resize(r, c);
        // Truncate lines that exceed terminal width to prevent wrapping artifacts
        List<AttributedString> clamped = new ArrayList<>(lines.size());
        for (AttributedString l : lines) {
            if (l.columnLength() > c) {
                clamped.add(l.columnSubSequence(0, c));
            } else {
                clamped.add(l);
            }
        }
        // Pad list to terminal height so Display erases any leftover lines
        while (clamped.size() < r) clamped.add(AttributedString.EMPTY);
        display.update(clamped, -1);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private String sidebarLine(int row, Jugador j, List<Enemigo> enems,
                                MotorJuego motor, GameLog log, int sideW) {
        int W = sideW;
        switch (row) {
            case 0: return pad("╔══ ESTADÍSTICAS " + "═".repeat(Math.max(0, W - 18)) + "╗", W);
            case 1: return pad("║ " + (j != null ? j.getNombre() : "---") + "  " + stateTag(motor), W) + " ║";
            case 2: {
                if (j == null) return pad("║ HP: ???", W) + " ║";
                int barW = Math.max(5, W - 16);
                return pad("║ HP " + hpBar(j.getVida(), j.getVidaMaxima(), barW)
                        + " " + j.getVida() + "/" + j.getVidaMaxima(), W) + " ║";
            }
            case 3: {
                if (j == null) return pad("║ XP:0  LVL:1  PTS:0", W) + " ║";
                return pad("║ XP:" + j.getExperiencia()
                        + "  LVL:" + j.getNivel()
                        + "  PTS:" + motor.getPuntuacion(), W) + " ║";
            }
            case 4: return pad("╠══ ENEMIGOS " + "═".repeat(Math.max(0, W - 13)) + "╣", W);
            case 5: case 6: case 7: {
                int idx = row - 5;
                if (idx < enems.size()) {
                    Enemigo e  = enems.get(idx);
                    int maxNm  = Math.max(4, W - 14);
                    String nm  = e.getNombre().length() > maxNm ? e.getNombre().substring(0, maxNm) : e.getNombre();
                    return pad("║ " + iaTag(e.getEstadoIA()) + " " + nm + " " + e.getVida() + "hp", W) + " ║";
                }
                return pad("║", W) + " ║";
            }
            case 8: return pad("╠══ LOG " + "═".repeat(Math.max(0, W - 8)) + "╣", W);
            default: {
                List<String> msgs = log.getMessages();
                int visible       = MAP_SIZE - 9;
                int offset        = msgs.size() - visible + (row - 9);
                if (offset >= 0 && offset < msgs.size()) {
                    String msg = msgs.get(offset);
                    if (msg.length() > W - 3) msg = msg.substring(0, W - 3);
                    return pad("║ " + msg, W) + " ║";
                }
                return pad("║", W) + " ║";
            }
        }
    }

    // ── End stats ─────────────────────────────────────────────────────────────

    private List<AttributedString> endStatLines(MotorJuego motor, int w) {
        Jugador j = motor.getJugador();
        List<AttributedString> out = new ArrayList<>();
        out.add(centered(statLine("Jugador",     j != null ? j.getNombre() : "?"), w));
        if (j != null) {
            out.add(centered(statLine("Vida final",  j.getVida() + "/" + j.getVidaMaxima()), w));
            out.add(centered(statLine("Nivel",       String.valueOf(j.getNivel())), w));
            out.add(centered(statLine("Experiencia", String.valueOf(j.getExperiencia())), w));
        }
        out.add(centered(statLine("Puntuación",  String.valueOf(motor.getPuntuacion())), w));
        out.add(centered(statLine("Turnos",      String.valueOf(motor.getTicks())), w));
        out.add(centered(statLine("Enemigos",    motor.getEnemigos().size() + " restantes"), w));
        if (motor.getSistemaLogros() != null
                && !motor.getSistemaLogros().getLogrosDesbloqueados().isEmpty()) {
            out.add(centered(statLine("Logros", motor.getSistemaLogros().getLogrosDesbloqueados().toString()), w));
        }
        return out;
    }

    // ── Animation helpers ─────────────────────────────────────────────────────

    /** Game-over line: per-char red/yellow sweep, centered in areaW columns. */
    private AttributedString animGameOverCentered(String text, int tick, int areaW) {
        if (text.isBlank()) return blankW(areaW);
        int lp = Math.max(0, (areaW - text.length()) / 2);
        int rp = Math.max(0, areaW - text.length() - lp);
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(AttributedStyle.DEFAULT).append(spaces(lp));
        for (int i = 0; i < text.length(); i++) {
            boolean bright = ((tick + i) % 3) != 0;
            AttributedStyle s = bright
                ? AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold()
                : AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold();
            ab.style(s).append(text.charAt(i));
        }
        ab.style(AttributedStyle.DEFAULT).append(spaces(rp));
        return ab.toAttributedString();
    }

    /** Row of alternating star/sparkle chars that shift each tick. */
    private String starRow(int tick, int len) {
        char[] chars = {'*', '·', '✦', '·', '*', ' ', '✦', ' '};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(chars[(tick + i) % chars.length]);
        return sb.toString();
    }

    private String animPlayer(int tick) { return (tick % 4 < 2) ? "@" : "§"; }

    private String animCoin(int tick) {
        String[] f = {"$", "¢", "o", "¢"}; return f[tick % 4];
    }

    private String animEnemy(String sprite, Enemigo.Estado estado, int tick) {
        if (estado == Enemigo.Estado.ATACAR)    return (tick % 2 == 0) ? sprite.toUpperCase() : "!";
        if (estado == Enemigo.Estado.PERSEGUIR) return (tick % 3 == 0) ? sprite : ">";
        return sprite;
    }

    // ── Layout helpers ────────────────────────────────────────────────────────

    /** Full-width horizontal border line. */
    private AttributedString hline(String l, String mid, String r, int w) {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append(l);
        for (int i = 0; i < w - 2; i++) ab.append(mid);
        ab.append(r);
        return ab.toAttributedString();
    }

    /** Centered title inside border. */
    private AttributedString titleBar(String title, int w) {
        int inner   = w - 2;
        int lp      = (inner - title.length()) / 2;
        int rp      = inner - title.length() - lp;
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_BORDER).append("║");
        ab.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold())
          .append(" ".repeat(lp)).append(title).append(" ".repeat(rp));
        ab.style(S_BORDER).append("║");
        return ab.toAttributedString();
    }

    private AttributedString centered(AttributedString s, int w) {
        // Measure visible length (AttributedString.length() = codepoints, not ANSI)
        int len = s.toString().length();
        int pad = Math.max(0, (w - len) / 2);
        if (pad == 0) return s;
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.append(" ".repeat(pad));
        ab.append(s);
        return ab.toAttributedString();
    }

    private String hpBar(int hp, int max, int width) {
        int filled = max > 0 ? (int) ((double) hp / max * width) : 0;
        char fill  = hp > max / 2 ? '█' : (hp > max / 4 ? '▓' : '░');
        return "[" + String.valueOf(fill).repeat(Math.max(0, filled))
                   + " ".repeat(Math.max(0, width - filled)) + "]";
    }

    private String stateTag(MotorJuego motor) {
        switch (motor.getEstado()) {
            case JUGANDO:   return "▶ JUGANDO";
            case PAUSA:     return "⏸ PAUSA  ";
            case GAME_OVER: return "✖ GAME OVER";
            case VICTORIA:  return "★ VICTORIA";
            default:        return "         ";
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
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    private AttributedString styled(AttributedStyle style, String text) {
        return new AttributedStringBuilder().style(style).append(text).toAttributedString();
    }

    private AttributedString statLine(String label, String value) {
        AttributedStringBuilder ab = new AttributedStringBuilder();
        ab.style(S_LABEL).append("  " + label + ": ");
        ab.style(S_VALUE).append(value);
        return ab.toAttributedString();
    }

    private int clamp(int v) { return Math.max(0, Math.min(MAP_SIZE - 1, v)); }

    // ── ASCII art (compact, fits in 80 cols) ──────────────────────────────────

    private static final String[] TITLE_LINES = {
        " ██████╗ ██╗   ██╗███╗   ██╗ ██████╗ ███████╗ ██████╗ ███╗   ██╗",
        " ██╔══██╗██║   ██║████╗  ██║██╔════╝ ██╔════╝██╔═══██╗████╗  ██║",
        " ██║  ██║██║   ██║██╔██╗ ██║██║  ███╗█████╗  ██║   ██║██╔██╗ ██║",
        " ██║  ██║██║   ██║██║╚██╗██║██║   ██║██╔══╝  ██║   ██║██║╚██╗██║",
        " ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝███████╗╚██████╔╝██║ ╚████║",
        " ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝",
    };

    private static final String[] VICTORY_LINES = {
        " ██╗   ██╗██╗ ██████╗████████╗ ██████╗ ██████╗ ██╗ █████╗ ██╗",
        " ██║   ██║██║██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗██║██╔══██╗██║",
        " ██║   ██║██║██║        ██║   ██║   ██║██████╔╝██║███████║██║",
        " ╚██╗ ██╔╝██║██║        ██║   ██║   ██║██╔══██╗██║██╔══██║╚═╝",
        "  ╚████╔╝ ██║╚██████╗   ██║   ╚██████╔╝██║  ██║██║██║  ██║██╗",
        "   ╚═══╝  ╚═╝ ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═╝",
    };

    private static final String[] GAMEOVER_LINES = {
        "  ██████╗  █████╗ ███╗   ███╗███████╗     ██████╗ ██╗   ██╗███████╗██████╗",
        " ██╔════╝ ██╔══██╗████╗ ████║██╔════╝    ██╔═══██╗██║   ██║██╔════╝██╔══██╗",
        " ██║  ███╗███████║██╔████╔██║█████╗      ██║   ██║██║   ██║█████╗  ██████╔╝",
        " ██║   ██║██╔══██║██║╚██╔╝██║██╔══╝      ██║   ██║╚██╗ ██╔╝██╔══╝  ██╔══██╗",
        " ╚██████╔╝██║  ██║██║ ╚═╝ ██║███████╗    ╚██████╔╝ ╚████╔╝ ███████╗██║  ██║",
        "  ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝     ╚═════╝   ╚═══╝  ╚══════╝╚═╝  ╚═╝",
    };
}
