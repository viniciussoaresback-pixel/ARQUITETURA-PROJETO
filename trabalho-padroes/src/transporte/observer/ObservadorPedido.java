package transporte.observer;

/** PADRÃO OBSERVER — interface que todo "interessado" no pedido implementa. */
public interface ObservadorPedido {
    void atualizar(Pedido pedido);
}
