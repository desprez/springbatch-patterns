package fr.training.springbatch.job.partition.jdbc;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Simple minded partitioner for a range of values of a column in a database table. Works best if the values are uniformly distributed (e.g. auto-generated
 * primary key values).
 *
 * @author Dave Syer
 */
public class ColumnRangePartitioner implements Partitioner {
    private JdbcOperations jdbcTemplate;
    private String table;
    private String column;

    /**
     * The name of the SQL table the data are in.
     *
     * @param table
     *            the name of the table
     */
    public void setTable(final String table) {
        this.table = table;
    }

    /**
     * The name of the column to partition.
     *
     * @param column
     *            the column name.
     */
    public void setColumn(final String column) {
        this.column = column;
    }

    /**
     * The data source for connecting to the database.
     *
     * @param dataSource
     *            a {@link DataSource}
     */
    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Partition a database table assuming that the data in the column specified are uniformly distributed. The execution context values will have keys
     * <code>minValue</code> and <code>maxValue</code> specifying the range of values to consider in each partition.
     *
     * @see Partitioner#partition(int)
     */
    @Override
    public Map<String, ExecutionContext> partition(final int gridSize) {
        final int min = jdbcTemplate.queryForObject("SELECT MIN(" + column + ") FROM " + table, Integer.class);

        final int max = jdbcTemplate.queryForObject("SELECT MAX(" + column + ") FROM " + table, Integer.class);

        final int targetSize = (max - min) / gridSize + 1;

        final Map<String, ExecutionContext> result = new HashMap<>();

        int number = 0;
        int start = min;
        int end = start + targetSize - 1;

        while (start <= max) {
            final ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }

            value.putInt("minValue", start);
            value.putInt("maxValue", end);

            start += targetSize;
            end += targetSize;

            number++;
        }
        return result;
    }
}