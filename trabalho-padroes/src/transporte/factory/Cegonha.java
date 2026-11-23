package transporte.factory;

/** Produto concreto: caminhão cegonha, leva até 11 carros e divide o custo entre eles. */
public class Cegonha implements Transporte {
    private static final double PRECO_KM = 9.50;

    @Override public String getTipo() { return "Cegonha"; }
    @Override public int getCapacidadeVeiculos() { return 11; }

    @Override
    public double calcularFrete(double distanciaKm, int quantidadeVeiculos) {
        // O custo da viagem é rateado pela capacidade: cada carro paga a sua "vaga".
        return (distanciaKm * PRECO_KM / getCapacidadeVeiculos()) * quantidadeVeiculos;
    }
}
