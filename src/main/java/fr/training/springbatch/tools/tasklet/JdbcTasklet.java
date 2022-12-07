/*
 * Copyright 2006-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.training.springbatch.tools.tasklet;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;

/**
 * @author Dave Syer,
 * @author Christian Tzolov
 * @author Thomas Risberg
 *
 */
public class JdbcTasklet implements Tasklet, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(JdbcTasklet.class);

    private NamedParameterJdbcTemplate jdbcTemplate;

    private String sql;

    /**
     * An SQL query to execute in the tasklet. The query can be a select, update, delete or insert, and it can contain embedded query parameters using a
     * {@link BeanPropertySqlParameterSource} whose root context is the step context. So for example
     *
     * <pre>
     * DELETE from LEAD_INPUTS where ID=:jobParameters[idToDelete]
     * </pre>
     *
     * Note that the syntax for the named parameters is different from and not as flexible as Spring EL. So it might be better anyway if possible to use late
     * binding to push step context properties into the query, e.g. this will work in a bean definition which is step scoped:
     *
     * <pre>
     * <bean id="tasklet" class="org...JdbcTasklet" scope="step">
     *   <property name="sql">
     *     <value>
     * DELETE from LEAD_INPUTS where ID=#{jobParameters['i.to.delete']?:-1}
     *     </value>
     *   </property>
     * </bean>
     * </pre>
     *
     * @see BeanPropertySqlParameterSource
     *
     * @param sql
     *            the sql to set
     */
    public void setSql(final String sql) {
        this.sql = sql;
    }

    /**
     * @param dataSource
     *            the dataSource to set
     */
    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(jdbcTemplate != null, "A DataSource must be provided");
        Assert.state(sql != null, "A SQL query must be provided");
    }

    /**
     * Execute the {@link #setSql(String) SQL query} provided. If the query starts with "select" (case insensitive) the result is a list of maps, which is
     * logged and added to the step execution exit status. Otherwise the query is executed and the result is an indication, also in the exit status, of the
     * number of rows updated.
     */
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {

        final StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        final ExitStatus exitStatus = stepExecution.getExitStatus();
        String msg = "";
        if (isSelect(sql)) {
            log.debug("Executing: " + sql);
            final List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, new BeanPropertySqlParameterSource(chunkContext.getStepContext()));
            msg = "Result: " + result;
        } else if (isUpdate(sql)) {
            log.debug("Updating : " + sql);
            final int updated = jdbcTemplate.update(sql, new BeanPropertySqlParameterSource(chunkContext.getStepContext()));
            contribution.incrementWriteCount(updated);
            msg = "Updated: " + updated + " rows";
        }
        log.debug(msg);
        stepExecution.setExitStatus(exitStatus.addExitDescription(msg));

        return RepeatStatus.FINISHED;

    }

    /**
     *
     * @param sqlCommand
     * @return
     */
    private boolean isUpdate(final String sqlCommand) {
        final String upperCaseCommand = sqlCommand.trim().toUpperCase();
        return upperCaseCommand.startsWith("UPDATE") || upperCaseCommand.startsWith("DELETE");
    }

    /**
     * @param sqlCommand
     * @return
     */
    private static boolean isSelect(final String sqlCommand) {
        return sqlCommand.trim().toUpperCase().startsWith("SELECT");
    }

}
