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

        areaEstadisticas = new JTextArea(10, 50);
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
            }, 0, 100, TimeUnit.MILLISECONDS);
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
            stats.append("MODO PANDEMIA\n");
            stats.append("Tiempo: ").append(simPandemia.getTiempoActual()).append("/").append(simPandemia.getTiempoSimulacion()).append(" min\n");
            stats.append("Clientes en fila única: ").append(simPandemia.getFilaUnica().size()).append("\n");
            stats.append("Clientes atendidos: ").append(simPandemia.getClientesAtendidosTotal()).append("\n");

            if (simPandemia.getClientesAtendidosTotal() > 0) {
                stats.append("Tiempo espera promedio: ")
                        .append(String.format("%.1f", (double)simPandemia.getTiempoEsperaTotal() / simPandemia.getClientesAtendidosTotal()))
                        .append(" min\n");
            }

            stats.append("Cajas abiertas: ");
            for (Caja caja : simPandemia.getCajas()) {
                if (caja.isAbierta()) {
                    stats.append(caja.getId()).append(" ");
                }
            }
        } else {
            stats.append("MODO NORMAL\n");
            stats.append("Tiempo: ").append(simNormal.getTiempoActual()).append("/").append(simNormal.getTiempoSimulacion()).append(" min\n");
            stats.append("Clientes en sistema: ").append(simNormal.getClientesEnSistema().size()).append("\n");
            stats.append("Clientes atendidos: ").append(simNormal.getClientesAtendidosTotal()).append("\n");

            if (simNormal.getClientesAtendidosTotal() > 0) {
                stats.append("Tiempo espera promedio: ")
                        .append(String.format("%.1f", (double)simNormal.getTiempoEsperaTotal() / simNormal.getClientesAtendidosTotal()))
                        .append(" min\n");
            }

            stats.append("Cajas abiertas: ");
            for (Caja caja : simNormal.getCajas()) {
                if (caja.isAbierta()) {
                    stats.append(caja.getId()).append(" ");
                }
            }
        }

        areaEstadisticas.setText(stats.toString());
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

class PanelSimulacion extends JPanel {
    private SimulacionBase simulacion;
    private boolean esPandemia;

    public PanelSimulacion(SimulacionBase simulacion, boolean esPandemia) {
        this.simulacion = simulacion;
        this.esPandemia = esPandemia;
        setPreferredSize(new Dimension(1150, 500));
        setBackground(new Color(240, 240, 240));
    }

    public void setSimulacion(SimulacionBase simulacion) {
        this.simulacion = simulacion;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (simulacion != null) {
            if (esPandemia) {
                dibujarSimulacionPandemia(g2d);
            } else {
                dibujarSimulacionNormal(g2d);
            }
        }
    }

    private void dibujarSimulacionPandemia(Graphics2D g) {
        SimulacionPandemia sim = (SimulacionPandemia) simulacion;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("MODO PANDEMIA - FILA ÚNICA", 20, 30);

        g.setColor(Color.DARK_GRAY);
        g.drawString("Fila Única (" + sim.getFilaUnica().size() + " clientes):", 20, 60);

        int xFila = 20;
        int yFila = 80;
        int clienteIndex = 0;

        for (Cliente cliente : sim.getFilaUnica()) {
            dibujarCliente(g, cliente, xFila + (clienteIndex % 10) * 40, yFila + (clienteIndex / 10) * 30);
            clienteIndex++;
        }

        for (Caja caja : sim.getCajas()) {
            dibujarCaja(g, caja);
        }
    }

    private void dibujarSimulacionNormal(Graphics2D g) {
        SimulacionNormal sim = (SimulacionNormal) simulacion;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("MODO NORMAL - MÚLTIPLES FILAS", 20, 30);

        g.drawString("Clientes buscando caja: " + sim.getClientesEnSistema().size(), 20, 60);

        int xCliente = 20;
        int yCliente = 80;
        int clienteIndex = 0;

        for (Cliente cliente : sim.getClientesEnSistema()) {
            if (cliente.getEstado() == Cliente.EstadoCliente.ESPERANDO_FILA) {
                dibujarCliente(g, cliente, xCliente + (clienteIndex % 10) * 40, yCliente + (clienteIndex / 10) * 30);
                clienteIndex++;
            }
        }

        for (Caja caja : sim.getCajas()) {
            dibujarCaja(g, caja);
        }
    }

    private void dibujarCaja(Graphics2D g, Caja caja) {
        int x = caja.getX();
        int y = caja.getY();

        if (caja.isAbierta()) {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, 90, 60);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, 90, 60);

            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Caja " + caja.getId(), x + 20, y + 15);
            g.drawString("Atendidos: " + caja.getClientesAtendidos(), x + 5, y + 30);

            if (caja.getClienteActual() != null) {
                dibujarCliente(g, caja.getClienteActual(), x + 25, y - 30);

                int progreso = (int)((1.0 - (double)caja.getClienteActual().getTiempoRestanteServicio() /
                        caja.getClienteActual().getTiempoServicio()) * 50);
                g.setColor(Color.BLUE);
                g.fillRect(x + 15, y + 40, progreso, 5);
            }

            int xCola = x + 90;
            int yCola = y;
            int clienteIndex = 0;

            for (Cliente cliente : caja.getCola()) {
                dibujarCliente(g, cliente, xCola, yCola + clienteIndex * 25);
                clienteIndex++;
            }

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("Cola: " + caja.getTamanioCola(), xCola, yCola - 5);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, y, 90, 60);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, 90, 60);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Caja " + caja.getId(), x + 20, y + 15);
            g.drawString("CERRADA", x + 15, y + 35);
        }
    }

    private void dibujarCliente(Graphics2D g, Cliente cliente, int x, int y) {
        Color color = cliente.getColor();

        g.setColor(color);
        g.fillOval(x, y, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, 20, 20);

        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.drawString(String.valueOf(cliente.getId()), x + 6, y + 13);

        switch (cliente.getEstado()) {
            case ESPERANDO_FILA:
                g.setColor(Color.ORANGE);
                break;
            case ESPERANDO_CAJA:
                g.setColor(Color.YELLOW);
                break;
            case SIENDO_ATENDIDO:
                g.setColor(Color.GREEN);
                break;
            case TERMINADO:
                g.setColor(Color.RED);
                break;
        }
        g.fillRect(x + 15, y + 15, 5, 5);
    }
}