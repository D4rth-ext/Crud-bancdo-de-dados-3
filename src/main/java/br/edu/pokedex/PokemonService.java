package br.edu.pokedex;

import jakarta.validation.Validator;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

@Service
public class PokemonService {
    public static final List<String> TYPES = List.of("Bug", "Dark", "Dragon", "Electric", "Fairy", "Fighting", "Fire", "Flying", "Ghost", "Grass", "Ground", "Ice", "Normal", "Poison", "Psychic", "Rock", "Steel", "Water");
    private final MongoTemplate mongo;
    private final Validator validator;

    public PokemonService(MongoTemplate mongo, Validator validator) {
        this.mongo = mongo;
        this.validator = validator;
    }

    public long total() { return mongo.count(new Query(), Pokemon.class); }

    public List<Pokemon> listar(int pagina) {
        if (pagina < 0) throw new IllegalArgumentException("Página inválida.");
        return mongo.find(new Query().with(Sort.by("_id")).skip((long) pagina * 20).limit(20), Pokemon.class);
    }

    public List<Pokemon> buscar(String nome) {
        if (nome.isBlank()) throw new IllegalArgumentException("Digite um nome para consultar.");
        return mongo.find(Query.query(Criteria.where("name").regex(Pattern.quote(nome.trim()), "i")).with(Sort.by("_id")), Pokemon.class);
    }

    public Pokemon consultar(int id) {
        Pokemon pokemon = mongo.findById(id, Pokemon.class);
        if (pokemon == null) throw new IllegalArgumentException("Pokémon não encontrado.");
        return pokemon;
    }

    private void validar(Pokemon pokemon) {
        if (!validator.validate(pokemon).isEmpty())
            throw new IllegalArgumentException("Dados inválidos: número positivo, nome até 100 caracteres, 1 ou 2 tipos, atributos de 1 a 255 e geração de 1 a 9.");
        if (!TYPES.containsAll(pokemon.types()) || new HashSet<>(pokemon.types()).size() != pokemon.types().size())
            throw new IllegalArgumentException("Escolha um ou dois tipos distintos da lista.");
    }

    public Pokemon cadastrar(Pokemon pokemon) {
        validar(pokemon);
        return mongo.insert(pokemon);
    }

    public Pokemon editar(int id, Pokemon pokemon) {
        validar(pokemon);
        if (id != pokemon.id()) throw new IllegalArgumentException("O número não pode ser alterado.");
        Update update = new Update().set("name", pokemon.name()).set("types", pokemon.types())
            .set("legendary", pokemon.legendary()).set("hp", pokemon.hp()).set("attack", pokemon.attack())
            .set("defense", pokemon.defense()).set("speed", pokemon.speed()).set("generation", pokemon.generation());
        if (mongo.updateFirst(Query.query(Criteria.where("_id").is(id)), update, Pokemon.class).getMatchedCount() == 0)
            throw new IllegalArgumentException("Pokémon não encontrado.");
        return pokemon;
    }

    public void excluir(int id) {
        if (mongo.remove(Query.query(Criteria.where("_id").is(id)), Pokemon.class).getDeletedCount() == 0)
            throw new IllegalArgumentException("Pokémon não encontrado.");
    }
}
