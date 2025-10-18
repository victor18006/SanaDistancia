import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GUI extends JFrame {
    private JTabbedPane tabbedPane;
    private PanelSimulacion panelPandemia;
    private PanelSimulacion panelNormal;
    private JTextArea areaEstadisticas;
    private JButton btnIniciar, btnPausar, btnReanudar, btnReiniciar;
    private ScheduledExecutorService executor;
    private SimulacionPandemia simPandemia;
    private SimulacionNormal simNormal;
    private boolean ejecutando;

    public GUI() {
        setTitle("Simulador de Colas - Costco");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initComponents();
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    private void initComponents() {
        simPandemia = new SimulacionPandemia();
        simNormal = new SimulacionNormal();

        panelPandemia = new PanelSimulacion(simPandemia, true);
        panelNormal = new PanelSimulacion(simNormal, false);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Modo Pandemia", panelPandemia);
        tabbedPane.addTab("Modo Normal", panelNormal);

        JPanel panelControles = crearPanelControles();

        areaEstadisticas = new JTextArea(15, 50);
        areaEstadisticas.setEditable(false);
        areaEstadisticas.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollEstadisticas = new JScrollPane(areaEstadisticas);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelControles, BorderLayout.NORTH);
        panelSur.add(scrollEstadisticas, BorderLayout.CENTER);

        add(panelSur, BorderLayout.SOUTH);

        actualizarEstadisticas();
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Controles de Simulación"));

        btnIniciar = new JButton("Iniciar Simulación");
        btnPausar = new JButton("Pausar");
        btnReanudar = new JButton("Reanudar");
        btnReiniciar = new JButton("Reiniciar");

        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(false);
        btnReiniciar.setEnabled(false);

        btnIniciar.addActionListener(e -> iniciarSimulacion());
        btnPausar.addActionListener(e -> pausarSimulacion());
        btnReanudar.addActionListener(e -> reanudarSimulacion());
        btnReiniciar.addActionListener(e -> reiniciarSimulacion());

        panel.add(btnIniciar);
        panel.add(btnPausar);
        panel.add(btnReanudar);
        panel.add(btnReiniciar);

        return panel;
    }

    private void iniciarSimulacion() {
        if (!ejecutando) {
            ejecutando = true;
            btnIniciar.setEnabled(false);
            btnPausar.setEnabled(true);
            btnReiniciar.setEnabled(true);

            executor.scheduleAtFixedRate(() -> {
                if (tabbedPane.getSelectedIndex() == 0) {
                    simPandemia.ejecutarPaso();
                    panelPandemia.repaint();
                } else {
                    simNormal.ejecutarPaso();
                    panelNormal.repaint();
                }

                SwingUtilities.invokeLater(() -> actualizarEstadisticas());

                if ((tabbedPane.getSelectedIndex() == 0 && simPandemia.isTerminada()) ||
                        (tabbedPane.getSelectedIndex() == 1 && simNormal.isTerminada())) {
                    detenerSimulacion();
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    private void pausarSimulacion() {
        if (tabbedPane.getSelectedIndex() == 0) {
            simPandemia.pausar();
        } else {
            simNormal.pausar();
        }
        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(true);
    }

    private void reanudarSimulacion() {
        if (tabbedPane.getSelectedIndex() == 0) {
            simPandemia.reanudar();
        } else {
            simNormal.reanudar();
        }
        btnPausar.setEnabled(true);
        btnReanudar.setEnabled(false);
    }

    private void reiniciarSimulacion() {
        detenerSimulacion();
        simPandemia = new SimulacionPandemia();
        simNormal = new SimulacionNormal();
        panelPandemia.setSimulacion(simPandemia);
        panelNormal.setSimulacion(simNormal);
        actualizarEstadisticas();
        btnIniciar.setEnabled(true);
    }

    private void detenerSimulacion() {
        ejecutando = false;
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor = Executors.newSingleThreadScheduledExecutor();

        btnIniciar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(false);
    }

    private void actualizarEstadisticas() {
        StringBuilder stats = new StringBuilder();

        if (tabbedPane.getSelectedIndex() == 0) {
            //stats.append("=== MODO PANDEMIA ===\n");
            stats.append("Tiempo: ").append(simPandemia.getTiempoActual()).append("/").append(simPandemia.getTiempoSimulacion()).append(" min\n");
            //stats.append("Clientes en fila única: ").append(simPandemia.getFilaUnica().size()).append("\n");
            //stats.append("Clientes atendidos: ").append(simPandemia.getClientesAtendidosTotal()).append("\n");
            //stats.append("Cajas abiertas: ").append(contarCajasAbiertas(simPandemia.getCajas())).append("/12\n");

            if (simPandemia.getClientesAtendidosTotal() > 0) {
                double tiempoPromedio = (double)simPandemia.getTiempoEsperaTotal() / simPandemia.getClientesAtendidosTotal();
                //stats.append("Tiempo espera promedio: ").append(String.format("%.1f", tiempoPromedio)).append(" min\n");
            }

            stats.append("ESTADÍSTICAS DETALLADAS POR CAJA:\n");
            stats.append(simPandemia.getEstadisticasDetalladas());
        } else {
            //stats.append("=== MODO NORMAL ===\n");
            stats.append("Tiempo: ").append(simNormal.getTiempoActual()).append("/").append(simNormal.getTiempoSimulacion()).append(" min\n");
            //stats.append("Clientes en sistema: ").append(simNormal.getClientesEnSistema().size()).append("\n");
            //stats.append("Clientes atendidos: ").append(simNormal.getClientesAtendidosTotal()).append("\n");
            //stats.append("Cajas abiertas: ").append(contarCajasAbiertas(simNormal.getCajas())).append("/12\n");

            if (simNormal.getClientesAtendidosTotal() > 0) {
                double tiempoPromedio = (double)simNormal.getTiempoEsperaTotal() / simNormal.getClientesAtendidosTotal();
                //stats.append("Tiempo espera promedio: ").append(String.format("%.1f", tiempoPromedio)).append(" min\n");
            }

            stats.append("ESTADÍSTICAS DETALLADAS POR CAJA:\n");
            stats.append(simNormal.getEstadisticasDetalladas());
        }

        areaEstadisticas.setText(stats.toString());
    }

    private int contarCajasAbiertas(java.util.List<Caja> cajas) {
        int count = 0;
        for (Caja caja : cajas) {
            if (caja.isAbierta()) count++;
        }
        return count;
    }

    @Override
    public void dispose() {
        if (executor != null) {
            executor.shutdown();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new GUI().setVisible(true);
        });
    }
}