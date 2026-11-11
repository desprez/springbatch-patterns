package fr.training.springbatch.job.classifier;

import fr.training.springbatch.job.BatchTestConfiguration;

import fr.training.springbatch.job.classifier.model.AdoptionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, ClassifierJobConfig.class },
        properties = { "spring.batch.job.enabled=false",
                "spring.batch.job.names=classifier-job",
                "application.classifier-job.filepath=src/main/resources/csv/pets-data.csv"})
class ClassifierJobConfigTest {

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void should_launch_job() throws Exception {
        //when
        testUtils.setJob(job);
        var jobExecution = testUtils.startJob();

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        // 22 NEW pets created + the 2 pre-existing pets updated (Harry Pawter is deleted;
        // updates targeting unknown pets are no-ops)
        var petsInDb = jdbcTemplate.queryForList("SELECT name, status, species FROM pet");
        assertThat(petsInDb)
                .hasSize(24)
                .contains(
                        petMap("Isaac Mewton", AdoptionStatus.NEW, "cat"),
                        petMap("Chris P. Bacon", AdoptionStatus.NEW, "pig"),
                        petMap("Frodog", AdoptionStatus.WAITING, "dog"),
                        petMap("Meowy Poppins", AdoptionStatus.AVAILABLE, "cat")
                )
                .doesNotContain(petMap("Harry Pawter", AdoptionStatus.WAITING, "dog"));
    }

    private static Map<String, Object> petMap(String name, AdoptionStatus status, String species) {
        return Map.of("NAME", name, "STATUS", status.toString(), "SPECIES", species);
    }
}