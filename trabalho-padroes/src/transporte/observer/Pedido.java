package transporte.observer;

import java.util.ArrayList;
import java.util.List;
import transporte.singleton.BancoDeDados;

/**
 * PADRÃO OBSERVER — Sujeito (Subject / Publisher).
 * Quando o status do pedido muda, ele avisa automaticamente todos os observadores
 * cadastrados, sem precisar conhecer a classe concreta de cada um.
 */
public class Pedido {
    private final int numero;
    private final String cliente;
    private final String placa;
    private final String transporte;
    private final double valorFrete;
    private StatusPedido status = StatusPedido.AGUARDANDO_COLETA;

    private final List<ObservadorPedido> observadores = new ArrayList<>();

    public Pedido(int numero, String cliente, String placa, String transporte, double valorFrete) {
        this.numero = numero;
        this.cliente = cliente;
        this.placa = placa;
        this.transporte = transporte;
        this.valorFrete = valorFrete;
    }

    public void adicionarObservador(ObservadorPedido o) { observadores.add(o); }
    public void removerObservador(ObservadorPedido o) { observadores.remove(o); }

    private void notificarObservadores() {
        for (ObservadorPedido o : observadores) {
            o.atualizar(this);
        }
    }

    public void alterarStatus(StatusPedido novoStatus) {
        this.status = novoStatus;
        System.out.println("\n>> Pedido #" + numero + " mudou para " + novoStatus);
        // Usa o Singleton: todo o sistema grava no MESMO banco.
        BancoDeDados.getInstancia().salvar("Pedido #" + numero + " -> " + novoStatus);
        notificarObservadores();
    }

    public int getNumero() { return numero; }
    public String getCliente() { return cliente; }
    public String getPlaca() { return placa; }
    public String getTransporte() { return transporte; }
    public double getValorFrete() { return valorFrete; }
    public StatusPedido getStatus() { return status; }
}
