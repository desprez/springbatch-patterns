package fr.training.springbatch.job.classifier;

import fr.training.springbatch.job.classifier.model.PetToCreate;
import fr.training.springbatch.job.classifier.model.PetToDelete;
import fr.training.springbatch.job.classifier.model.PetToUpdate;

import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class PetService {

    private static final Logger log = LogManager.getLogger(PetService.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PetService(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public void deletePet(PetToDelete petToDelete) {
        log.info("Delete pet with name: {}", petToDelete.getName());
        var deletePetQuery = """
            DELETE FROM pet
            WHERE name = :name
            """;
        namedParameterJdbcTemplate.update(deletePetQuery, Map.of("name", petToDelete.getName()));
    }

    public void createPet(PetToCreate petToCreate) {
        log.info("Create pet with name: {}", petToCreate.getName());
        var createPetQuery = """
            INSERT INTO pet(id, name, status, species)
            VALUES (:id, :name, :status, :species)
            """;
        var queryParams = Map.of(
                "id", UUID.randomUUID(),
                "name", petToCreate.getName(),
                "status", petToCreate.getStatus().toString(),
                "species", petToCreate.getSpecies()
        );
        namedParameterJdbcTemplate.update(createPetQuery, queryParams);
    }

    public void updatePet(PetToUpdate petToUpdate) {
        log.info("Update pet with name: {}", petToUpdate.getName());
        var updatePetQuery = """
            UPDATE pet
            SET status = :status
            WHERE name = :name
            """;
        var queryParams = Map.of(
                "name", petToUpdate.getName(),
                "status", petToUpdate.getStatus().toString()
        );
        namedParameterJdbcTemplate.update(updatePetQuery, queryParams);
    }
}