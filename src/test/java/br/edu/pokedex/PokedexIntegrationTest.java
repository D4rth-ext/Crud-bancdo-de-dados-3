package br.edu.pokedex;

import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PokedexIntegrationTest {
    private static final String TEST_URI = "mongodb://localhost:27017/pokedex_test_" + UUID.randomUUID().toString().replace("-", "") + "?serverSelectionTimeoutMS=5000";
    @Autowired TestRestTemplate http;
    @Autowired MongoTemplate mongo;
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> TEST_URI);
    }
    @AfterAll void cleanup() { mongo.getDb().drop(); }

    Map<String,Object> pokemon(int id) {
        return new HashMap<>(Map.of("_id", id, "name", "Teste Pokémon", "types", List.of("Fire"), "legendary", false,
            "hp", 50, "attack", 60, "defense", 70, "speed", 80, "generation", 1));
    }
    @Test void importaArquivoEConsultaComFiltros() {
        var all = http.getForEntity("/api/pokemons", Map.class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody().get("total")).isEqualTo(800);
        var result = http.getForObject("/api/pokemons?q=bulbasaur&type=Grass&generation=1&legendary=false", Map.class);
        assertThat(result.get("total")).isEqualTo(1);
        var detail = http.getForObject("/api/pokemons/1", Map.class);
        assertThat(detail.get("name")).isEqualTo("Bulbasaur");
        assertThat(detail.get("_id")).isEqualTo(1);
    }
    @Test void cadastraEditaPersisteEExclui() {
        var body=pokemon(9001);
        var created=http.postForEntity("/api/pokemons", body, Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getHeaders().getLocation().toString()).isEqualTo("/api/pokemons/9001");
        assertThat(http.postForEntity("/api/pokemons", body, Map.class).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        body.put("name", "Pokémon editado");
        assertThat(http.exchange("/api/pokemons/9001", HttpMethod.PUT,new HttpEntity<>(body),Map.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mongo.findById(9001,Pokemon.class).name()).isEqualTo("Pokémon editado");
        assertThat(http.exchange("/api/pokemons/9001",HttpMethod.DELETE,HttpEntity.EMPTY,Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(http.getForEntity("/api/pokemons/9001",Map.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test void rejeitaDadosInvalidos() {
        var body=pokemon(9002);body.put("hp",0);
        assertThat(http.postForEntity("/api/pokemons",body,Map.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        body.put("hp",50);body.put("types",List.of("Inventado"));
        assertThat(http.postForEntity("/api/pokemons",body,Map.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        body.put("types",List.of("Fire","Fire"));
        assertThat(http.postForEntity("/api/pokemons",body,Map.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    @Test void edicaoNaoCriaRegistroNemMudaNumero() {
        assertThat(http.exchange("/api/pokemons/9003",HttpMethod.PUT,new HttpEntity<>(pokemon(9003)),Map.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange("/api/pokemons/1",HttpMethod.PUT,new HttpEntity<>(pokemon(9003)),Map.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.exchange("/api/pokemons/9003",HttpMethod.DELETE,HttpEntity.EMPTY,Map.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test void paginacaoEEntradaSaoValidadas() {
        var result=http.getForObject("/api/pokemons?page=1&size=1",Map.class);
        assertThat((List<?>)result.get("items")).hasSize(1);
        assertThat(http.getForEntity("/api/pokemons?size=0",Map.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.getForEntity("/api/pokemons?q=%5B",Map.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test void entregaInterface() {
        assertThat(http.getForObject("/",String.class)).contains("Pokédex", "Novo Pokémon");
        assertThat(http.getForEntity("/app.js",String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
