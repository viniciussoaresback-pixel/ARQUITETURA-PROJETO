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

/** Demonstração dos 3 padrões só no console (sem tela). */
public class DemoConsole {
    public static void main(String[] args) {
        System.out.println("===== 1) SINGLETON =====");
        BancoDeDados banco1 = BancoDeDados.getInstancia();
        BancoDeDados banco2 = BancoDeDados.getInstancia();
        System.out.println("banco1 e banco2 são o MESMO objeto? " + (banco1 == banco2));

        System.out.println("\n===== 2) FACTORY METHOD =====");
        // O código cliente só conhece a classe abstrata Logistica.
        Logistica viaCegonha = new LogisticaCegonha();
        Logistica viaGuincho = new LogisticaGuincho();
        double freteCegonha = viaCegonha.planejarViagem("São Paulo -> Curitiba", 410, 1);
        viaGuincho.planejarViagem("São Paulo -> Curitiba", 410, 1);
        viaCegonha.planejarViagem("São Paulo -> Recife", 2660, 8);

        System.out.println("\n===== 3) OBSERVER =====");
        Pedido pedido = new Pedido(1001, "Cliente Exemplo Ltda", "ABC1D23",
                viaCegonha.getTipoTransporte(), freteCegonha);
        pedido.adicionarObservador(new NotificadorCliente());
        pedido.adicionarObservador(new PainelLogistica());
        pedido.adicionarObservador(new Financeiro());

        pedido.alterarStatus(StatusPedido.EM_TRANSITO);
        pedido.alterarStatus(StatusPedido.ENTREGUE);

        System.out.println("\n===== Histórico gravado no banco (Singleton) =====");
        BancoDeDados.getInstancia().listar().forEach(r -> System.out.println(" - " + r));
        System.out.println("Total de operações no banco: " + banco1.getConsultasRealizadas());
    }
}
