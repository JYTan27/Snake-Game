package com.comp2042.model.stats;

import com.comp2042.model.GameMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunSessionTest {

    private static final Instant RECORDED_AT = Instant.parse("2026-11-25T09:00:00Z");
    private final AtomicLong nanos = new AtomicLong();
    private final RunSession session = new RunSession(
            GameMode.POWER_UPS, Clock.fixed(RECORDED_AT, ZoneOffset.UTC), nanos::get);

    @Test
    void ignoresAnUnstartedRun() {
        advanceMillis(1_200);
        session.pause();

        assertTrue(session.finish(RunOutcome.ABANDONED, 0, 2).isEmpty());
    }

    @Test
    void excludesInitialWaitingAndPausesWhenRecordingTheFinalResult() {
        advanceMillis(1_200);
        session.startOrResume();
        advanceMillis(250);
        session.startOrResume();
        advanceMillis(150);
        session.pause();
        advanceMillis(3_000);
        session.pause();
        session.startOrResume();
        advanceMillis(600);

        RunResult result = session.finish(RunOutcome.WIN, 25, 7).orElseThrow();

        assertEquals(GameMode.POWER_UPS.name(), result.modeId());
        assertEquals(RunOutcome.WIN, result.outcome());
        assertEquals(25, result.score());
        assertEquals(7, result.snakeLength());
        assertEquals(RECORDED_AT, result.recordedAt());
        assertEquals(1_000, result.activeTimeMillis());
    }

    @Test
    void finishingWhilePausedRecordsOnlyOnceEvenIfTheRunReceivesMoreInput() {
        session.startOrResume();
        advanceMillis(700);
        session.pause();
        advanceMillis(1_500);

        RunResult result = session.finish(RunOutcome.ABANDONED, 5, 3).orElseThrow();

        assertEquals(700, result.activeTimeMillis());
        assertEquals(RunOutcome.ABANDONED, result.outcome());
        session.startOrResume();
        advanceMillis(800);
        assertTrue(session.finish(RunOutcome.LOSS, 10, 4).isEmpty());
    }

    private void advanceMillis(long millis) {
        nanos.addAndGet(TimeUnit.MILLISECONDS.toNanos(millis));
    }
}
