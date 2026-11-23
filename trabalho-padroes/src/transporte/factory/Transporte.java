package transporte.factory;

/** PADRÃO FACTORY METHOD — Produto: interface comum a todos os transportes. */
public interface Transporte {
    String getTipo();
    int getCapacidadeVeiculos();
    double calcularFrete(double distanciaKm, int quantidadeVeiculos);
}
