package com.dungeoncrawler.tui;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Replaces System.out to capture engine log lines into GameLog
 * while suppressing them from the terminal (the TUI renders everything).
 */
public class LogInterceptor extends PrintStream {

    private final GameLog log;
    private final StringBuilder buf = new StringBuilder();

    public LogInterceptor(OutputStream original, GameLog log) {
        super(original, true);
        this.log = log;
    }

    @Override
    public void println(String x) {
        if (x != null && !x.isBlank()) {
            log.add(x);
        }
    }

    @Override
    public void print(String s) {
        // swallow prompt characters the old Main would print
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return this;
    }
}
