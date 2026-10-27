# Pokédex — Java e MongoDB

Aplicação web em português para consultar e gerenciar Pokémon, usando **Java 21, Spring Boot e MongoDB**.

## Funcionalidades

- Importação dos **800 registros** do arquivo `pokemons.json` enviado, somente se a coleção ainda não existir.
- Busca por nome ou número; filtros por tipo, geração e lendário; paginação.
- Fichas com HP, ataque, defesa e velocidade.
- Cadastro, edição e exclusão com confirmação, validação e mensagens de erro.
- Persistência na coleção `pokemons`, banco `pokedex`.

O `_id` é o número da coleção original, não necessariamente a numeração oficial da Pokédex: o conjunto inclui formas Mega. Os campos originais são preservados.

## Executar

Instale **JDK 21**, **Maven 3.9+** e **Docker com Compose**. Configure `JAVA_HOME` para o JDK 21. No terminal, dentro da pasta do projeto:

```bash
docker compose up -d --wait
mvn clean verify
java -jar target/pokedex-1.0.0.jar
```

Abra o navegador no endereço local da aplicação, porta **8080**. Para encerrar, pressione `Ctrl+C`. Para parar o MongoDB mantendo os dados:

```bash
docker compose down
```

Durante o desenvolvimento, pode usar `mvn spring-boot:run` no lugar de `java -jar`.

### Banco já instalado

Também é possível usar MongoDB local sem Docker. A URI padrão é `mongodb://localhost:27017/pokedex?serverSelectionTimeoutMS=5000`. Configure `MONGODB_URI` no ambiente para outro servidor. Nunca salve senhas no Git. A porta HTTP pode ser alterada com `PORT`.

A aplicação escuta apenas na máquina local por padrão. É um projeto de estudo, sem autenticação de usuários; exposição pública requer autenticação, autorização e HTTPS.

## API REST

| Método | Rota | Operação |
| --- | --- | --- |
| GET | `/api/pokemons?q=bulba&type=Grass&generation=1&legendary=false&page=0&size=24` | Busca com filtros |
| GET | `/api/pokemons/1` | Consulta individual |
| POST | `/api/pokemons` | Cadastro |
| PUT | `/api/pokemons/1` | Atualização |
| DELETE | `/api/pokemons/1` | Exclusão |

Exemplo de corpo para POST/PUT:

```json
{
  "_id": 801,
  "types": ["Grass", "Poison"],
  "name": "Meu Pokémon",
  "legendary": false,
  "hp": 45,
  "attack": 49,
  "defense": 49,
  "speed": 45,
  "generation": 1
}
```

O número é positivo, único e não pode mudar na edição. Nome obrigatório, até 100 caracteres; um ou dois tipos oficiais distintos; atributos de 1 a 255; geração de 1 a 9. Respostas: 201 para cadastro, 204 para exclusão, 400 para dados inválidos, 404 para número inexistente, 409 para duplicidade.

## Testes

Com MongoDB local na porta 27017, execute `mvn verify`. Os testes de integração usam HTTP e banco real, em um banco temporário `pokedex_test_<identificador>`, removido ao final. Não alteram o banco principal. Cobrem importação, consulta, filtros, paginação, CRUD, persistência, validação e entrega da interface.

## Estrutura

- `src/main/java/br/edu/pokedex/`: modelo, inicialização, API e erros.
- `src/main/resources/data/pokemons.json`: coleção original enviada.
- `src/main/resources/static/`: interface HTML, CSS e JavaScript.
- `src/test/java/br/edu/pokedex/`: testes de integração.
- `compose.yaml`: MongoDB com volume persistente.

Reiniciar não sobrescreve a coleção nem repõe Pokémon excluídos. Para reimportar, faça backup e remova explicitamente a coleção `pokemons` antes de iniciar novamente.
