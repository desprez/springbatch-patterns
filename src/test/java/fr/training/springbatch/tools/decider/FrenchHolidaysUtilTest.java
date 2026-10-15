package fr.training.springbatch.tools.decider;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FrenchHolidaysUtilTest {

    @Test
    void getBusinessDays_should_compute_days_between_two_dates_no_weekend() {
        // Given
        LocalDate startDate = LocalDate.of(2016, 1, 6);
        LocalDate endDate = LocalDate.of(2016, 1, 7);

        // When
        int result = FrenchHolidaysUtil.getWorkingDays(startDate, endDate);

        // Then
        assertEquals(1, result);
    }

    @Test
    void getBusinessDays_should_compute_days_between_two_dates_with_weekend() {
        // Given
        LocalDate startDate = LocalDate.of(2016, 1, 6);
        LocalDate endDate = LocalDate.of(2016, 1, 12);

        // When
        int result = FrenchHolidaysUtil.getWorkingDays(startDate, endDate);

        // Then
        assertEquals(4, result);
    }

    @Test
    void getBusinessDays_should_compute_days_between_two_dates() {
        // Given
        LocalDate startDate = LocalDate.of(2016, 1, 6);
        LocalDate endDate = LocalDate.of(2016, 2, 15);

        // When
        int result = FrenchHolidaysUtil.getWorkingDays(startDate, endDate);

        // Then
        assertEquals(28, result);
    }

    @Test
    void getBusinessDays_should_compute_days_between_two_dates_ends_on_weekend_day() {
        // Given
        LocalDate startDate = LocalDate.of(2016, 2, 26);
        LocalDate endDate = LocalDate.of(2016, 2, 27);

        // When
        int result = FrenchHolidaysUtil.getWorkingDays(startDate, endDate);

        // Then
        assertEquals(1, result);
    }

    @Test
    void isHoliday_newYear() {
        // Given
        LocalDate date = LocalDate.of(2016, 1, 1);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_workday() {
        // Given
        LocalDate date = LocalDate.of(2016, 1, 2);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertFalse(result);
    }

    @Test
    void isHoliday_feteTravail() {
        // Given
        LocalDate date = LocalDate.of(2016, 5, 1);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_victoire1945() {
        // Given
        LocalDate date = LocalDate.of(2016, 5, 8);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_bastilleDay() {
        // Given
        LocalDate date = LocalDate.of(2016, 7, 14);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_assomption() {
        // Given
        LocalDate date = LocalDate.of(2016, 8, 15);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_armistice1918() {
        // Given
        LocalDate date = LocalDate.of(2016, 11, 11);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_christmas() {
        // Given
        LocalDate date = LocalDate.of(2016, 12, 25);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_easter_2016() {
        // Given
        LocalDate date = LocalDate.of(2016, 3, 28);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_easter_2017() {
        // Given
        LocalDate date = LocalDate.of(2017, 4, 17);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_easter_2018() {
        // Given
        LocalDate date = LocalDate.of(2018, 4, 2);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_ascension_2015() {
        // Given
        LocalDate date = LocalDate.of(2015, 5, 14);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_ascension_2016() {
        // Given
        LocalDate date = LocalDate.of(2016, 5, 5);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_ascension_2011() {
        // Given
        LocalDate date = LocalDate.of(2011, 6, 2);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_ascension_2008() {
        // Given
        LocalDate date = LocalDate.of(2008, 5, 1);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_pentecost_2015() {
        // Given
        LocalDate date = LocalDate.of(2015, 5, 25);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_pentecost_2016() {
        // Given
        LocalDate date = LocalDate.of(2016, 5, 16);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void isHoliday_pentecost_2017() {
        // Given
        LocalDate date = LocalDate.of(2017, 6, 5);

        // When
        boolean result = FrenchHolidaysUtil.isHoliday(date);

        // Then
        assertTrue(result);
    }

    @Test
    void getHolidays_should_return_all_eleven_french_public_holidays() {
        // Given
        int year = 2026;

        // When
        Map<LocalDate, String> holidays = FrenchHolidaysUtil.getHolidays(year);

        // Then
        assertNotNull(holidays);
        assertEquals(11, holidays.size());
        assertEquals("Jour de l'an", holidays.get(LocalDate.of(2026, 1, 1)));
        assertEquals("Lundi de Pâques", holidays.get(LocalDate.of(2026, 4, 6)));
        assertEquals("Fête du travail", holidays.get(LocalDate.of(2026, 5, 1)));
        assertEquals("Victoire 1945", holidays.get(LocalDate.of(2026, 5, 8)));
        assertEquals("Jeudi de l'Ascension", holidays.get(LocalDate.of(2026, 5, 14)));
        assertEquals("Lundi de Pentecôte", holidays.get(LocalDate.of(2026, 5, 25)));
        assertEquals("Fête nationale", holidays.get(LocalDate.of(2026, 7, 14)));
        assertEquals("Assomption", holidays.get(LocalDate.of(2026, 8, 15)));
        assertEquals("Toussaint", holidays.get(LocalDate.of(2026, 11, 1)));
        assertEquals("Armistice 1918", holidays.get(LocalDate.of(2026, 11, 11)));
        assertEquals("Noël", holidays.get(LocalDate.of(2026, 12, 25)));
    }

    @Test
    void isWorkingDay_should_return_true_for_regular_weekday() {
        // Given a regular Friday (2024-03-15)
        LocalDate friday = LocalDate.of(2024, 3, 15);

        // When / Then
        assertTrue(FrenchHolidaysUtil.isWorkingDay(friday));
    }

    @Test
    void isWorkingDay_should_return_false_for_saturday() {
        // Given a Saturday (2024-03-16)
        LocalDate saturday = LocalDate.of(2024, 3, 16);

        // When / Then
        assertFalse(FrenchHolidaysUtil.isWorkingDay(saturday));
    }

    @Test
    void isWorkingDay_should_return_false_for_sunday() {
        // Given a Sunday (2024-03-17)
        LocalDate sunday = LocalDate.of(2024, 3, 17);

        // When / Then
        assertFalse(FrenchHolidaysUtil.isWorkingDay(sunday));
    }

    @Test
    void isWorkingDay_should_return_false_for_holiday_on_weekday() {
        // Given a French holiday on a Wednesday (Fête du travail: 2024-05-01)
        LocalDate laborDay = LocalDate.of(2024, 5, 1);

        // When / Then
        assertFalse(FrenchHolidaysUtil.isWorkingDay(laborDay));
    }

    @Test
    void isBusinessDay_should_return_true_for_regular_weekday() {
        // Given a regular Friday (2024-03-15)
        LocalDate friday = LocalDate.of(2024, 3, 15);

        // When / Then
        assertTrue(FrenchHolidaysUtil.isBusinessDay(friday));
    }

    @Test
    void isBusinessDay_should_return_true_for_saturday() {
        // Given a Saturday (2024-03-16)
        LocalDate saturday = LocalDate.of(2024, 3, 16);

        // When / Then
        assertTrue(FrenchHolidaysUtil.isBusinessDay(saturday));
    }

    @Test
    void isBusinessDay_should_return_false_for_sunday() {
        // Given a Sunday (2024-03-17)
        LocalDate sunday = LocalDate.of(2024, 3, 17);

        // When / Then
        assertFalse(FrenchHolidaysUtil.isBusinessDay(sunday));
    }

    @Test
    void isBusinessDay_should_return_false_for_holiday() {
        // Given a French holiday on a Wednesday (Fête du travail: 2024-05-01)
        LocalDate laborDay = LocalDate.of(2024, 5, 1);

        // When / Then
        assertFalse(FrenchHolidaysUtil.isBusinessDay(laborDay));
    }

    @Test
    void getLastWorkingDayOfTheMonth_when_month_ends_on_friday() {
        // May 31, 2024 is a Friday
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheMonth(Month.MAY, 2024);

        // When / Then
        assertEquals(LocalDate.of(2024, 5, 31), result);
    }

    @Test
    void getLastWorkingDayOfTheMonth_when_month_ends_on_saturday() {
        // August 31, 2024 is a Saturday -> last working day is Friday August 30, 2024
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheMonth(Month.AUGUST, 2024);

        // When / Then
        assertEquals(LocalDate.of(2024, 8, 30), result);
    }

    @Test
    void getLastWorkingDayOfTheMonth_when_month_ends_on_sunday() {
        // June 30, 2024 is a Sunday -> last working day is Friday June 28, 2024
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheMonth(Month.JUNE, 2024);

        // When / Then
        assertEquals(LocalDate.of(2024, 6, 28), result);
    }

    @Test
    void getLastWorkingDayOfTheMonth_for_february_in_leap_year() {
        // February 29, 2024 is a Thursday
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheMonth(Month.FEBRUARY, 2024);

        // When / Then
        assertEquals(LocalDate.of(2024, 2, 29), result);
    }

    @Test
    void getLastWorkingDayOfTheMonth_for_february_in_non_leap_year() {
        // February 28, 2023 is a Tuesday
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheMonth(Month.FEBRUARY, 2023);

        // When / Then
        assertEquals(LocalDate.of(2023, 2, 28), result);
    }

    @Test
    void getLastWorkingDayOfTheWeek_when_friday_is_regular_working_day() {
        // Week 11 of 2024: March 11 to March 17, 2024 -> Friday March 15, 2024
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheWeek(11, 2024);

        // When / Then
        assertEquals(LocalDate.of(2024, 3, 15), result);
    }

    @Test
    void getLastWorkingDayOfTheWeek_when_friday_is_french_holiday() {
        // Week 28 of 2023: July 10 to July 16, 2023. July 14 is Fête nationale -> Thursday July 13, 2023
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheWeek(28, 2023);

        // When / Then
        assertEquals(LocalDate.of(2023, 7, 13), result);
    }

    @Test
    void getLastWorkingDayOfTheWeek_for_week_53_with_holiday_on_friday() {
        // Week 53 of 2020: Dec 28, 2020 to Jan 3, 2021. Friday Jan 1 is Jour de l'an -> Thursday Dec 31, 2020
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheWeek(53, 2020);

        // When / Then
        assertEquals(LocalDate.of(2020, 12, 31), result);
    }

    @Test
    void getLastWorkingDayOfTheWeek_for_week_1() {
        // Week 1 of 2024: Jan 1 to Jan 7, 2024. Jan 1 is holiday -> Friday Jan 5, 2024
        LocalDate result = FrenchHolidaysUtil.getLastWorkingDayOfTheWeek(1, 2024);

        // When / Then
        assertEquals(LocalDate.of(2024, 1, 5), result);
    }

}