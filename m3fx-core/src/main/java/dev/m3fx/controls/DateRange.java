package dev.m3fx.controls;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

/**
 * An inclusive range of dates. The constructor orders the bounds, so {@code start} is never after
 * {@code end}.
 */
public record DateRange(LocalDate start, LocalDate end) {

    public DateRange {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
    }

    /** A single-day range. */
    public static DateRange of(LocalDate day) {
        return new DateRange(day, day);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /** Number of days, both bounds included. */
    public long days() {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    /** Short display form, e.g. "Oct 3 – Oct 17" (with the year when the bounds differ in year). */
    public String format(Locale locale) {
        DateTimeFormatter dayMonth = DateTimeFormatter.ofPattern("MMM d", locale);
        DateTimeFormatter full = DateTimeFormatter.ofPattern("MMM d, yyyy", locale);
        if (start.equals(end)) {
            return full.format(start);
        }
        DateTimeFormatter f = start.getYear() == end.getYear() ? dayMonth : full;
        return f.format(start) + " – " + full.format(end);
    }

    @Override
    public String toString() {
        return start + "/" + end;
    }
}
