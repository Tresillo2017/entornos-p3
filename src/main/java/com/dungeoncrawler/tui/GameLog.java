package com.dungeoncrawler.tui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Circular message log shown in the sidebar.
 * Captures up to MAX_MESSAGES recent entries.
 */
public class GameLog {

    private static final int MAX_MESSAGES = 50;

    private final Deque<String> messages = new ArrayDeque<>();

    public void add(String msg) {
        if (messages.size() >= MAX_MESSAGES) messages.pollFirst();
        messages.addLast(msg);
    }

    /** Returns all messages oldest-first. */
    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clear() {
        messages.clear();
    }
}
