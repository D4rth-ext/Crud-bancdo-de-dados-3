package br.edu.pokedex;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@ConditionalOnProperty(name = "pokedex.console.enabled", havingValue = "true", matchIfMissing = true)
public class PokedexConsole implements CommandLineRunner {
    private final PokemonService service;
    private final Scanner input;
    private final PrintStream output;

    @org.springframework.beans.factory.annotation.Autowired
    public PokedexConsole(PokemonService service) {
        this(service, new Scanner(System.in, StandardCharsets.UTF_8), System.out);
    }

    PokedexConsole(PokemonService service, Scanner input, PrintStream output) {
        this.service = service;
        this.input = input;
        this.output = output;
    }

    @Override
    public void run(String... args) {
        output.println("\nPOKÉDEX — Java + MongoDB");
        try {
            while (true) {
                output.println("\n1 - Listar Pokémon\n2 - Buscar por nome\n3 - Consultar ficha por número\n4 - Cadastrar Pokémon\n5 - Editar Pokémon\n6 - Excluir Pokémon\n0 - Sair");
                String option = ler("Opção: ");
                try {
                    switch (option) {
                        case "1" -> listar();
                        case "2" -> mostrarResultados(service.buscar(ler("Nome ou parte do nome: ")));
                        case "3" -> ficha(service.consultar(numero("Número: ", 1, Integer.MAX_VALUE)));
                        case "4" -> { service.cadastrar(formulario(null)); output.println("Pokémon cadastrado com sucesso!"); }
                        case "5" -> editar();
                        case "6" -> excluir();
                        case "0" -> { output.println("Até a próxima, treinador!"); return; }
                        default -> output.println("Opção inválida. Escolha um número do menu.");
                    }
                } catch (DuplicateKeyException e) {
                    output.println("Já existe um Pokémon com esse número.");
                } catch (IllegalArgumentException e) {
                    output.println(e.getMessage());
                } catch (DataAccessException e) {
                    output.println("Não foi possível acessar o MongoDB. Confira se o servidor está rodando.");
                }
            }
        } catch (NoSuchElementException e) {
            output.println("\nEntrada encerrada. Até a próxima!");
        }
    }

    private String ler(String prompt) {
        output.print(prompt);
        output.flush();
        return input.nextLine().trim();
    }

    private int numero(String prompt, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(ler(prompt));
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            output.printf("Digite um número inteiro entre %d e %d.%n", min, max);
        }
    }

    private void listar() {
        long total = service.total();
        if (total == 0) { output.println("Nenhum Pokémon cadastrado."); return; }
        int pagina = numero("Página (1 a " + ((total + 19) / 20) + "): ", 1, (int) ((total + 19) / 20));
        mostrarResultados(service.listar(pagina - 1));
        output.println("Total na coleção: " + total + ". Cada página mostra até 20 Pokémon.");
    }

    private void mostrarResultados(List<Pokemon> pokemons) {
        if (pokemons.isEmpty()) { output.println("Nenhum Pokémon encontrado."); return; }
        for (Pokemon p : pokemons) output.printf("#%03d | %-24s | %-18s | Geração %d%s%n", p.id(), p.name(), String.join("/", p.types()), p.generation(), p.legendary() ? " | Lendário" : "");
        output.println("Use a opção 3 para abrir a ficha pelo número da coleção.");
    }

    private void ficha(Pokemon p) {
        output.printf("%n===== #%03d — %s =====%nTipos: %s%nGeração: %d%nLendário: %s%nHP: %d%nAtaque: %d%nDefesa: %d%nVelocidade: %d%n", p.id(), p.name(), String.join(" / ", p.types()), p.generation(), p.legendary() ? "Sim" : "Não", p.hp(), p.attack(), p.defense(), p.speed());
    }

    private Pokemon formulario(Pokemon anterior) {
        if (anterior != null) output.println("Preencha os novos dados. Pressione Enter para manter o valor atual.");
        int id = anterior == null ? numero("Número na coleção: ", 1, Integer.MAX_VALUE) : anterior.id();
        String nome;
        do { nome = campo("Nome", anterior == null ? null : anterior.name()); }
        while (nome.isBlank() || nome.length() > 100);
        output.println("Tipos disponíveis: " + String.join(", ", PokemonService.TYPES));
        List<String> tipos;
        while (true) {
            String valor = campo("Tipos separados por vírgula", anterior == null ? null : String.join(",", anterior.types()));
            tipos = Arrays.stream(valor.split(",", -1)).map(String::trim)
                .map(t -> PokemonService.TYPES.stream().filter(tipo -> tipo.equalsIgnoreCase(t)).findFirst().orElse(t)).toList();
            if (tipos.size() >= 1 && tipos.size() <= 2 && PokemonService.TYPES.containsAll(tipos) && new HashSet<>(tipos).size() == tipos.size()) break;
            output.println("Informe um ou dois tipos válidos e distintos. Exemplo: Grass,Poison");
        }
        boolean lendario;
        while (true) {
            String valor = campo("Lendário (s/n)", anterior == null ? null : anterior.legendary() ? "s" : "n");
            if (valor.equalsIgnoreCase("s") || valor.equalsIgnoreCase("n")) { lendario = valor.equalsIgnoreCase("s"); break; }
            output.println("Digite s para sim ou n para não.");
        }
        int hp = atributo("HP", anterior == null ? null : anterior.hp(), 255);
        int ataque = atributo("Ataque", anterior == null ? null : anterior.attack(), 255);
        int defesa = atributo("Defesa", anterior == null ? null : anterior.defense(), 255);
        int velocidade = atributo("Velocidade", anterior == null ? null : anterior.speed(), 255);
        int geracao = atributo("Geração", anterior == null ? null : anterior.generation(), 9);
        return new Pokemon(id, tipos, nome, lendario, hp, ataque, defesa, velocidade, geracao);
    }

    private String campo(String nome, String atual) {
        String valor = ler(nome + (atual == null ? "" : " [" + atual + "]") + ": ");
        return valor.isBlank() && atual != null ? atual : valor;
    }

    private int atributo(String nome, Integer atual, int max) {
        while (true) {
            try {
                int valor = Integer.parseInt(campo(nome + " (1–" + max + ")", atual == null ? null : atual.toString()));
                if (valor >= 1 && valor <= max) return valor;
            } catch (NumberFormatException ignored) { }
            output.println("Valor inválido. Digite um inteiro entre 1 e " + max + ".");
        }
    }

    private void editar() {
        Pokemon atual = service.consultar(numero("Número do Pokémon para editar: ", 1, Integer.MAX_VALUE));
        ficha(atual);
        service.editar(atual.id(), formulario(atual));
        output.println("Pokémon atualizado com sucesso!");
    }

    private void excluir() {
        Pokemon atual = service.consultar(numero("Número do Pokémon para excluir: ", 1, Integer.MAX_VALUE));
        ficha(atual);
        if (ler("Excluir " + atual.name() + "? Essa ação é definitiva. (s/n): ").equalsIgnoreCase("s")) {
            service.excluir(atual.id());
            output.println("Pokémon excluído com sucesso!");
        } else output.println("Exclusão cancelada.");
    }
}
