package transporte.observer;

/** Observador concreto: avisa o cliente a cada mudança. */
public class NotificadorCliente implements ObservadorPedido {
    @Override
    public void atualizar(Pedido p) {
        System.out.printf("   [Cliente] Mensagem para %s: seu veículo %s está %s.%n",
                p.getCliente(), p.getPlaca(), p.getStatus());
    }
}
