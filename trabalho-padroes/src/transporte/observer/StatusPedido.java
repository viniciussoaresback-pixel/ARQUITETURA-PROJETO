package transporte.observer;

/** Situações possíveis de um pedido, na ordem em que acontecem. */
public enum StatusPedido {
    AGUARDANDO_COLETA, EM_TRANSITO, ENTREGUE;

    /** Próximo status da sequência (ENTREGUE é o último). */
    public StatusPedido proximo() {
        return this == ENTREGUE ? ENTREGUE : values()[ordinal() + 1];
    }
}
