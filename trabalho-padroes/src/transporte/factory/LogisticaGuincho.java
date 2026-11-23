package transporte.factory;

/** Criador concreto: decide que o transporte será um Guincho. */
public class LogisticaGuincho extends Logistica {
    @Override
    protected Transporte criarTransporte() {
        return new Guincho();
    }
}
