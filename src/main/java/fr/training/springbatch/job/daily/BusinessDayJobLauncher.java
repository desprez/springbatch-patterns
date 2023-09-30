package fr.training.springbatch.job.daily;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

public class BusinessDayJobLauncher extends SimpleJobLauncher {

    private static final Logger logger = LoggerFactory.getLogger(BusinessDayJobLauncher.class);

    @Override
    public JobExecution run(final Job job, final JobParameters jobParameters)
            throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {

        if (isBusinessDay(LocalDate.now())) {
            logger.debug("Today is a working day : launching job {}", job.getName());
            return super.run(job, jobParameters);
        }
        logger.warn("Today is not a working day : canceling job {} launch", job.getName());
        return null;
    }

    private boolean isBusinessDay(final LocalDate now) {
        return isWorkingDay(now) && !isPublicHoliday(now);
    }

    public static boolean isPublicHoliday(final LocalDate date) {
        return publicHolidays(date.getYear()).contains(date);
    }

    public static boolean isWorkingDay(final LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SUNDAY && date.getDayOfWeek() != DayOfWeek.SATURDAY;
    }

    /**
     * Populate a list of french public Holidays according to the given year.
     *
     * @param year
     *            the year
     * @return a list of french public Holidays.
     */
    public static List<LocalDate> publicHolidays(final int year) {
        return Arrays.asList(newYearSDay(year), easterMonday(year), laborDay(year), wordWarIIVictoryDay(year), ascensionDay(year), whitMonday(year),
                bastilleDay(year), assumption(year), allSaintsDay(year), armisticeDay(year), christmasDay(year));
    }

    /**
     * Compute Easter Day according to the given year.
     *
     * @see <a href=https://fr.wikipedia.org/wiki/Calcul_de_la_date_de_P%C3%A2ques>Article Wikipedia sur le calcul de la date de paques</a>
     * @param year
     *            the year
     * @return the Easter Day of the year
     */
    public static LocalDate easterDay(final int year) {
        // cycle de Méton
        final int n = year % 19;
        // centaine de l'year
        final int c = year / 100;
        // rang de l'year
        final int u = year % 100;
        final int s = c / 4;
        final int t = c % 4;
        // cycle de proemtose
        final int p = (c + 8) / 25;
        // proemptose
        final int q = (c - p + 1) / 3;
        // épacte
        final int e = (19 * n + c - s - q + 15) % 30;
        final int b = u / 4;
        final int d = u % 4;
        // lettre dominicale
        final int l = (32 + 2 * t + 2 * b - e - d) % 7;
        // correction
        final int h = (n + 11 * e + 22 * l) / 451;
        final int f = e + l - 7 * h + 114;
        final int mois = f / 31;
        final int jours = f % 31 + 1;

        return LocalDate.of(year, mois, jours);
    }

    /**
     * Easter Monday = 1 day after Easter sunday.
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate easterMonday(final int year) {
        return easterDay(year).plusDays(1);
    }

    /**
     * Ascension Day = 39 days after Easter day.
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate ascensionDay(final int year) {
        return easterDay(year).plusDays(39);
    }

    /**
     * Whit Monday = 50 days after Easter day.
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate whitMonday(final int year) {
        return easterDay(year).plusDays(50);
    }

    /**
     * New Year's Day
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate newYearSDay(final int year) {
        return LocalDate.of(year, Month.JANUARY, 1);
    }

    /**
     * Labor Day = 1st of May of the year
     *
     * @param year
     *            demandée
     * @return a new {@link LocalDate}
     */
    public static LocalDate laborDay(final int year) {
        return LocalDate.of(year, Month.MAY, 1);
    }

    /**
     * Word War II VictoryDay = 8th of May of the year
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate wordWarIIVictoryDay(final int year) {
        return LocalDate.of(year, Month.MAY, 8);
    }

    /**
     * Bastille Day = 14th of July of the year
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate bastilleDay(final int year) {
        return LocalDate.of(year, Month.JULY, 14);
    }

    /**
     * Saints Day = the 1st of November of the year
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate allSaintsDay(final int year) {
        return LocalDate.of(year, Month.NOVEMBER, 1);
    }

    /**
     * Assumption = the 15st of August of the year
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate assumption(final int year) {
        return LocalDate.of(year, Month.AUGUST, 15);
    }

    /**
     * Armistice = 11th of November of the year
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate armisticeDay(final int year) {
        return LocalDate.of(year, Month.NOVEMBER, 11);
    }

    /**
     * Christmas Day = 25th of December of the year
     *
     * @param year
     *            the year
     * @return a new {@link LocalDate}
     */
    public static LocalDate christmasDay(final int year) {
        return LocalDate.of(year, Month.DECEMBER, 25);
    }
}
