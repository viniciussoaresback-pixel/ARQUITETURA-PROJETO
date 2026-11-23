package transporte;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import transporte.tela.TelaPrincipal;

/** Ponto de entrada: abre a tela do sistema. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Visual "Nimbus", que já vem no Java.
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                    }
                }
            } catch (Exception e) {
                // Se não conseguir, usa o visual padrão.
            }
            new TelaPrincipal().setVisible(true);
        });
    }
}
