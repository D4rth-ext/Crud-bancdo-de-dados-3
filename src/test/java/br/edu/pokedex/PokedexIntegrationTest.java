package br.edu.pokedex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "pokedex.console.enabled=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PokedexIntegrationTest {
    static final String URI = "mongodb://localhost:27017/pokedex_test_" + UUID.randomUUID().toString().replace("-", "") + "?serverSelectionTimeoutMS=5000";
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> URI);
    }
    @Autowired PokemonService service;
    @Autowired MongoTemplate mongo;
    @AfterAll void cleanup() { mongo.getDb().drop(); }

    Pokemon pokemon(int id, String name) { return new Pokemon(id, List.of("Fire"), name, false, 50, 60, 70, 80, 1); }
    String console(String input) {
        var buffer = new ByteArrayOutputStream();
        new PokedexConsole(service, new Scanner(input), new PrintStream(buffer, true, StandardCharsets.UTF_8)).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test void importaEBuscaColecao() {
        assertThat(service.total()).isEqualTo(800);
        assertThat(service.buscar("bulba")).extracting(Pokemon::name).containsExactly("Bulbasaur");
        assertThat(service.consultar(1).hp()).isEqualTo(45);
        assertThat(service.listar(0)).hasSize(20);
        assertThat(service.listar(1).getFirst().id()).isEqualTo(21);
        assertThat(service.buscar("[")).isEmpty();
    }
    @Test void crudPersisteNoMongo() {
        try {
            service.cadastrar(pokemon(9001, "Novo Pokémon"));
            assertThatThrownBy(() -> service.cadastrar(pokemon(9001, "Duplicado"))).isInstanceOf(DuplicateKeyException.class);
            service.editar(9001, pokemon(9001, "Editado"));
            assertThat(mongo.findById(9001, Pokemon.class).name()).isEqualTo("Editado");
            service.excluir(9001);
            assertThatThrownBy(() -> service.consultar(9001)).hasMessage("Pokémon não encontrado.");
        } finally { mongo.remove(org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where("_id").is(9001)), Pokemon.class); }
    }
    @Test void validaDadosENaoCriaNaEdicao() {
        assertThatThrownBy(() -> service.cadastrar(new Pokemon(9002, List.of("Inventado"), "Teste", false, 50, 60, 70, 80, 1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.cadastrar(new Pokemon(9002, List.of("Fire"), "Teste", false, 0, 60, 70, 80, 1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.editar(9002, pokemon(9002, "Não existe"))).hasMessage("Pokémon não encontrado.");
        assertThatThrownBy(() -> service.editar(1, pokemon(9002, "Outro número"))).hasMessage("O número não pode ser alterado.");
    }
    @Test void terminalExecutaCrudCompleto() {
        String output = console("4\n9003\nTeste terminal\nfire\nn\n50\n60\n70\n80\n1\n2\nTeste terminal\n3\n9003\n5\n9003\nEditado terminal\n\n\n\n\n\n\n\n6\n9003\ns\n0\n");
        assertThat(output).contains("Pokémon cadastrado com sucesso!", "Teste terminal", "HP: 50", "Pokémon atualizado com sucesso!", "Editado terminal", "Pokémon excluído com sucesso!", "Até a próxima");
        assertThatThrownBy(() -> service.consultar(9003)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void terminalTrataErrosECancelamento() {
        String output = console("x\n3\nabc\n99999\n2\nSemResultado123\n6\n1\nn\n0\n");
        assertThat(output).contains("Opção inválida", "Digite um número inteiro", "Pokémon não encontrado", "Nenhum Pokémon encontrado", "Exclusão cancelada");
        assertThat(service.consultar(1).name()).isEqualTo("Bulbasaur");
    }
    @Test void terminalSaiQuandoEntradaTermina() {
        assertThat(console("")).contains("Entrada encerrada");
    }
}
