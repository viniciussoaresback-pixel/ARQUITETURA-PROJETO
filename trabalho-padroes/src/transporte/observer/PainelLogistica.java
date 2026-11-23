package transporte.observer;

/** Observador concreto: atualiza o painel da equipe de logística. */
public class PainelLogistica implements ObservadorPedido {
    @Override
    public void atualizar(Pedido p) {
        System.out.printf("   [Painel] Pedido #%d movido para a coluna %s.%n",
                p.getNumero(), p.getStatus());
    }
}
