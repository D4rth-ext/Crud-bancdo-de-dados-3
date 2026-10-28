# Pokédex — Como executar no Windows

Uma Pokédex feita em Java e MongoDB para consultar, cadastrar, editar e excluir Pokémon. Já inclui os **800 Pokémon do arquivo JSON**.

## 1. Instale as ferramentas

- **Java JDK 21:** https://adoptium.net/temurin/releases/?version=21
- **Maven 3.9 ou superior:** https://maven.apache.org/install.html
- **Docker Desktop:** https://docs.docker.com/desktop/setup/install/windows-install/

Na instalação do Java, habilite as opções de adicionar ao `PATH` e configurar `JAVA_HOME`, se disponíveis. Para o Maven, extraia o arquivo ZIP e adicione a pasta `bin` dele ao `Path` nas Variáveis de Ambiente do Windows.

Depois, feche e abra o PowerShell e confira:

```powershell
java -version
javac -version
mvn -version
docker --version
```

O Java e o compilador `javac` devem mostrar a versão **21**, e os outros comandos devem mostrar suas versões. Se algum comando não for reconhecido, confira a instalação antes de continuar.

### Se o Docker pedir “Virtual Machine Platform” ou WSL

Abra o **PowerShell como administrador** e execute:

```powershell
wsl --install
```

Aguarde concluir, reinicie o computador e instale o Docker Desktop novamente. Se aparecer outro erro, copie a mensagem para pedir ajuda.

## 2. Abra a pasta do projeto

Extraia o ZIP baixado do GitHub. Abra a pasta que contém o arquivo `pom.xml`.

Na barra de endereço do Explorador de Arquivos, digite `powershell` e pressione **Enter**.

## 3. Inicie a Pokédex

Abra o **Docker Desktop** e aguarde ele iniciar.

No PowerShell da pasta do projeto, execute **um comando por vez**:

```powershell
docker compose up -d --wait
```

Esse comando inicia o MongoDB. Depois, compile o projeto e execute os testes:

```powershell
mvn clean verify
```

Aguarde aparecer **BUILD SUCCESS**. Então inicie a aplicação:

```powershell
java -jar target/pokedex-1.0.0.jar
```

Mantenha esse terminal aberto. No navegador, digite **localhost:8080** na barra de endereço.

Na primeira execução, os 800 Pokémon são importados automaticamente para o MongoDB. Não precisa importar o JSON manualmente.

## 4. Teste a aplicação

1. Busque **Bulbasaur** e abra sua ficha.
2. Experimente os filtros de tipo, geração e lendário.
3. Clique em **Novo Pokémon** e cadastre um Pokémon com o número **801**.
4. Abra a ficha dele, clique em **Editar Pokémon** e salve uma alteração.
5. Clique em **Excluir** e confirme para removê-lo.

As alterações ficam salvas no banco mesmo depois de fechar a aplicação.

## 5. Como fechar e abrir novamente

Para fechar a aplicação, pressione **Ctrl+C** no terminal. Depois, pare o banco:

```powershell
docker compose down
```

Para abrir novamente, inicie o Docker Desktop e execute na pasta do projeto:

```powershell
docker compose up -d --wait
java -jar target/pokedex-1.0.0.jar
```

Só precisa executar `mvn clean verify` novamente se alterar o código ou quiser repetir os testes.

## Problemas comuns

- **“docker não é reconhecido”:** instale o Docker Desktop e abra um novo PowerShell.
- **“mvn não é reconhecido”:** confira se a pasta `bin` do Maven está no `Path` e abra um novo PowerShell.
- **“release version 21 not supported”:** confira se `mvn -version` mostra Java 21 e ajuste `JAVA_HOME` para o JDK 21.
- **Erro de conexão com o MongoDB:** confira se o Docker Desktop está aberto e repita `docker compose up -d --wait`.
- **Porta 8080 ocupada:** feche outra instância da aplicação antes de iniciar novamente.
