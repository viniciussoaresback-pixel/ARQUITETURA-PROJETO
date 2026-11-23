# Sistema de Transporte de Veículos — Padrões de Projeto em Java

Trabalho de **Arquitetura de Software e Padrões de Projetos** — Bimestre 2 — 2026
**Aluno:** Vinicius Soares Back — **RA:** 25.7571

Sistema simples de uma transportadora de veículos (caminhão cegonha e guincho), com interface gráfica em **Java Swing**, que aplica três padrões de projeto do catálogo GoF:

| Padrão | Família | Onde está | O que resolve |
|---|---|---|---|
| **Factory Method** | Criacional | `factory/Logistica.java` | Cria o transporte certo (Cegonha ou Guincho) sem `if` espalhado; tipo novo entra sem alterar o código existente |
| **Observer** | Comportamental | `observer/Pedido.java` | Quando o status do pedido muda, avisa Cliente, Painel, Financeiro e a própria tela automaticamente |
| **Singleton** | Criacional | `singleton/BancoDeDados.java` | Garante uma única conexão com o banco de dados em todo o sistema |

![Tela do sistema](telas/tela-principal.png)

## Requisitos

- **Java 11 ou superior** (JDK). Não usa nenhuma biblioteca externa.
- Os arquivos estão em **UTF-8**.

## Como executar

### Pelo VS Code (recomendado)
1. Instale a extensão **Extension Pack for Java**.
2. Abra a pasta `trabalho-padroes` (*File → Open Folder*).
3. Vá em **Run and Debug** e escolha **"Abrir a tela do sistema (Main)"**.

A pasta `.vscode` já deixa o projeto configurado em UTF-8, para os acentos não estragarem.

### Pelo terminal
Dentro da pasta `trabalho-padroes`:

```bash
javac -encoding UTF-8 -d out src/transporte/*.java src/transporte/*/*.java
java -Dstdout.encoding=UTF-8 -cp out transporte.Main
```

Para a demonstração só no console, sem tela:

```bash
java -Dstdout.encoding=UTF-8 -cp out transporte.DemoConsole
```

## Como usar a tela

1. **Novo pedido (Factory Method):** preencha cliente, placa, rota e quantidade de veículos, escolha **Cegonha** ou **Guincho** e clique em **Calcular frete** ou **Criar pedido**. O frete muda conforme o transporte.
2. **Pedidos (Observer):** selecione um pedido e clique em **Avançar status ▶** (Aguardando coleta → Em trânsito → Entregue). Os observadores marcados em *Avisar* são notificados; na entrega, o Financeiro gera a cobrança.
3. **Testar Singleton:** mostra que duas chamadas a `getInstancia()` devolvem o mesmo objeto.
4. **Ver histórico do banco:** lista tudo o que foi gravado no banco único.
5. **Registro de eventos:** mostra ao vivo o que cada classe está fazendo.

## Estrutura do projeto

```
trabalho-padroes/
├─ README.md                         este arquivo
├─ Trabalho-Padroes-de-Projeto.docx  documento do trabalho (propósito, motivação, UML e código)
├─ src/transporte/
│  ├─ Main.java                      abre a tela
│  ├─ DemoConsole.java               demonstração no console
│  ├─ singleton/
│  │  └─ BancoDeDados.java           Singleton
│  ├─ factory/
│  │  ├─ Transporte.java             Produto (interface)
│  │  ├─ Cegonha.java                Produto concreto
│  │  ├─ Guincho.java                Produto concreto
│  │  ├─ Logistica.java              Criador (abstrato) com o Factory Method criarTransporte()
│  │  ├─ LogisticaCegonha.java       Criador concreto
│  │  └─ LogisticaGuincho.java       Criador concreto
│  ├─ observer/
│  │  ├─ Pedido.java                 Sujeito
│  │  ├─ ObservadorPedido.java       Observador (interface)
│  │  ├─ NotificadorCliente.java     Observador concreto
│  │  ├─ PainelLogistica.java        Observador concreto
│  │  ├─ Financeiro.java             Observador concreto
│  │  └─ StatusPedido.java           enum dos status
│  └─ tela/
│     ├─ TelaPrincipal.java          janela (JFrame); também é um Observador
│     └─ SaidaParaTela.java          envia o System.out para o "Registro de eventos"
├─ uml/                              diagramas UML (PNG e fonte Mermaid)
├─ telas/                            prints da tela funcionando
└─ .vscode/                          configuração do VS Code (UTF-8 e execução)
```

## Diagramas UML

| Singleton | Factory Method | Observer |
|---|---|---|
| ![Singleton](uml/singleton.png) | ![Factory Method](uml/factory.png) | ![Observer](uml/observer.png) |

## Conceitos de orientação a objetos usados

- **Encapsulamento:** atributos privados; construtor privado no Singleton.
- **Abstração:** classe abstrata `Logistica` e as interfaces `Transporte` e `ObservadorPedido`.
- **Herança:** `LogisticaCegonha`/`LogisticaGuincho` herdam de `Logistica`; `TelaPrincipal` herda de `JFrame`.
- **Polimorfismo:** cada `Transporte` calcula o frete do seu jeito; cada observador reage do seu jeito em `atualizar()`.
- **Baixo acoplamento:** o `Pedido` não conhece as classes concretas que o observam.

## Referências

- [Refactoring.Guru — Catálogo de padrões de projeto](https://refactoring.guru/pt-br/design-patterns/catalog)
- [DevMedia — Padrões de projeto em Java](https://www.devmedia.com.br/guia/padroes-de-projeto-em-java/34456)
- GAMMA, E. et al. *Padrões de Projeto: soluções reutilizáveis de software orientado a objetos.* Bookman, 2000.
