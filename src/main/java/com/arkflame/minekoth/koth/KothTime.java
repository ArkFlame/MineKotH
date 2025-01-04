package com.arkflame.minekoth.koth;

public class KothTime {
    private int hour;
    private int minute;

    public KothTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public boolean isBefore(KothTime other) {
        if (hour < other.hour) {
            return true;
        } else if (hour == other.hour) {
            return minute < other.minute;
        } else {
            return false;
        }
    }

    public boolean isAfter(KothTime other) {
        if (hour > other.hour) {
            return true;
        } else if (hour == other.hour) {
            return minute > other.minute;
        } else {
            return false;
        }
    }

    public boolean isBetween(KothTime start, KothTime end) {
        return isAfter(start) && isBefore(end);
    }

    public boolean isSame(KothTime other) {
        return hour == other.hour && minute == other.minute;
    }

    public int getMinutesUntil(KothTime other) {
        int minutes = 0;

        if (isBefore(other)) {
            minutes = (other.hour - hour) * 60 + (other.minute - minute);
        }

        return minutes;
    }

    public KothTime addMinutes(int minutes) {
        int newHour = hour;
        int newMinute = minute + minutes;

        if (newMinute >= 60) {
            newHour += newMinute / 60;
            newMinute %= 60;
        }

        return new KothTime(newHour, newMinute);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", hour, minute);
    }
}
