package transporte.factory;

/** Criador concreto: decide que o transporte será uma Cegonha. */
public class LogisticaCegonha extends Logistica {
    @Override
    protected Transporte criarTransporte() {
        return new Cegonha();
    }
}
