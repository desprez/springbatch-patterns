package fr.training.springbatch.tools.decider;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.IsoFields;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for computing French public holidays and working/business days.
 * <p>
 * Supports both fixed-date holidays (such as New Year's Day, Bastille Day, Christmas)
 * and variable-date Christian holidays (Easter Monday, Ascension Day, Pentecost Monday)
 * computed using the Meeus/Jones/Butcher algorithm.
 * </p>
 */
public class FrenchHolidaysUtil {

    private FrenchHolidaysUtil() {
        /* This utility class should not be instantiated */
    }

    /**
     * Checks if the given date is a working day (Monday through Friday, excluding French public holidays).
     *
     * @param aDate the date to check
     * @return {@code true} if the date is a working day; {@code false} otherwise
     */
    public static boolean isWorkingDay(LocalDate aDate) {
        DayOfWeek day = aDate.getDayOfWeek();
        return !DayOfWeek.SATURDAY.equals(day) && !DayOfWeek.SUNDAY.equals(day) && !isHoliday(aDate);
    }

    /**
     * Checks if the given date is a business day (Monday through Saturday, excluding French public holidays).
     *
     * @param aDate the date to check
     * @return {@code true} if the date is a business day; {@code false} otherwise
     */
    public static boolean isBusinessDay(LocalDate aDate) {
        DayOfWeek day = aDate.getDayOfWeek();
        return !DayOfWeek.SUNDAY.equals(day) && !isHoliday(aDate);
    }

    /**
     * Returns the last working day of the specified month and year.
     *
     * @param month the month of the year
     * @param year  the year
     * @return the {@link LocalDate} representing the last working day of the month
     */
    public static LocalDate getLastWorkingDayOfTheMonth(Month month, int year) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        while (!isWorkingDay(lastDay)) {
            lastDay = lastDay.minusDays(1);
        }
        return lastDay;
    }

    /**
     * Returns the last working day of the specified ISO week number and year.
     *
     * @param weekNumber the ISO-8601 week number of the year (1-53)
     * @param year       the year
     * @return the {@link LocalDate} representing the last working day of the week
     */
    public static LocalDate getLastWorkingDayOfTheWeek(int weekNumber, int year) {
        LocalDate lastDay = LocalDate.of(year, Month.JANUARY, 4)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNumber)
                .with(DayOfWeek.SUNDAY);
        while (!isWorkingDay(lastDay)) {
            lastDay = lastDay.minusDays(1);
        }
        return lastDay;
    }

    /**
     * Returns the number of working days between two dates, i.e. all days except weekends and public holidays.
     * The start date is inclusive and the end date is exclusive.
     *
     * @param fromDate the start date (inclusive)
     * @param toDate   the end date (exclusive)
     * @return the number of working days between the two dates
     */
    public static int getWorkingDays(LocalDate fromDate, LocalDate toDate) {
        int workingDays = 0;
        while (!fromDate.isEqual(toDate)) {
            if (isWorkingDay(fromDate)) workingDays++;
            fromDate = fromDate.plusDays(1);
        }
        return workingDays;
    }

    /**
     * Returns the name of the French public holiday on the given date, or {@code null} if the
     * date is not a public holiday.
     *
     * @param date the date to look up
     * @return the French name of the public holiday (e.g. {@code "Noël"}, {@code "Fête du travail"}),
     *         or {@code null} if the date is not a public holiday
     */
    public static String getHolidayName(LocalDate date) {
        return getHolidays(date.getYear()).get(date);
    }

    /**
     * Checks if the given date is a French public holiday.
     *
     * @param date the date to check
     * @return {@code true} if the given date is a French public holiday; {@code false} otherwise
     */
    public static boolean isHoliday(LocalDate date) {
        return getHolidayName(date) != null;
    }

    /**
     * Returns a map of {@code (date -> public holiday name)} of all French public holidays for the specified year.
     *
     * @param year the year for which to compute public holidays
     * @return a {@link Map} of holiday dates to their French names
     */
    public static Map<LocalDate, String> getHolidays(int year) {

        Map<LocalDate, String> holidays = new HashMap<>();

        holidays.put(LocalDate.of(year, Month.JANUARY, 1), "Jour de l'an");
        holidays.put(LocalDate.of(year, Month.MAY, 1), "Fête du travail");
        holidays.put(LocalDate.of(year, Month.MAY, 8), "Victoire 1945");
        holidays.put(LocalDate.of(year, Month.JULY, 14), "Fête nationale");
        holidays.put(LocalDate.of(year, Month.AUGUST, 15), "Assomption");
        holidays.put(LocalDate.of(year, Month.NOVEMBER, 1), "Toussaint");
        holidays.put(LocalDate.of(year, Month.NOVEMBER, 11), "Armistice 1918");
        holidays.put(LocalDate.of(year, Month.DECEMBER, 25), "Noël");

        LocalDate easter = getEasterSundayDate(year);
        LocalDate easterMondayHoliday = getEasterMondayHoliday(year);

        holidays.put(easterMondayHoliday, "Lundi de Pâques");
        holidays.put(getAscensionHoliday(easter), "Jeudi de l'Ascension");
        holidays.put(getPentecostMondayHoliday(easter), "Lundi de Pentecôte");

        return holidays;
    }

    /**
     * Computes the date of Easter Monday for a given year.
     *
     * @param year the year
     * @return the {@link LocalDate} of Easter Monday
     */
    private static LocalDate getEasterMondayHoliday(int year) {
        return getEasterSundayDate(year).plusDays(1);
    }

    /**
     * Computes the date of Easter Sunday for a given year using the Meeus/Jones/Butcher algorithm.
     *
     * @param year the year
     * @return the {@link LocalDate} of Easter Sunday
     */
    private static LocalDate getEasterSundayDate(int year) {
        int a, b, c, d, e, f, g, h, i, j, k, l;
        int x, month, day;
        a = year % 19;
        b = year / 100;
        c = year % 100;
        d = b / 4;
        e = b % 4;
        f = (b + 8) / 25;
        g = (b - f + 1) / 3;
        h = (19 * a + b - d - g + 15) % 30;
        i = c / 4;
        j = c % 4;
        k = (32 + 2 * e + 2 * i - h - j) % 7;
        l = (a + 11 * h + 22 * k) / 451;
        x = h + k - 7 * l + 114;
        month = x / 31;
        day = (x % 31) + 1;

        return LocalDate.of(year, month, day);
    }

    /**
     * Computes the date of Whit Monday (Lundi de Pentecôte) given the date of Easter Sunday.
     *
     * @param easter the {@link LocalDate} of Easter Sunday
     * @return the {@link LocalDate} of Pentecost Monday
     */
    private static LocalDate getPentecostMondayHoliday(LocalDate easter) {
        return getPentecostSunday(easter).plusDays(1);
    }

    /**
     * Computes the date of Pentecost Sunday given the date of Easter Sunday (49 days after Easter Sunday).
     *
     * @param easter the {@link LocalDate} of Easter Sunday
     * @return the {@link LocalDate} of Pentecost Sunday
     */
    private static LocalDate getPentecostSunday(LocalDate easter) {
        int pentecostDay = 1;
        int pentecostMonth = 1;

        if (Month.MARCH.equals(easter.getMonth())) {
            pentecostDay = 49 - (31 - easter.getDayOfMonth() + 30);
            pentecostMonth = 5;
        }

        if (Month.APRIL.equals(easter.getMonth()) && easter.getDayOfMonth() <= 12) {
            pentecostDay = 49 - (30 - easter.getDayOfMonth());
            pentecostMonth = 5;
        }

        if (Month.APRIL.equals(easter.getMonth()) && easter.getDayOfMonth() > 12) {
            pentecostDay = 49 - (30 - easter.getDayOfMonth() + 31);
            pentecostMonth = 6;
        }

        return LocalDate.of(easter.getYear(), pentecostMonth, pentecostDay);
    }

    /**
     * Computes the date of Ascension Day given the date of Easter Sunday (39 days after Easter Sunday).
     *
     * @param easter the {@link LocalDate} of Easter Sunday
     * @return the {@link LocalDate} of Ascension Day
     */
    private static LocalDate getAscensionHoliday(LocalDate easter) {
        int ascensionDay = 1;
        int ascensionMonth = 1;
        if (easter.getDayOfMonth() == 22 && Month.MARCH.equals(easter.getMonth())) {
            ascensionDay = 30;
            ascensionMonth = 4;
        }

        if (easter.getDayOfMonth() > 22 && Month.MARCH.equals(easter.getMonth())) {
            ascensionDay = 39 - (31 - easter.getDayOfMonth() + 30);
            ascensionMonth = 5;
        }

        if (easter.getDayOfMonth() <= 22 && Month.APRIL.equals(easter.getMonth())) {
            ascensionDay = 39 - (30 - easter.getDayOfMonth());
            ascensionMonth = 5;
        }

        if (easter.getDayOfMonth() > 22 && Month.APRIL.equals(easter.getMonth())) {
            ascensionDay = 39 - (30 - easter.getDayOfMonth() + 31);
            ascensionMonth = 6;
        }

        return LocalDate.of(easter.getYear(), ascensionMonth, ascensionDay);
    }

}