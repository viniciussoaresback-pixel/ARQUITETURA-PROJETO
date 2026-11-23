package transporte.tela;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Redireciona o System.out para a caixa de "Registro de eventos" da tela.
 * Assim as classes dos padrões continuam simples (só usam System.out.println)
 * e tudo o que elas escrevem aparece na janela.
 */
public class SaidaParaTela extends OutputStream {
    private final JTextArea area;
    private final PrintStream consoleOriginal;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public SaidaParaTela(JTextArea area, PrintStream consoleOriginal) {
        this.area = area;
        this.consoleOriginal = consoleOriginal;
    }

    @Override
    public synchronized void write(int b) {
        buffer.write(b);
        if (b == '\n') {
            flush();
        }
    }

    @Override
    public synchronized void flush() {
        if (buffer.size() == 0) {
            return;
        }
        String texto = buffer.toString(StandardCharsets.UTF_8).replace("\r", "");
        buffer.reset();
        consoleOriginal.print(texto); // continua aparecendo no console também
        SwingUtilities.invokeLater(() -> {
            area.append(texto);
            area.setCaretPosition(area.getDocument().getLength());
        });
    }

    /** Liga o redirecionamento. */
    public static void instalar(JTextArea area) {
        PrintStream original = System.out;
        System.setOut(new PrintStream(new SaidaParaTela(area, original), true, StandardCharsets.UTF_8));
    }
}
