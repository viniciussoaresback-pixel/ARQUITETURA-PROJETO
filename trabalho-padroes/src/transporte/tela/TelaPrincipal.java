package transporte.tela;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import transporte.factory.Logistica;
import transporte.factory.LogisticaCegonha;
import transporte.factory.LogisticaGuincho;
import transporte.observer.Financeiro;
import transporte.observer.NotificadorCliente;
import transporte.observer.ObservadorPedido;
import transporte.observer.PainelLogistica;
import transporte.observer.Pedido;
import transporte.observer.StatusPedido;
import transporte.singleton.BancoDeDados;

/**
 * Tela principal do sistema (Java Swing).
 * Ela também é um OBSERVADOR: quando um pedido muda de status,
 * a tela é avisada e atualiza a tabela sozinha.
 */
public class TelaPrincipal extends JFrame implements ObservadorPedido {

    private static final Color AZUL = new Color(0x1E3A5F);
    private static final NumberFormat REAL = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    // Rotas de exemplo: nome e distância em km.
    private static final String[] ROTAS = {
        "São Paulo → Curitiba", "São Paulo → Rio de Janeiro", "São Paulo → Belo Horizonte",
        "Curitiba → Porto Alegre", "São Paulo → Recife"
    };
    private static final double[] DISTANCIAS = {410, 430, 590, 710, 2660};

    private final List<Pedido> pedidos = new ArrayList<>();
    private int proximoNumero = 1001;

    // Campos do formulário
    private final JTextField campoCliente = new JTextField("Cliente Exemplo Ltda", 18);
    private final JTextField campoPlaca = new JTextField("ABC1D23", 8);
    private final JComboBox<String> comboRota = new JComboBox<>(ROTAS);
    private final JSpinner campoQtd = new JSpinner(new SpinnerNumberModel(1, 1, 11, 1));
    private final JRadioButton opCegonha = new JRadioButton("Cegonha (até 11 carros)", true);
    private final JRadioButton opGuincho = new JRadioButton("Guincho (1 carro)");
    private final JCheckBox avisaCliente = new JCheckBox("Cliente", true);
    private final JCheckBox avisaPainel = new JCheckBox("Painel da logística", true);
    private final JCheckBox avisaFinanceiro = new JCheckBox("Financeiro", true);
    private final JLabel rotuloFrete = new JLabel("Frete: —");

    private final ModeloTabela modeloTabela = new ModeloTabela();
    private final JTable tabela = new JTable(modeloTabela);
    private final JTextArea areaLog = new JTextArea();
    private final JLabel barraStatus = new JLabel(" Pronto.");

    public TelaPrincipal() {
        super("Sistema de Transporte de Veículos — Padrões de Projeto");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        areaLog.setEditable(false);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        SaidaParaTela.instalar(areaLog);

        add(montarCabecalho(), BorderLayout.NORTH);
        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, montarFormulario(), montarPedidos());
        centro.setDividerLocation(420);
        JSplitPane tudo = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centro, montarLog());
        tudo.setDividerLocation(470);
        add(tudo, BorderLayout.CENTER);
        barraStatus.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(barraStatus, BorderLayout.SOUTH);

        // Primeiro acesso ao banco: o Singleton é criado aqui.
        BancoDeDados.getInstancia();
    }

    // ---------- Montagem da tela ----------

    private JPanel montarCabecalho() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AZUL);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel titulo = new JLabel("Transportadora de Veículos");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        JLabel sub = new JLabel("Singleton  •  Factory Method  •  Observer");
        sub.setForeground(new Color(0xC7D7EA));
        JPanel textos = new JPanel(new BorderLayout());
        textos.setOpaque(false);
        textos.add(titulo, BorderLayout.NORTH);
        textos.add(sub, BorderLayout.SOUTH);
        p.add(textos, BorderLayout.WEST);

        JButton testar = new JButton("Testar Singleton");
        testar.addActionListener(e -> testarSingleton());
        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        direita.setOpaque(false);
        direita.add(testar);
        p.add(direita, BorderLayout.EAST);
        return p;
    }

    private JPanel montarFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Novo pedido  (Factory Method)"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int linha = 0;
        linha = adicionarLinha(p, c, linha, "Cliente:", campoCliente);
        linha = adicionarLinha(p, c, linha, "Placa:", campoPlaca);
        linha = adicionarLinha(p, c, linha, "Rota:", comboRota);
        linha = adicionarLinha(p, c, linha, "Qtd. de veículos:", campoQtd);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(opCegonha);
        grupo.add(opGuincho);
        JPanel tipos = new JPanel(new java.awt.GridLayout(0, 1));
        tipos.add(opCegonha);
        tipos.add(opGuincho);
        linha = adicionarLinha(p, c, linha, "Transporte:", tipos);

        JPanel avisos = new JPanel(new java.awt.GridLayout(0, 1));
        avisos.add(avisaCliente);
        avisos.add(avisaPainel);
        avisos.add(avisaFinanceiro);
        linha = adicionarLinha(p, c, linha, "Avisar (Observer):", avisos);

        rotuloFrete.setFont(rotuloFrete.getFont().deriveFont(Font.BOLD, 16f));
        rotuloFrete.setForeground(AZUL);
        c.gridx = 0;
        c.gridy = linha++;
        c.gridwidth = 2;
        p.add(rotuloFrete, c);

        JButton calcular = new JButton("Calcular frete");
        calcular.addActionListener(e -> calcularFrete());
        JButton criar = new JButton("Criar pedido");
        criar.setFont(criar.getFont().deriveFont(Font.BOLD));
        criar.addActionListener(e -> criarPedido());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botoes.add(calcular);
        botoes.add(new JLabel("  "));
        botoes.add(criar);
        c.gridy = linha++;
        p.add(botoes, c);

        c.gridy = linha;
        c.weighty = 1;
        p.add(new JLabel(), c); // empurra tudo para cima
        return p;
    }

    private int adicionarLinha(JPanel p, GridBagConstraints c, int linha, String rotulo, java.awt.Component campo) {
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = linha;
        c.weightx = 0;
        p.add(new JLabel(rotulo), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(campo, c);
        return linha + 1;
    }

    private JPanel montarPedidos() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createTitledBorder("Pedidos  (Observer)"));
        tabela.setRowHeight(26);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(150);
        p.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JButton avancar = new JButton("Avançar status ▶");
        avancar.setFont(avancar.getFont().deriveFont(Font.BOLD));
        avancar.addActionListener(e -> avancarStatus());
        JButton historico = new JButton("Ver histórico do banco");
        historico.addActionListener(e -> mostrarHistorico());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.add(avancar);
        botoes.add(historico);
        p.add(botoes, BorderLayout.SOUTH);
        return p;
    }

    private JScrollPane montarLog() {
        JScrollPane s = new JScrollPane(areaLog);
        s.setBorder(BorderFactory.createTitledBorder("Registro de eventos (o que cada classe faz)"));
        s.setPreferredSize(new Dimension(100, 220));
        return s;
    }

    // ---------- Ações dos botões ----------

    /** FACTORY METHOD: a tela escolhe o criador; o criador decide qual Transporte criar. */
    private Logistica logisticaEscolhida() {
        return opCegonha.isSelected() ? new LogisticaCegonha() : new LogisticaGuincho();
    }

    private Double calcularFrete() {
        int i = comboRota.getSelectedIndex();
        int qtd = (Integer) campoQtd.getValue();
        try {
            double frete = logisticaEscolhida().planejarViagem(ROTAS[i], DISTANCIAS[i], qtd);
            rotuloFrete.setText("Frete: " + REAL.format(frete));
            return frete;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private void criarPedido() {
        String cliente = campoCliente.getText().trim();
        String placa = campoPlaca.getText().trim().toUpperCase();
        if (cliente.isEmpty() || placa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha o cliente e a placa.", "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Double frete = calcularFrete();
        if (frete == null) {
            return;
        }
        Pedido pedido = new Pedido(proximoNumero++, cliente, placa,
                logisticaEscolhida().getTipoTransporte(), frete);

        // OBSERVER: cadastra quem quer ser avisado das mudanças deste pedido.
        if (avisaCliente.isSelected()) {
            pedido.adicionarObservador(new NotificadorCliente());
        }
        if (avisaPainel.isSelected()) {
            pedido.adicionarObservador(new PainelLogistica());
        }
        if (avisaFinanceiro.isSelected()) {
            pedido.adicionarObservador(new Financeiro());
        }
        pedido.adicionarObservador(this); // a própria tela também observa

        pedidos.add(pedido);
        BancoDeDados.getInstancia().salvar("Pedido #" + pedido.getNumero() + " criado ("
                + cliente + ", " + placa + ")");
        modeloTabela.fireTableDataChanged();
        tabela.setRowSelectionInterval(pedidos.size() - 1, pedidos.size() - 1);
        barraStatus.setText(" Pedido #" + pedido.getNumero() + " criado. Selecione-o e clique em \"Avançar status\".");
    }

    private void avancarStatus() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um pedido na tabela.");
            return;
        }
        Pedido p = pedidos.get(linha);
        if (p.getStatus() == StatusPedido.ENTREGUE) {
            JOptionPane.showMessageDialog(this, "O pedido #" + p.getNumero() + " já foi entregue.");
            return;
        }
        p.alterarStatus(p.getStatus().proximo()); // dispara o aviso aos observadores
    }

    /** SINGLETON: pede o banco duas vezes e mostra que é o mesmo objeto. */
    private void testarSingleton() {
        BancoDeDados banco1 = BancoDeDados.getInstancia();
        BancoDeDados banco2 = BancoDeDados.getInstancia();
        String msg = "banco1 == banco2 ?  " + (banco1 == banco2)
                + "\n\nbanco1 → objeto nº " + Integer.toHexString(System.identityHashCode(banco1))
                + "\nbanco2 → objeto nº " + Integer.toHexString(System.identityHashCode(banco2))
                + "\n\nÉ o MESMO objeto: só existe uma conexão com o banco.";
        System.out.println("[Singleton] banco1 == banco2 ? " + (banco1 == banco2));
        JOptionPane.showMessageDialog(this, msg, "Teste do Singleton", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarHistorico() {
        BancoDeDados banco = BancoDeDados.getInstancia();
        StringBuilder sb = new StringBuilder();
        for (String r : banco.listar()) {
            sb.append("• ").append(r).append('\n');
        }
        if (sb.length() == 0) {
            sb.append("(nada gravado ainda)\n");
        }
        sb.append("\nOperações no banco: ").append(banco.getConsultasRealizadas());
        JTextArea texto = new JTextArea(sb.toString(), 14, 46);
        texto.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(texto),
                "Histórico gravado no banco (Singleton)", JOptionPane.PLAIN_MESSAGE);
    }

    // ---------- A tela como OBSERVADOR ----------

    @Override
    public void atualizar(Pedido pedido) {
        modeloTabela.fireTableDataChanged();
        int i = pedidos.indexOf(pedido);
        if (i >= 0) {
            tabela.setRowSelectionInterval(i, i);
        }
        barraStatus.setText(" Pedido #" + pedido.getNumero() + " agora está " + pedido.getStatus() + ".");
        System.out.println("   [Tela] Tabela atualizada.");
    }

    /** Modelo da tabela: lê direto da lista de pedidos. */
    private class ModeloTabela extends AbstractTableModel {
        private final String[] colunas = {"Nº", "Cliente", "Placa", "Transporte", "Frete", "Status"};

        @Override public int getRowCount() { return pedidos.size(); }
        @Override public int getColumnCount() { return colunas.length; }
        @Override public String getColumnName(int c) { return colunas[c]; }

        @Override
        public Object getValueAt(int linha, int coluna) {
            Pedido p = pedidos.get(linha);
            switch (coluna) {
                case 0: return p.getNumero();
                case 1: return p.getCliente();
                case 2: return p.getPlaca();
                case 3: return p.getTransporte();
                case 4: return REAL.format(p.getValorFrete());
                default: return p.getStatus();
            }
        }
    }
}
