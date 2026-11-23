package transporte.factory;

/**
 * PADRÃO FACTORY METHOD — Criador (Creator).
 * Contém a regra de negócio comum (planejar a viagem), mas deixa para as
 * subclasses decidirem QUAL transporte será criado.
 */
public abstract class Logistica {

    // O "Factory Method": cada subclasse devolve o seu tipo de transporte.
    protected abstract Transporte criarTransporte();

    // Regra de negócio que NÃO depende da classe concreta do transporte.
    public double planejarViagem(String rota, double distanciaKm, int quantidadeVeiculos) {
        Transporte transporte = criarTransporte();
        if (quantidadeVeiculos > transporte.getCapacidadeVeiculos()
                && transporte.getCapacidadeVeiculos() > 1) {
            throw new IllegalArgumentException("Capacidade excedida para " + transporte.getTipo()
                    + " (máximo " + transporte.getCapacidadeVeiculos() + " veículos)");
        }
        double frete = transporte.calcularFrete(distanciaKm, quantidadeVeiculos);
        System.out.printf("[Logística] %s | %s | %.0f km | %d veículo(s) | frete R$ %.2f%n",
                rota, transporte.getTipo(), distanciaKm, quantidadeVeiculos, frete);
        return frete;
    }

    // Usado pela tela para mostrar qual transporte esta logística cria.
    public String getTipoTransporte() {
        return criarTransporte().getTipo();
    }
}
