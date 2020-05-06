package com.color.sms.messages.theme.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@SuppressLint("SimpleDateFormat")
public class DateTimeUtility {
    public static final List<Long> times = Arrays.asList(TimeUnit.DAYS.toMillis(365L), TimeUnit.DAYS.toMillis(30L), TimeUnit.DAYS.toMillis(1L), TimeUnit.HOURS.toMillis(1L), TimeUnit.MINUTES.toMillis(1L), TimeUnit.SECONDS.toMillis(1L));
    private static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");
    private static final List<String> timesStringViet = Arrays.asList("năm", "tháng", "ngsfy", "giờ", "phút", "giây");
    private static Locale locale = new Locale("vi", "VN");

    public static String format(long time) {
        return new SimpleDateFormat("dd/MM/yyyy hh:mma", locale).format(time);
    }

    public static String formatMessageTime(long time) {
        return new SimpleDateFormat("dd-MM-yyyy hh:mm aaa", locale).format(time);
    }

    public static String formatFull(long time) {
        return new SimpleDateFormat("EEE dd/MM/yyyy\nhh:mm a", locale).format(time);
    }

    public static String formatDate(long time) {
        return new SimpleDateFormat("dd/MM/yyyy", locale).format(time);
    }

    public static String formatDateStart(long time) {
        return new SimpleDateFormat("dd/MM", locale).format(time);
    }

    public static String formatDateToDayMonth(long time) {
        return new SimpleDateFormat("dd MMMM", locale).format(time);
    }

    private static String formatDateToDayMonthYear(long time) {
        return new SimpleDateFormat("dd/MM/yyyy", locale).format(time);
    }

    public static String formatDateToMonth(long time) {
        return new SimpleDateFormat("MMMM", locale).format(time);
    }

    public static String formatDayofWeek(long time) {
        return new SimpleDateFormat("EEEE", locale).format(time);
    }

    public static String formatDayofWeek2(long time) {
        return new SimpleDateFormat("EEE", locale).format(time);
    }

    public static String formatEventTime(long time) {
        return new SimpleDateFormat("hh:mma", locale).format(time);
    }

    public static String formatFullTime(long time) {
        return new SimpleDateFormat("hh:mma", locale).format(time);
    }

    private static String fullFormatDate(long time) {
        return new SimpleDateFormat("EEEE dd/MM/yyyy").format(time);
    }

    public static String fireBaseFormatDate(long time) {
        return new SimpleDateFormat("ddMMyyyy", locale).format(time);
    }

    public static String getDay(long time) {
        return new SimpleDateFormat("dd", locale).format(time);
    }

    public static String getHour(long time) {
        return new SimpleDateFormat("HH", locale).format(time);
    }

    public static String getMinute(long time) {
        return new SimpleDateFormat("mm", locale).format(time);
    }

    public static String getMonth(long time) {
        return new SimpleDateFormat("M", locale).format(time);
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = GregorianCalendar.getInstance(locale);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = GregorianCalendar.getInstance(locale);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 23, 59, 59);
        return calendar.getTime();
    }

    public static long getTimeEndOfDay(long time) {
        Calendar localCalendar = GregorianCalendar.getInstance(locale);
        localCalendar.setTimeInMillis(time);
        localCalendar.set(Calendar.HOUR_OF_DAY, 23);
        localCalendar.set(Calendar.MINUTE, 59);
        localCalendar.set(Calendar.SECOND, 59);
        localCalendar.set(Calendar.MILLISECOND, 999);
        return localCalendar.getTimeInMillis();
    }

    public static long getTimeStartOfDay(long time) {
        Calendar localCalendar = GregorianCalendar.getInstance(locale);
        localCalendar.setTimeInMillis(time); // compute start of the day for the timestamp
        localCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);
        return localCalendar.getTimeInMillis();
    }

    public static String getYear(long time) {
        return new SimpleDateFormat("yyyy", locale).format(time);
    }

    public static boolean isOneDay(long time1, long time2) {
        return formatDateToDayMonthYear(time1).equalsIgnoreCase(formatDateToDayMonthYear(time2));
    }

    @SuppressLint("DefaultLocale")
    public static String formatTime(long millis) {
        long seconds = Math.round((double) millis / 1000);
        long hours = TimeUnit.SECONDS.toHours(seconds);
        if (hours > 0)
            seconds -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = seconds > 0 ? TimeUnit.SECONDS.toMinutes(seconds) : 0;
        if (minutes > 0)
            seconds -= TimeUnit.MINUTES.toSeconds(minutes);
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public static String formatVideoTime(long timeInMillisec) {
        long hours = TimeUnit.MILLISECONDS.toHours(timeInMillisec) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillisec) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillisec) % 60;
        if (hours > 0)
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        if (minutes > 0) return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        if (seconds > 0) return String.format(Locale.getDefault(), "00:%02d", seconds);
        return "00:00";
    }
}
