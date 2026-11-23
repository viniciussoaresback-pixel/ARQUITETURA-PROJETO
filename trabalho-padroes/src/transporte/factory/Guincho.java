package transporte.factory;

/** Produto concreto: guincho plataforma, leva 1 carro (entregas urgentes ou curtas). */
public class Guincho implements Transporte {
    private static final double PRECO_KM = 4.20;
    private static final double TAXA_SAIDA = 150.00;

    @Override public String getTipo() { return "Guincho plataforma"; }
    @Override public int getCapacidadeVeiculos() { return 1; }

    @Override
    public double calcularFrete(double distanciaKm, int quantidadeVeiculos) {
        // Uma viagem por carro: cada veículo paga a viagem inteira.
        return (TAXA_SAIDA + distanciaKm * PRECO_KM) * quantidadeVeiculos;
    }
}
