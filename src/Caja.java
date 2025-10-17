import java.util.LinkedList;
import java.util.Queue;

public class Caja {
    private int id;
    private Queue<Cliente> cola;
    private Cliente clienteActual;
    private int tiempoOcioso;
    private int clientesAtendidos;
    private boolean abierta;
    private int x, y;
    private int tiempoSimulacionGlobal;
    private int tiempoAbierta;

    public Caja(int id, int x, int y) {
        this.id = id;
        this.cola = new LinkedList<>();
        this.clienteActual = null;
        this.tiempoOcioso = 0;
        this.clientesAtendidos = 0;
        this.abierta = false;
        this.x = x;
        this.y = y;
        this.tiempoSimulacionGlobal = 0;
        this.tiempoAbierta = 0;
    }

    public void setTiempoSimulacionGlobal(int tiempo) {
        this.tiempoSimulacionGlobal = tiempo;
    }

    public void agregarCliente(Cliente cliente) {
        cola.add(cliente);
        cliente.setEstado(Cliente.EstadoCliente.ESPERANDO_CAJA);
    }

    public void siguienteCliente() {
        if (!cola.isEmpty() && clienteActual == null) {
            clienteActual = cola.poll();
            clienteActual.setTiempoInicioServicio(tiempoSimulacionGlobal);
            clienteActual.setEstado(Cliente.EstadoCliente.SIENDO_ATENDIDO);
            clientesAtendidos++;
        }
    }

    public void actualizar() {
        if (abierta) {
            tiempoAbierta++; // Contar tiempo abierta
        }

        if (clienteActual != null) {
            //Controlar tiempo de servicio REAL
            int tiempoTranscurrido = tiempoSimulacionGlobal - clienteActual.getTiempoInicioServicio();

            // Verificar si ya terminó de pagar (tiempo REAL)
            if (tiempoTranscurrido >= clienteActual.getTiempoServicio()) {
                clienteActual.setEstado(Cliente.EstadoCliente.TERMINADO);
            }

            // Si terminó, limpiar cliente actual
            if (clienteActual.getEstado() == Cliente.EstadoCliente.TERMINADO) {
                clienteActual = null;
            }
        } else {
            tiempoOcioso++;
            // Tomar siguiente cliente si está disponible
            if (!cola.isEmpty()) {
                siguienteCliente();
            }
        }
    }

    public boolean necesitaCerrarse() {
        return isAbierta() &&
                getCola().isEmpty() &&
                getClienteActual() == null &&
                getTiempoOcioso() > 30; // 30 minutos de inactividad
    }

    public int getId() { return id; }
    public Queue<Cliente> getCola() { return cola; }
    public Cliente getClienteActual() { return clienteActual; }
    public int getTiempoOcioso() { return tiempoOcioso; }
    public int getClientesAtendidos() { return clientesAtendidos; }
    public boolean isAbierta() { return abierta; }
    public void setAbierta(boolean abierta) { this.abierta = abierta; }
    public int getTamanioCola() { return cola.size(); }
    public int getX() { return x; }
    public int getY() { return y; }

    public void setTiempoOcioso(int tiempoOcioso) {
        this.tiempoOcioso = tiempoOcioso;
    }

    public void setClienteActual(Cliente cliente) {
        this.clienteActual = cliente;
    }

    public int getTiempoAbierta() { return tiempoAbierta; }

    public boolean estaLlena() {
        // En modo pandemia: máximo 2 clientes (1 atendido + 1 en cola)
        // En modo normal: máximo 5 clientes (1 atendido + 4 en cola)
        int maxPermitido = 4; // Por defecto para modo normal
        return getTamanioCola() >= maxPermitido;
    }

    public int getTiempoSimulacionGlobal() {
        return tiempoSimulacionGlobal;
    }
}