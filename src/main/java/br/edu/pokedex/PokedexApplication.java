package br.edu.pokedex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class PokedexApplication {
    public static void main(String[] args) { SpringApplication.run(PokedexApplication.class, args); }

    @Bean
    CommandLineRunner importarColecao(MongoTemplate mongo, ObjectMapper mapper) {
        return args -> {
            // A coleção existente nunca é sobrescrita, mesmo quando todos os registros foram excluídos.
            if (!mongo.collectionExists(Pokemon.class)) {
                try (var input = new ClassPathResource("data/pokemons.json").getInputStream()) {
                    List<Pokemon> pokemons = mapper.readValue(input, new TypeReference<>() {});
                    mongo.insert(pokemons, Pokemon.class);
                    System.out.println("Pokédex: " + pokemons.size() + " Pokémon importados.");
                }
            }
        };
    }
}
