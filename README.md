# Locadora de Veículos "Rota Segura" — TP2

Aplicação desktop em **Java (Swing)** com persistência em **banco relacional (SQLite)** usando o padrão **DAO**.
Continuação do TP1 (modelo orientado a objetos): agora com interface gráfica e dados gravados em banco.

Autor: Marcelo Antonio Rodrigues Junior — Técnicas de Programação.

## Requisitos de ambiente

| Item | Versão |
|------|--------|
| JDK  | 21 ou superior (testado com a 21) |
| Maven | 3.9 ou superior (para baixar o driver e rodar pelo terminal; o Eclipse já traz o m2e embutido) |
| Internet | apenas na primeira execução, para o Maven baixar o driver `sqlite-jdbc` |

Não é preciso instalar servidor de banco de dados: o SQLite grava tudo no arquivo `rotasegura.db`,
criado automaticamente (com as tabelas e alguns dados de teste) na pasta de onde a aplicação é executada.

## Como executar

### Opção 1 — Eclipse
1. `File > Import... > Maven > Existing Maven Projects` e selecione a pasta `RotaSegura` (a que contém o `pom.xml`).
2. Confirme que o projeto usa um JDK 21+ (`Project > Properties > Java Build Path > Libraries`).
3. Se aparecer erro de compilação logo após importar: botão direito no projeto > `Maven > Update Project...` (Alt+F5), marque *Force Update of Snapshots/Releases* e confirme.
4. Abra `src/main/java/br/com/rotasegura/aplicacao/Principal.java` > botão direito > `Run As > Java Application`.

### Opção 2 — Terminal com Maven (Windows, Linux ou macOS)
Dentro da pasta do projeto (onde está o `pom.xml`):

```
mvn compile exec:java
```

### Opção 3 — Gerar um JAR executável
```
mvn package
java -jar target/RotaSegura-1.0-SNAPSHOT.jar
```
O JAR já leva o driver do SQLite embutido. O banco `rotasegura.db` é criado na pasta em que o comando `java -jar` for executado.

### Outras IDEs (NetBeans, IntelliJ, VS Code)
Abra a pasta como projeto **Maven** e execute a classe `br.com.rotasegura.aplicacao.Principal`.

### Opção 4 — Sem Maven (compilação manual)
Baixe os arquivos `sqlite-jdbc-3.46.1.3.jar` e `slf4j-api` (Maven Central) para uma pasta `lib/` e, na raiz do projeto:

```
# Linux / macOS
mkdir -p out && javac -encoding UTF-8 -cp "lib/*" -d out $(find src/main/java -name "*.java")
cp -r src/main/resources/* out/
java -cp "out:lib/*" br.com.rotasegura.aplicacao.Principal

# Windows (cmd)
mkdir out
dir /s /b src\main\java\*.java > fontes.txt
javac -encoding UTF-8 -cp "lib/*" -d out @fontes.txt
xcopy /s /y src\main\resources out\
java -cp "out;lib/*" br.com.rotasegura.aplicacao.Principal
```

## Uso rápido
- **Clientes / Veículos**: busque pelo campo de pesquisa, clique numa linha da tabela para editar, use *Novo*, *Salvar* e *Excluir*.
  O CPF (cliente) e a placa/categoria (veículo) não podem ser alterados depois de cadastrados.
- **Locações**: escolha cliente e veículo disponível, informe as datas (`dd/MM/yyyy`) e clique em *Abrir locação*.
  Selecione uma locação e use *Registrar devolução...* para abrir a tela de fechamento: informe a data efetiva,
  confira o resumo calculado (diária e seguro de cada categoria) e confirme.
  O relatório de encerramento é gravado em `relatorios/contrato_N_encerramento.txt`
  (o comprovante de abertura continua sendo gravado em `relatorios/contrato_N.txt`).
- Dados inválidos (CPF, placa, datas, números etc.) geram uma caixa de diálogo com a mensagem de erro; o sistema não fecha.

## Regras de fechamento do contrato
- Dias cobrados = dias entre o início e a devolução efetiva (mínimo de 1 diária).
- Valor final = `diária da categoria × dias + seguro da categoria × dias` (polimorfismo de `calcularValorDiaria()` e `calcularSeguro()`):

| Categoria | Diária | Seguro por dia |
|-----------|--------|----------------|
| Popular | base (+10% com ar-condicionado) | R$ 25 |
| Sedan | base + 20% | R$ 45 |
| SUV | base + 25% (+40% com tração 4x4) | R$ 75 |

## Arquitetura (camadas)

```
src/main/java/br/com/rotasegura
├── aplicacao   Principal (inicia o banco e abre a janela)
├── visao       Telas Swing (JanelaPrincipal, PainelClientes, PainelVeiculos, PainelLocacoes, DialogoDevolucao)
├── servico     LocacaoService (regras de negócio e controle das transações)
├── modelo      Cliente, Veiculo (abstrata) + Popular/Sedan/SUV, Contrato, Imprimivel
├── dao         DAO<T,K>, ClienteDAO, VeiculoDAO, ContratoDAO, ConexaoBD (todo o SQL fica aqui)
└── excecao     DadosInvalidosException, VeiculoIndisponivelException, PersistenciaException
src/main/resources/db/rota_segura.sql   criação das tabelas + carga inicial
```

- A **visão** só fala com o `LocacaoService` e com o modelo; não existe SQL nela.
- O **serviço** concentra as regras e as transações: abrir ou encerrar uma locação grava o contrato **e** altera o status
  do veículo (disponível/locado) na mesma transação (`commit`; em caso de erro, `rollback`).
- O **DAO** usa `PreparedStatement` e `SELECT` com `JOIN` para reconstruir `Contrato` com `Cliente` e a subclasse correta de `Veiculo`.

## Banco de dados
Três tabelas: `cliente`, `veiculo` e `contrato` (com chaves estrangeiras ativas).
A hierarquia de veículos fica em uma única tabela, com a coluna `tipo` (`POPULAR`, `SEDAN`, `SUV`) e as colunas específicas de cada categoria
(`possui_ar_condicionado`, `capacidade_porta_malas`, `tracao_4x4`). Clientes e veículos que já têm locações não podem ser excluídos.

O script `src/main/resources/db/rota_segura.sql` é executado automaticamente na primeira inicialização.
Para recomeçar com o banco limpo, feche a aplicação e apague o arquivo `rotasegura.db`.
Para usar outro arquivo de banco: `java -Drotasegura.db=outro.db -jar ...`.
