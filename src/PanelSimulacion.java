import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelSimulacion extends JPanel {
    private SimulacionBase simulacion;
    private boolean esPandemia;

    public PanelSimulacion(SimulacionBase simulacion, boolean esPandemia) {
        this.simulacion = simulacion;
        this.esPandemia = esPandemia;
        setPreferredSize(new Dimension(1350, 500));
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
            dibujarSalida(g2d);

            if (esPandemia) {
                dibujarSimulacionPandemia(g2d);
            } else {
                dibujarSimulacionNormal(g2d);
            }
        }
    }

    private void dibujarSalida(Graphics2D g) {
        // SALIDA MUCHO MÁS A LA DERECHA
        int xSalida = 1200;
        int ySalida = 150;

        g.setColor(Color.blue);
        g.drawRect(xSalida, ySalida, 120, 180);

        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString("SALIDA", xSalida + 35, ySalida - 10);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Atendidos: " + simulacion.getClientesAtendidosTotal(),
                xSalida, ySalida + 270);

        g.drawString("Últimos clientes:", xSalida, ySalida + 290);

        List<Cliente> clientesTerminados = obtenerClientesTerminados();
        int maxClientesMostrar = Math.min(clientesTerminados.size(), 12);

        for (int i = 0; i < maxClientesMostrar; i++) {
            Cliente cliente = clientesTerminados.get(clientesTerminados.size() - 1 - i);
            int xCliente = xSalida + 10 + (i % 3) * 35;
            int yCliente = ySalida + 80 + (i / 3) * 30;
            dibujarClienteSalida(g, cliente, xCliente, yCliente);
        }
    }

    private List<Cliente> obtenerClientesTerminados() {
        // Ahora usa la cola de salida de SimulacionBase
        return new ArrayList<>(simulacion.getColaSalida());
    }

    private void dibujarClienteSalida(Graphics2D g, Cliente cliente, int x, int y) {
        Color color = cliente.getColor();

        g.setColor(color);
        g.fillOval(x, y, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, 20, 20);

        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.drawString(String.valueOf(cliente.getId()), x + 6, y + 13);

        g.setColor(Color.RED);
        g.fillRect(x + 15, y + 15, 5, 5);
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

            if (caja.estaLlena()) {
                g.setColor(Color.RED);
                g.drawString("LLENA", x + 25, y + 45);
            }

            if (caja.getClienteActual() != null) {
                dibujarCliente(g, caja.getClienteActual(), x + 25, y - 30);

                //Calcular progreso basado en tiempo REAL
                int tiempoTranscurrido = caja.getTiempoSimulacionGlobal() -
                        caja.getClienteActual().getTiempoInicioServicio();
                int tiempoTotal = caja.getClienteActual().getTiempoServicio();

                double porcentajeCompletado = Math.min(1.0, (double) tiempoTranscurrido / tiempoTotal);
                int progreso = (int)(porcentajeCompletado * 50);

                g.setColor(Color.BLUE);
                g.fillRect(x + 15, y + 40, progreso, 5);

                // Dibujar tiempo restante
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.PLAIN, 9));
                int tiempoRestante = Math.max(0, tiempoTotal - tiempoTranscurrido);
                g.drawString(tiempoRestante + " min", x + 15, y + 55);
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
            case EN_SALIDA:
                g.setColor(Color.RED);
                break;
        }
        g.fillRect(x + 15, y + 15, 5, 5);
    }
}