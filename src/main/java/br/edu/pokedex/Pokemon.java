package br.edu.pokedex;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("pokemons")
public record Pokemon(
    @Id @JsonProperty("_id") @NotNull @Positive Integer id,
    @NotEmpty @Size(max = 2) List<@NotBlank String> types,
    @NotBlank @Size(max = 100) String name,
    @NotNull Boolean legendary,
    @NotNull @Min(1) @Max(255) Integer hp,
    @NotNull @Min(1) @Max(255) Integer attack,
    @NotNull @Min(1) @Max(255) Integer defense,
    @NotNull @Min(1) @Max(255) Integer speed,
    @NotNull @Min(1) @Max(9) Integer generation
) {}
