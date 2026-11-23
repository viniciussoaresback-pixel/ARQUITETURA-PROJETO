package transporte.singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PADRÃO SINGLETON
 * Garante que exista UMA única conexão com o banco de dados em todo o sistema.
 * (Aqui o "banco" é simulado em memória para o exemplo rodar sem instalar nada.)
 */
public final class BancoDeDados {

    // 1) A única instância fica guardada num atributo estático e privado.
    //    "volatile" garante que todas as threads enxerguem o valor atualizado.
    private static volatile BancoDeDados instancia;

    private final List<String> registros = new ArrayList<>();
    private int consultasRealizadas = 0;

    // 2) Construtor PRIVADO: ninguém fora da classe consegue fazer "new BancoDeDados()".
    private BancoDeDados() {
        System.out.println("[BancoDeDados] Abrindo conexão com o banco... (só deve aparecer 1 vez)");
    }

    // 3) Ponto de acesso global. Cria a instância só na primeira chamada (lazy)
    //    e usa "double-checked locking" para ser seguro com várias threads.
    public static BancoDeDados getInstancia() {
        if (instancia == null) {
            synchronized (BancoDeDados.class) {
                if (instancia == null) {
                    instancia = new BancoDeDados();
                }
            }
        }
        return instancia;
    }

    public synchronized void salvar(String registro) {
        consultasRealizadas++;
        registros.add(registro);
        System.out.println("[BancoDeDados] Salvo: " + registro);
    }

    public synchronized List<String> listar() {
        consultasRealizadas++;
        return Collections.unmodifiableList(new ArrayList<>(registros));
    }

    public int getConsultasRealizadas() {
        return consultasRealizadas;
    }
}
