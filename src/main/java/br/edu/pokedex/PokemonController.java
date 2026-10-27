package br.edu.pokedex;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {
    public static final Set<String> TYPES = Set.of("Bug", "Dark", "Dragon", "Electric", "Fairy", "Fighting", "Fire", "Flying", "Ghost", "Grass", "Ground", "Ice", "Normal", "Poison", "Psychic", "Rock", "Steel", "Water");
    private final MongoTemplate mongo;
    public PokemonController(MongoTemplate mongo) { this.mongo = mongo; }

    @GetMapping
    public Map<String, Object> listar(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String type, @RequestParam(required = false) Integer generation,
            @RequestParam(required = false) Boolean legendary, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        if (page < 0 || size < 1 || size > 100 || q.length() > 100 || page > 100000)
            throw new ResponseStatusException(BAD_REQUEST, "Paginação ou busca inválida.");
        Query query = new Query();
        if (!q.isBlank()) {
            String term = q.trim();
            Criteria name = Criteria.where("name").regex(Pattern.quote(term), "i");
            try { query.addCriteria(new Criteria().orOperator(name, Criteria.where("_id").is(Integer.parseInt(term)))); }
            catch (NumberFormatException e) { query.addCriteria(name); }
        }
        if (!type.isBlank()) query.addCriteria(Criteria.where("types").is(type));
        if (generation != null) query.addCriteria(Criteria.where("generation").is(generation));
        if (legendary != null) query.addCriteria(Criteria.where("legendary").is(legendary));
        long total = mongo.count(query, Pokemon.class);
        query.with(Sort.by("_id")).skip((long) page * size).limit(size);
        return Map.of("items", mongo.find(query, Pokemon.class), "total", total, "page", page, "size", size);
    }

    @GetMapping("/{id}")
    public Pokemon consultar(@PathVariable int id) {
        Pokemon pokemon = mongo.findById(id, Pokemon.class);
        if (pokemon == null) throw new ResponseStatusException(NOT_FOUND, "Pokémon não encontrado.");
        return pokemon;
    }

    private void validarTipos(Pokemon pokemon) {
        if (!TYPES.containsAll(pokemon.types()) || new HashSet<>(pokemon.types()).size() != pokemon.types().size())
            throw new ResponseStatusException(BAD_REQUEST, "Escolha um ou dois tipos distintos da lista.");
    }

    @PostMapping
    public ResponseEntity<Pokemon> cadastrar(@Valid @RequestBody Pokemon pokemon) {
        validarTipos(pokemon);
        return ResponseEntity.created(URI.create("/api/pokemons/" + pokemon.id())).body(mongo.insert(pokemon));
    }

    @PutMapping("/{id}")
    public Pokemon editar(@PathVariable int id, @Valid @RequestBody Pokemon pokemon) {
        if (id != pokemon.id()) throw new ResponseStatusException(BAD_REQUEST, "O número do Pokémon não pode ser alterado.");
        validarTipos(pokemon);
        Update update = new Update().set("name", pokemon.name()).set("types", pokemon.types())
            .set("legendary", pokemon.legendary()).set("hp", pokemon.hp()).set("attack", pokemon.attack())
            .set("defense", pokemon.defense()).set("speed", pokemon.speed()).set("generation", pokemon.generation());
        if (mongo.updateFirst(Query.query(Criteria.where("_id").is(id)), update, Pokemon.class).getMatchedCount() == 0)
            throw new ResponseStatusException(NOT_FOUND, "Pokémon não encontrado.");
        return pokemon;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) {
        if (mongo.remove(Query.query(Criteria.where("_id").is(id)), Pokemon.class).getDeletedCount() == 0)
            throw new ResponseStatusException(NOT_FOUND, "Pokémon não encontrado.");
        return ResponseEntity.noContent().build();
    }
}
