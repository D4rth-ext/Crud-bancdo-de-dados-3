# Pokédex no terminal — Java e MongoDB

Consulte, cadastre, edite e exclua Pokémon pelo terminal. O projeto inclui os **800 Pokémon do JSON fornecido**. Não usa navegador nem precisa de Docker.

## 1. O que precisa instalar

- **Java JDK 21**: https://adoptium.net/temurin/releases/?version=21
- **Maven 3.9 ou superior**: https://maven.apache.org/install.html
- **MongoDB Community Server**: https://www.mongodb.com/try/download/community

Na instalação do MongoDB, marque a opção de instalar como **serviço do Windows**. O Compass é opcional: ele serve para visualizar o banco, mas não substitui o servidor.

No PowerShell, confira se as ferramentas estão instaladas:

```powershell
java -version
javac -version
mvn -version
```

Java, javac e o Java mostrado pelo Maven devem estar na versão 21. Se um comando não for reconhecido, confira sua instalação e o `Path` do Windows. A variável `JAVA_HOME` deve apontar para a pasta do JDK 21.

## 2. Inicie o MongoDB

Pressione **Win + R**, digite `services.msc` e pressione Enter. Procure **MongoDB Server**. Se estiver parado, clique com o botão direito e escolha **Iniciar**.

O projeto conecta por padrão ao MongoDB local na porta **27017**, banco **pokedex**, coleção **pokemons**. Se o banco já está funcionando nessa porta, pode seguir para o próximo passo.

## 3. Execute o projeto

Extraia o ZIP do projeto. Abra a pasta que contém o arquivo **pom.xml**. Na barra de endereço do Explorador de Arquivos, digite `powershell` e pressione Enter.

Execute um comando por vez:

```powershell
mvn clean verify
```

Quando aparecer **BUILD SUCCESS**, execute:

```powershell
java -jar target/pokedex-1.0.0.jar
```

O menu aparece no próprio terminal:

```text
1 - Listar Pokémon
2 - Buscar por nome
3 - Consultar ficha por número
4 - Cadastrar Pokémon
5 - Editar Pokémon
6 - Excluir Pokémon
0 - Sair
```

Digite o número da opção e pressione Enter. Responda às perguntas que aparecerem.

## 4. Como testar

1. Digite **2** e busque **Bulbasaur**.
2. Digite **3** e informe o número **1** para consultar sua ficha.
3. Digite **4** e cadastre um Pokémon com um número ainda livre, como **801**. Tipos devem ser informados em inglês, por exemplo `Grass,Poison`.
4. Digite **5** para editar esse Pokémon. Pressione Enter em um campo para manter seu valor.
5. Digite **6** para excluí-lo e confirme com **s**.
6. Digite **0** para sair.

A opção 1 lista 20 Pokémon por página. A opção 2 aceita parte do nome, sem diferenciar maiúsculas e minúsculas. A ficha mostra tipos, geração, lendário, HP, ataque, defesa e velocidade.

Os atributos vão de 1 a 255 e a geração de 1 a 9. O número precisa ser positivo e único. Ele corresponde ao `_id` do JSON, que inclui formas Mega e pode diferir da numeração oficial da Pokédex.

## Abrir novamente

Com o MongoDB rodando, basta executar na pasta do projeto:

```powershell
java -jar target/pokedex-1.0.0.jar
```

Os dados ficam salvos. Os 800 registros são importados apenas se a coleção ainda não existir, sem sobrescrever suas alterações.

## Problemas comuns

- **mvn não reconhecido:** adicione a pasta `bin` do Maven ao `Path` e abra outro PowerShell.
- **Erro sobre Java 21:** confira `mvn -version` e ajuste `JAVA_HOME` para o JDK 21.
- **Erro de conexão com o MongoDB:** confira se o serviço MongoDB Server está iniciado na porta 27017.
- **Caracteres estranhos no terminal:** execute `chcp 65001` antes de iniciar e use um terminal com suporte a UTF-8.

Para outro servidor MongoDB, configure `MONGODB_URI` no ambiente antes de executar. Não salve senhas no Git.

## Testes automáticos

`mvn clean verify` compila o projeto e executa seis testes de integração contra MongoDB local. Eles verificam importação, consultas, CRUD, validação e interação com o menu. Usam um banco temporário `pokedex_test_<identificador>`, removido ao final, sem alterar o banco principal.
