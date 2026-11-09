# CLAUDE.md — Sistema de Gestão Contábil para Igrejas

## Sobre o projeto

Software desktop em Java para o **Escritório Contábil EKE**, que atende **igrejas** (cerca de 6 hoje; terceiro setor e empresas, talvez, no futuro). Projeto solo do Marcos (Vini), estudante de Engenharia de Software. A **usuária final e cliente é a avó dele, contadora e dona do escritório**.

O sistema tem dois módulos:

- **Módulo A — Controle de Vencimentos:** CNDs (Federal CNPJ e CPF do representante, Estadual SEFAZ-AM, Municipal, CNDT, CRF/FGTS, Falência TJAM, Viabilidade), mandato da diretoria e obrigações periódicas (EFD-Reinf). Painel de alertas e atalho para emissão. Resolve prazos que já foram perdidos.
- **Módulo B — Contabilidade:** digitação do livro caixa (que chega como **foto de caderno** ou, raramente, `.xlsx`), pendências, sugestão de conta, e **geração automática** do fluxo de caixa e do balancete mensais, hoje montados à mão em Excel.

O sistema **não transmite** dados a órgãos públicos e **não emite certidões sozinho**: organiza, alerta e facilita. ECD/ECF, OCR do livro caixa, constituição de igrejas novas, folha/eSocial e XML de nota fiscal estão **fora do MVP**.

Plano completo (fora do repositório, porque contém dados reais das igrejas): `C:\Users\marco\Desktop\Marcos Vinicius\Estudos\Sistema-Contabil\plano-sistema-contabil-v3.md` (requisitos RF/RNF, User Stories, UML, cronograma).

> **O repositório é público.** Nada de nomes de igrejas, valores, CNPJs, CPFs ou caminhos do Drive em código, testes, commits ou documentos versionados.

## Como trabalhar com o Marcos (importante)

O objetivo do projeto **não é só entregar o software, é o Marcos aprender** a desenvolver sozinho com um processo real. Por isso:

- **Explique antes de escrever.** Diga o que vai fazer e por quê, e só então altere arquivos.
- **Passos pequenos.** Uma tarefa por vez (uma classe, um método, uma tela). Não gere o sistema inteiro de uma vez.
- **Explique o código em profundidade** quando ele pedir: linha por linha, incluindo o porquê de cada decisão de design.
- **Quando houver mais de um caminho razoável, apresente as opções** e deixe o Marcos escolher. Decisões de arquitetura e de requisitos são dele.
- **Corrija com honestidade.** Se ele propuser algo com problema (técnico ou de requisito), diga e explique.
- Em testes, **dê dicas em vez da resposta pronta** quando ele estiver praticando.
- Ele é intermediário em Java (OOP, CRUD com SQL e ArrayList). Já viu: encapsulamento, `static`, `try-with-resources`, `PreparedStatement` vs `Statement`, JDBC básico, primeiros testes JUnit 5 com `@TempDir`. Conceitos novos (JavaFX, FXML, Mockito, migrações, `Clock` injetável) merecem explicação.
- Responda em **português do Brasil**.

## Stack

- Java 21, Maven (Wrapper: `./mvnw`), JavaFX (FXML), SQLite via JDBC (`org.xerial:sqlite-jdbc`), JUnit 5
- Previstos: Apache POI (Excel), biblioteca de PDF a escolher (OpenPDF / PDFBox / JasperReports)
- IDE do Marcos: Eclipse (ele roda e depura por lá). Você trabalha pelo terminal; depois de alterar arquivos, ele atualiza no Eclipse.
- Git + GitHub. Board Scrumban no GitHub Projects.

## Arquitetura: MVC + Service + Repository

Pacote base: `com.marcos.sistemacontabil`

| Pacote | Responsabilidade |
|---|---|
| `model` | Entidades puras (sem SQL, sem JavaFX) |
| `service` | Regras de negócio (partida dobrada, situação de vencimento, fechamento de mês). Sem JavaFX, sem SQL |
| `repository` | Acesso a dados. Repositories por **interface**, com implementação SQLite. Contém `DatabaseConfig` |
| `ui` | `Main`, controllers JavaFX e `.fxml` |

Fluxo: `Controller → Service → Repository (interface) → implementação SQLite`.

Regras:
- Controller nunca acessa o banco diretamente.
- Regra de negócio fica no Service, testável sem UI e sem banco.
- Um único módulo Maven. Subpacotes por módulo funcional (`model.vencimento`, `model.contabil`) quando as classes crescerem.

Modelo principal: `Entidade`, `Dirigente`, `TipoItemVencimento`, `ItemVencimento` (Módulo A); `Conta`, `Lancamento`, `ItemLancamento` (Módulo B). Detalhes no diagrama de classes do plano v3.

## Decisões técnicas já tomadas

1. **Dinheiro nunca em `double`.** `BigDecimal` no Java e **centavos inteiros** (`INTEGER`) no SQLite.
2. **`PreparedStatement` sempre** que houver dado do usuário. `Statement` só para SQL fixo (ex.: `CREATE TABLE`).
3. **`try-with-resources`** para `Connection`, `Statement`, `ResultSet`.
4. **O arquivo `.db` não vai para o Git.** O esquema vive no código.
5. **Versionamento do esquema** com tabela `schema_version` e migrações em ordem.
6. **Lançamentos não são apagados**: estorno ou cancelamento, com datas de criação e de alteração. Mês fechado não pode ser editado.
7. **Nome do cadastro principal: `Entidade`** (decidido em 06/11/2026). A tabela e a classe ainda se chamam `empresa`/`Empresa`; renomear é a primeira tarefa da Sprint 2.
8. **Lançamento único, relatórios derivados.** Fluxo de caixa e balancete são consultas sobre os lançamentos, nunca dados digitados.
9. **Partida dobrada escondida da usuária.** A tela usa entrada/saída; o `LancamentoService` gera débito/crédito (entrada: débito Caixa / crédito receita; saída: débito despesa ou ativo / crédito Caixa).
10. **Item de vencimento genérico** (`ItemVencimento` + `TipoItemVencimento`): uma certidão nova é cadastro, não código novo. Prazos de validade e links de emissão são **dados configuráveis**.
11. **Datas com `LocalDate` e `Clock` injetável** nos Services de vencimento, para testar sem depender da data atual.
12. **PDFs anexados ficam em pasta** (`dados/anexos/<cnpj>/`); o banco guarda só o caminho relativo.
13. **LGPD:** dados de dirigentes (nome, CPF) são pessoais; guardar o mínimo. **Não guardar nome de dizimista/doador** no MVP.
14. O plano de contas modelo vem do **balancete atual da usuária** (códigos como `1.1.1.01 Caixa Geral`, `4.1.1.01 Dízimos`); cada conta de resultado sabe a sua **linha do fluxo de caixa**. Campo opcional `codigoReferencial` para uma futura ECD/ECF.

## Processo (Scrumban solo)

- Sprints de aproximadamente 1 semana (20h), com faixa de duração.
- Fluxo do board: Backlog → A Fazer (Sprint) → Em Andamento (máx. 2) → Em Revisão/Teste → Concluído.
- Uma **Issue por tarefa**, criada antes de começar. O commit da entrega termina com `Closes #N`.
- **Definition of Done:** compila, tem teste unitário relevante, funcionalidade verificada na UI, commit descritivo.
- Mensagens no formato `Sprint N: descrição curta. Closes #N`.
- Demo de 10 minutos para a usuária a cada sprint.

## Estado atual (06/11/2026)

- **Sprint 1 concluída:** projeto Maven, pacotes por camada, `DatabaseConfig` (tabela `empresa`), `Main` mínima, repositório e board.
- **Feito depois:** Maven Wrapper + plugin JavaFX (`91c3639`); testes JUnit do `DatabaseConfig` (`bbd3c83`).
- **Sprint 0:** entrevista feita e plano v3 escrito. **Falta** validar as User Stories lendo-as com a usuária.
- **Próxima (Sprint 2):** renomear para `Entidade`, criar `schema_version`, cadastro de Entidade ponta a ponta.
- Depois: Sprints 3–4 fecham o Módulo A (Marco 1: instalar v0.1 para ela usar); Sprints 5–10 fazem o Módulo B.

## Dados de teste

- As planilhas reais ficam no Google Drive da usuária (o caminho está no plano v3, fora do repo): uma de fluxo de caixa (uma aba por igreja) e uma de balancete mensal (uma aba por mês).
- Os relatórios gerados devem **bater centavo a centavo** com essas planilhas nos meses já fechados (teste de aceitação).
- **Nunca copiar dados reais das igrejas para o repositório.** Testes automatizados usam dados fictícios.

## Pontos a verificar no código atual

- O SLF4J mostra um aviso no console ("Failed to load class StaticLoggerBinder"). É inofensivo; `org.slf4j:slf4j-nop` resolve, se ele quiser.

## O que NÃO fazer

- Não use `double` para valores monetários.
- Não coloque SQL nem regra de negócio em controllers.
- Não faça commit de `target/`, `.settings/`, `.classpath`, `.project`, `*.db` ou de dados reais das igrejas.
- Não implemente itens fora do MVP (ECD/ECF, OCR, XML de nota, folha) sem confirmar com o Marcos.
- Não automatize o acesso aos sites de emissão de certidões (captcha, termos de uso). O sistema só abre o link e copia o CNPJ/CPF.
- Não faça refatorações grandes sem explicar o motivo e pedir confirmação.
- Não faça commits: o Marcos commita.

## Comandos úteis

```
./mvnw clean compile     # compilar
./mvnw clean test        # rodar testes
git status               # ver o que mudou
```
