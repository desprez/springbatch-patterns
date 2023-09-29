package fr.training.springbatch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchApplication {

    @Value("${spring.batch.job.names:#{null}}")
    private String jobName;

    public static void main(final String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(BatchApplication.class, args)));
    }

}
