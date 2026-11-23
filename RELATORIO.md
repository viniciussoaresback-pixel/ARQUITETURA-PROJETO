---
title: "AplicaÃ§Ã£o de PadrÃµes de Projeto em um Software Real"
subtitle: "Arquitetura de Software e PadrÃµes de Projetos â€” Bimestre 2 â€” 2026"
author: "Aluno: VINICIUS SOARES BACK  â€”  RA: 25.7571"
toc-title: "SumÃ¡rio"
---

# IntroduÃ§Ã£o

**Projeto:** Sistema de Transporte de VeÃ­culos (transportadora com caminhÃµes cegonha).

**Problema real:** uma transportadora recebe pedidos de clientes para levar carros de uma cidade a outra. O sistema precisa (1) gravar tudo num Ãºnico banco de dados, (2) calcular o frete conforme o tipo de caminhÃ£o usado e (3) avisar vÃ¡rias Ã¡reas (cliente, logÃ­stica, financeiro) sempre que o pedido muda de situaÃ§Ã£o.

Foram escolhidos **3 padrÃµes de projeto** do catÃ¡logo GoF (Gamma et al.): dois criacionais e um comportamental:

| PadrÃ£o | FamÃ­lia | Onde foi usado no sistema |
|---|---|---|
| Singleton | Criacional | ConexÃ£o Ãºnica com o banco de dados (`BancoDeDados`) |
| Factory Method | Criacional | CriaÃ§Ã£o do tipo de transporte: cegonha ou guincho (`Logistica`) |
| Observer | Comportamental | Avisos automÃ¡ticos quando o status do pedido muda (`Pedido`) |

Os trÃªs padrÃµes trabalham **juntos** na mesma aplicaÃ§Ã£o: o frete calculado pelo Factory Method vai para o `Pedido`; o `Pedido` (Observer) grava cada mudanÃ§a no `BancoDeDados` (Singleton).

Estrutura do cÃ³digo-fonte (Java 11 ou superior, sem bibliotecas externas):

```
src/transporte/
 â”œâ”€ Main.java                      (demonstraÃ§Ã£o)
 â”œâ”€ singleton/BancoDeDados.java
 â”œâ”€ factory/Transporte.java, Cegonha.java, Guincho.java,
 â”‚          Logistica.java, LogisticaCegonha.java, LogisticaGuincho.java
 â””â”€ observer/Pedido.java, StatusPedido.java, ObservadorPedido.java,
            NotificadorCliente.java, PainelLogistica.java, Financeiro.java
```

# PadrÃ£o 1 â€” Singleton

## Nome / PropÃ³sito

**Singleton** (criacional). Garante que uma classe tenha **apenas uma instÃ¢ncia** e oferece um **ponto de acesso global** a ela.

## MotivaÃ§Ã£o â€” Problema / SoluÃ§Ã£o real

**Problema:** vÃ¡rias partes do sistema (pedidos, relatÃ³rios, financeiro) precisam acessar o banco de dados. Se cada uma fizesse `new Conexao()`, o sistema abriria dezenas de conexÃµes, gastando memÃ³ria e podendo estourar o limite de conexÃµes do servidor de banco. AlÃ©m disso, os dados ficariam espalhados em objetos diferentes.

**SoluÃ§Ã£o:** a classe `BancoDeDados` tem o **construtor privado** (ninguÃ©m consegue usar `new`) e um mÃ©todo estÃ¡tico `getInstancia()` que cria o objeto sÃ³ na primeira chamada e devolve sempre o mesmo objeto nas prÃ³ximas. O mÃ©todo usa *double-checked locking* com `volatile` para ser seguro mesmo com vÃ¡rias threads.

## Estrutura â€” Diagrama UML

![Diagrama UML â€” Singleton](uml/singleton.png){width=45%}

**Participantes:** `BancoDeDados` (o Singleton) e os clientes `Main` e `Pedido`, que sÃ³ obtÃªm o banco por `getInstancia()`.

## CÃ³digo


```java
package transporte.singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PADRÃƒO SINGLETON
 * Garante que exista UMA Ãºnica conexÃ£o com o banco de dados em todo o sistema.
 * (Aqui o "banco" Ã© simulado em memÃ³ria para o exemplo rodar sem instalar nada.)
 */
public final class BancoDeDados {

    // 1) A Ãºnica instÃ¢ncia fica guardada num atributo estÃ¡tico e privado.
    //    "volatile" garante que todas as threads enxerguem o valor atualizado.
    private static volatile BancoDeDados instancia;

    private final List<String> registros = new ArrayList<>();
    private int consultasRealizadas = 0;

    // 2) Construtor PRIVADO: ninguÃ©m fora da classe consegue fazer "new BancoDeDados()".
    private BancoDeDados() {
        System.out.println("[BancoDeDados] Abrindo conexÃ£o com o banco... (sÃ³ deve aparecer 1 vez)");
    }

    // 3) Ponto de acesso global. Cria a instÃ¢ncia sÃ³ na primeira chamada (lazy)
    //    e usa "double-checked locking" para ser seguro com vÃ¡rias threads.
    public static BancoDeDados getInstancia() {
        if (instancia == null) {
            synchronized (BancoDeDados.class) {
                if (instancia == null) {
                    instancia = new BancoDeDados();
                }
            }
        }
        return instancia;
    }

    public synchronized void salvar(String registro) {
        consultasRealizadas++;
        registros.add(registro);
        System.out.println("[BancoDeDados] Salvo: " + registro);
    }

    public synchronized List<String> listar() {
        consultasRealizadas++;
        return Collections.unmodifiableList(new ArrayList<>(registros));
    }

    public int getConsultasRealizadas() {
        return consultasRealizadas;
    }
}
```

# PadrÃ£o 2 â€” Factory Method

## Nome / PropÃ³sito

**Factory Method** (criacional). Define um mÃ©todo para **criar objetos**, mas deixa as **subclasses decidirem qual classe concreta** serÃ¡ instanciada.

## MotivaÃ§Ã£o â€” Problema / SoluÃ§Ã£o real

**Problema:** a transportadora comeÃ§ou sÃ³ com caminhÃµes **cegonha** (levam atÃ© 11 carros e dividem o custo da viagem). Depois passou a usar **guincho plataforma** para entregas urgentes (1 carro por viagem, com taxa de saÃ­da). Se a regra "planejar viagem" tivesse `if (tipo == "cegonha") new Cegonha() else new Guincho()`, cada transporte novo (ex.: carreta fechada) obrigaria a mexer e arriscar quebrar esse cÃ³digo.

**SoluÃ§Ã£o:** a classe abstrata `Logistica` contÃ©m a regra comum `planejarViagem(...)` e chama o mÃ©todo abstrato `criarTransporte()` â€” o **Factory Method**. As subclasses `LogisticaCegonha` e `LogisticaGuincho` decidem qual `Transporte` criar. Para incluir um tipo novo basta criar duas classes novas, **sem alterar as existentes** (princÃ­pio Aberto/Fechado).

## Estrutura â€” Diagrama UML

![Diagrama UML â€” Factory Method](uml/factory.png){width=95%}

**Participantes:**

- *Produto:* interface `Transporte`;
- *Produtos concretos:* `Cegonha`, `Guincho`;
- *Criador:* `Logistica` (abstrata, com o Factory Method `criarTransporte()`);
- *Criadores concretos:* `LogisticaCegonha`, `LogisticaGuincho`.

## CÃ³digo


```java
package transporte.factory;

/** Produto (interface comum) do Factory Method. */
public interface Transporte {
    String getTipo();
    int getCapacidadeVeiculos();
    double calcularFrete(double distanciaKm, int quantidadeVeiculos);
}
```

```java
package transporte.factory;

/** Produto concreto: caminhÃ£o cegonha, leva atÃ© 11 carros e divide o custo entre eles. */
public class Cegonha implements Transporte {
    private static final double PRECO_KM = 9.50;

    @Override public String getTipo() { return "Cegonha"; }
    @Override public int getCapacidadeVeiculos() { return 11; }

    @Override
    public double calcularFrete(double distanciaKm, int quantidadeVeiculos) {
        // O custo da viagem Ã© rateado pela capacidade: cada carro paga a sua "vaga".
        return (distanciaKm * PRECO_KM / getCapacidadeVeiculos()) * quantidadeVeiculos;
    }
}
```

```java
package transporte.factory;

/** Produto concreto: guincho plataforma, leva 1 carro (entregas urgentes ou curtas). */
public class Guincho implements Transporte {
    private static final double PRECO_KM = 4.20;
    private static final double TAXA_SAIDA = 150.00;

    @Override public String getTipo() { return "Guincho plataforma"; }
    @Override public int getCapacidadeVeiculos() { return 1; }

    @Override
    public double calcularFrete(double distanciaKm, int quantidadeVeiculos) {
        // Uma viagem por carro: cada veÃ­culo paga a viagem inteira.
        return (TAXA_SAIDA + distanciaKm * PRECO_KM) * quantidadeVeiculos;
    }
}
```

```java
package transporte.factory;

/**
 * PADRÃƒO FACTORY METHOD â€” Criador (Creator).
 * ContÃ©m a regra de negÃ³cio comum (planejar a viagem), mas deixa para as
 * subclasses decidirem QUAL transporte serÃ¡ criado.
 */
public abstract class Logistica {

    // O "Factory Method": cada subclasse devolve o seu tipo de transporte.
    protected abstract Transporte criarTransporte();

    // Regra de negÃ³cio que NÃƒO depende da classe concreta do transporte.
    public double planejarViagem(String rota, double distanciaKm, int quantidadeVeiculos) {
        Transporte transporte = criarTransporte();
        if (quantidadeVeiculos > transporte.getCapacidadeVeiculos()
                && transporte.getCapacidadeVeiculos() > 1) {
            throw new IllegalArgumentException("Capacidade excedida para " + transporte.getTipo());
        }
        double frete = transporte.calcularFrete(distanciaKm, quantidadeVeiculos);
        System.out.printf("[LogÃ­stica] %s | %s | %.0f km | %d veÃ­culo(s) | frete R$ %.2f%n",
                rota, transporte.getTipo(), distanciaKm, quantidadeVeiculos, frete);
        return frete;
    }
}
```

```java
package transporte.factory;

/** Criador concreto: decide que o transporte serÃ¡ uma Cegonha. */
public class LogisticaCegonha extends Logistica {
    @Override
    protected Transporte criarTransporte() {
        return new Cegonha();
    }
}
```

```java
package transporte.factory;

/** Criador concreto: decide que o transporte serÃ¡ um Guincho. */
public class LogisticaGuincho extends Logistica {
    @Override
    protected Transporte criarTransporte() {
        return new Guincho();
    }
}
```

# PadrÃ£o 3 â€” Observer

## Nome / PropÃ³sito

**Observer** (comportamental). Define uma dependÃªncia **um-para-muitos**: quando um objeto (sujeito) muda de estado, **todos os seus dependentes (observadores) sÃ£o avisados automaticamente**.

## MotivaÃ§Ã£o â€” Problema / SoluÃ§Ã£o real

**Problema:** quando um pedido muda de status (aguardando coleta â†’ em trÃ¢nsito â†’ entregue), vÃ¡rias Ã¡reas precisam reagir: o **cliente** quer receber mensagem, o **painel da logÃ­stica** precisa mover o pedido de coluna e o **financeiro** precisa gerar a cobranÃ§a na entrega. Se a classe `Pedido` chamasse cada uma diretamente, ela ficaria acoplada a todas, e cada Ã¡rea nova exigiria mudar o `Pedido`.

**SoluÃ§Ã£o:** o `Pedido` guarda uma lista de `ObservadorPedido` (uma interface). Ao mudar o status, ele percorre a lista e chama `atualizar(this)` em cada um, sem saber quem sÃ£o. Para incluir uma Ã¡rea nova (ex.: aviso ao motorista), basta criar uma classe que implementa a interface e cadastrÃ¡-la com `adicionarObservador(...)`.

## Estrutura â€” Diagrama UML

![Diagrama UML â€” Observer](uml/observer.png){width=90%}

**Participantes:**

- *Sujeito:* `Pedido`;
- *Observador:* interface `ObservadorPedido`;
- *Observadores concretos:* `NotificadorCliente`, `PainelLogistica`, `Financeiro`.

## CÃ³digo


```java
package transporte.observer;

public enum StatusPedido {
    AGUARDANDO_COLETA, EM_TRANSITO, ENTREGUE
}
```

```java
package transporte.observer;

/** PADRÃƒO OBSERVER â€” interface que todo "interessado" no pedido implementa. */
public interface ObservadorPedido {
    void atualizar(Pedido pedido);
}
```

```java
package transporte.observer;

import java.util.ArrayList;
import java.util.List;
import transporte.singleton.BancoDeDados;

/**
 * PADRÃƒO OBSERVER â€” Sujeito (Subject / Publisher).
 * Quando o status do pedido muda, ele avisa automaticamente todos os observadores
 * cadastrados, sem precisar conhecer a classe concreta de cada um.
 */
public class Pedido {
    private final int numero;
    private final String cliente;
    private final String placa;
    private final double valorFrete;
    private StatusPedido status = StatusPedido.AGUARDANDO_COLETA;

    private final List<ObservadorPedido> observadores = new ArrayList<>();

    public Pedido(int numero, String cliente, String placa, double valorFrete) {
        this.numero = numero;
        this.cliente = cliente;
        this.placa = placa;
        this.valorFrete = valorFrete;
    }

    public void adicionarObservador(ObservadorPedido o) { observadores.add(o); }
    public void removerObservador(ObservadorPedido o) { observadores.remove(o); }

    private void notificarObservadores() {
        for (ObservadorPedido o : observadores) {
            o.atualizar(this);
        }
    }

    public void alterarStatus(StatusPedido novoStatus) {
        this.status = novoStatus;
        System.out.println("\n>> Pedido #" + numero + " mudou para " + novoStatus);
        // Usa o Singleton: todo o sistema grava no MESMO banco.
        BancoDeDados.getInstancia().salvar("Pedido #" + numero + " -> " + novoStatus);
        notificarObservadores();
    }

    public int getNumero() { return numero; }
    public String getCliente() { return cliente; }
    public String getPlaca() { return placa; }
    public double getValorFrete() { return valorFrete; }
    public StatusPedido getStatus() { return status; }
}
```

```java
package transporte.observer;

/** Observador concreto: avisa o cliente a cada mudanÃ§a. */
public class NotificadorCliente implements ObservadorPedido {
    @Override
    public void atualizar(Pedido p) {
        System.out.printf("   [Cliente] Mensagem para %s: seu veÃ­culo %s estÃ¡ %s.%n",
                p.getCliente(), p.getPlaca(), p.getStatus());
    }
}
```

```java
package transporte.observer;

/** Observador concreto: atualiza o painel da equipe de logÃ­stica. */
public class PainelLogistica implements ObservadorPedido {
    @Override
    public void atualizar(Pedido p) {
        System.out.printf("   [Painel] Pedido #%d movido para a coluna %s.%n",
                p.getNumero(), p.getStatus());
    }
}
```

```java
package transporte.observer;

/** Observador concreto: sÃ³ age quando o pedido Ã© ENTREGUE (gera a cobranÃ§a). */
public class Financeiro implements ObservadorPedido {
    @Override
    public void atualizar(Pedido p) {
        if (p.getStatus() == StatusPedido.ENTREGUE) {
            System.out.printf("   [Financeiro] CobranÃ§a gerada: %s, R$ %.2f.%n",
                    p.getCliente(), p.getValorFrete());
        }
    }
}
```

# DemonstraÃ§Ã£o â€” Classe principal


```java
package transporte;

import transporte.factory.Logistica;
import transporte.factory.LogisticaCegonha;
import transporte.factory.LogisticaGuincho;
import transporte.observer.Financeiro;
import transporte.observer.NotificadorCliente;
import transporte.observer.PainelLogistica;
import transporte.observer.Pedido;
import transporte.observer.StatusPedido;
import transporte.singleton.BancoDeDados;

/** Sistema de Transporte de VeÃ­culos â€” demonstraÃ§Ã£o dos 3 padrÃµes juntos. */
public class Main {
    public static void main(String[] args) {
        System.out.println("===== 1) SINGLETON =====");
        BancoDeDados banco1 = BancoDeDados.getInstancia();
        BancoDeDados banco2 = BancoDeDados.getInstancia();
        System.out.println("banco1 e banco2 sÃ£o o MESMO objeto? " + (banco1 == banco2));

        System.out.println("\n===== 2) FACTORY METHOD =====");
        // O cÃ³digo cliente sÃ³ conhece a classe abstrata Logistica.
        Logistica viaCegonha = new LogisticaCegonha();
        Logistica viaGuincho = new LogisticaGuincho();
        double freteCegonha = viaCegonha.planejarViagem("SÃ£o Paulo -> Curitiba", 410, 1);
        viaGuincho.planejarViagem("SÃ£o Paulo -> Curitiba", 410, 1);
        viaCegonha.planejarViagem("SÃ£o Paulo -> Recife", 2660, 8);

        System.out.println("\n===== 3) OBSERVER =====");
        Pedido pedido = new Pedido(1001, "Cliente Exemplo Ltda", "ABC1D23", freteCegonha);
        pedido.adicionarObservador(new NotificadorCliente());
        pedido.adicionarObservador(new PainelLogistica());
        pedido.adicionarObservador(new Financeiro());

        pedido.alterarStatus(StatusPedido.EM_TRANSITO);
        pedido.alterarStatus(StatusPedido.ENTREGUE);

        System.out.println("\n===== HistÃ³rico gravado no banco (Singleton) =====");
        BancoDeDados.getInstancia().listar().forEach(r -> System.out.println(" - " + r));
        System.out.println("Total de operaÃ§Ãµes no banco: " + banco1.getConsultasRealizadas());
    }
}
```

## Como compilar e executar

No terminal, dentro da pasta do projeto:

```
javac -encoding UTF-8 -d out src/transporte/*.java src/transporte/*/*.java
java -cp out transporte.Main
```

(TambÃ©m roda direto no IntelliJ, Eclipse ou VS Code: abra a pasta `src` e execute a classe `Main`.)

## SaÃ­da obtida

```
===== 1) SINGLETON =====
[BancoDeDados] Abrindo conexÃ£o com o banco... (sÃ³ deve aparecer 1 vez)
banco1 e banco2 sÃ£o o MESMO objeto? true

===== 2) FACTORY METHOD =====
[LogÃ­stica] SÃ£o Paulo -> Curitiba | Cegonha | 410 km | 1 veÃ­culo(s) | frete R$ 354.09
[LogÃ­stica] SÃ£o Paulo -> Curitiba | Guincho plataforma | 410 km | 1 veÃ­culo(s) | frete R$ 1872.00
[LogÃ­stica] SÃ£o Paulo -> Recife | Cegonha | 2660 km | 8 veÃ­culo(s) | frete R$ 18378.18

===== 3) OBSERVER =====

>> Pedido #1001 mudou para EM_TRANSITO
[BancoDeDados] Salvo: Pedido #1001 -> EM_TRANSITO
   [Cliente] Mensagem para Cliente Exemplo Ltda: seu veÃ­culo ABC1D23 estÃ¡ EM_TRANSITO.
   [Painel] Pedido #1001 movido para a coluna EM_TRANSITO.

>> Pedido #1001 mudou para ENTREGUE
[BancoDeDados] Salvo: Pedido #1001 -> ENTREGUE
   [Cliente] Mensagem para Cliente Exemplo Ltda: seu veÃ­culo ABC1D23 estÃ¡ ENTREGUE.
   [Painel] Pedido #1001 movido para a coluna ENTREGUE.
   [Financeiro] CobranÃ§a gerada: Cliente Exemplo Ltda, R$ 354.09.

===== HistÃ³rico gravado no banco (Singleton) =====
 - Pedido #1001 -> EM_TRANSITO
 - Pedido #1001 -> ENTREGUE
Total de operaÃ§Ãµes no banco: 3
```

# ConclusÃ£o

Com os trÃªs padrÃµes o sistema ficou mais fÃ¡cil de manter e de crescer:

- **Singleton:** um Ãºnico ponto de acesso ao banco, sem desperdÃ­cio de conexÃµes;
- **Factory Method:** novos tipos de transporte entram sem alterar a regra de planejamento;
- **Observer:** novas Ã¡reas passam a receber avisos sem mexer na classe `Pedido`.

Os trÃªs reforÃ§am princÃ­pios da orientaÃ§Ã£o a objetos: **encapsulamento** (construtor privado, atributos privados), **abstraÃ§Ã£o** (classe abstrata e interfaces), **heranÃ§a** (criadores concretos), **polimorfismo** (cada `Transporte` calcula o frete do seu jeito; cada observador reage do seu jeito) e **baixo acoplamento**.

# ReferÃªncias

- REFACTORING.GURU. *CatÃ¡logo de padrÃµes de projeto.* DisponÃ­vel em: <https://refactoring.guru/pt-br/design-patterns/catalog>.
- DEVMEDIA. *PadrÃµes de projeto em Java.* DisponÃ­vel em: <https://www.devmedia.com.br/guia/padroes-de-projeto-em-java/34456>.
- GAMMA, E.; HELM, R.; JOHNSON, R.; VLISSIDES, J. *PadrÃµes de Projeto: soluÃ§Ãµes reutilizÃ¡veis de software orientado a objetos.* Porto Alegre: Bookman, 2000.
