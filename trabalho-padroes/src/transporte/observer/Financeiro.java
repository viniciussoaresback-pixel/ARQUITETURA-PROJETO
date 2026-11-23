package transporte.observer;

/** Observador concreto: só age quando o pedido é ENTREGUE (gera a cobrança). */
public class Financeiro implements ObservadorPedido {
    @Override
    public void atualizar(Pedido p) {
        if (p.getStatus() == StatusPedido.ENTREGUE) {
            System.out.printf("   [Financeiro] Cobrança gerada: %s, R$ %.2f.%n",
                    p.getCliente(), p.getValorFrete());
        }
    }
}
