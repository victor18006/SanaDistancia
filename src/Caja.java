public class Caja {
    private int id;
    private Cola<Cliente> cola;
    private Cliente clienteActual;
    private int tiempoOcioso;
    private int clientesAtendidos;
    private boolean abierta;
    private int x, y;
    private int tiempoSimulacionGlobal;
    private int tiempoAbierta;
    // Nuevos campos para estadísticas
    private int tiempoEsperaTotal;
    private int tiempoServicioTotal;

    public Caja(int id, int x, int y) {
        this.id = id;
        this.cola = new Cola<>();
        this.clienteActual = null;
        this.tiempoOcioso = 0;
        this.clientesAtendidos = 0;
        this.abierta = false;
        this.x = x;
        this.y = y;
        this.tiempoSimulacionGlobal = 0;
        this.tiempoAbierta = 0;
        this.tiempoEsperaTotal = 0;
        this.tiempoServicioTotal = 0;
    }

    public void setTiempoSimulacionGlobal(int tiempo) {
        this.tiempoSimulacionGlobal = tiempo;
    }

    public void agregarCliente(Cliente cliente) {
        cola.add(cliente);
        cliente.setEstado(Cliente.EstadoCliente.ESPERANDO_CAJA);
    }

    public void siguienteCliente() {
        if (!cola.estaVacia() && clienteActual == null) {
            clienteActual = cola.poll();
            clienteActual.setTiempoInicioServicio(tiempoSimulacionGlobal);
            clienteActual.setEstado(Cliente.EstadoCliente.SIENDO_ATENDIDO);
            clientesAtendidos++;

            // Acumular tiempo de espera del cliente
            int tiempoEsperaCliente = tiempoSimulacionGlobal - clienteActual.getTiempoLlegada();
            tiempoEsperaTotal += tiempoEsperaCliente;
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
                clienteActual.setTiempoSalida(tiempoSimulacionGlobal); // Registrar tiempo de salida

                // Acumular tiempo de servicio del cliente
                tiempoServicioTotal += clienteActual.getTiempoServicio();
            }

            // Si terminó, limpiar cliente actual
            if (clienteActual.getEstado() == Cliente.EstadoCliente.TERMINADO) {
                //clienteActual = null;
            }
        } else {
            tiempoOcioso++;
            // Tomar siguiente cliente si está disponible
            if (!cola.estaVacia()) {
                siguienteCliente();
            }
        }
    }

    public boolean necesitaCerrarse() {
        return isAbierta() &&
                getCola().estaVacia() &&
                getClienteActual() == null &&
                getTiempoOcioso() > 30; // 30 minutos de inactividad
    }

    public int getId() { return id; }
    public Cola<Cliente> getCola() { return cola; }
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

    // Nuevos métodos para estadísticas
    public double getTiempoEsperaPromedio() {
        if (clientesAtendidos == 0) return 0;
        return (double) tiempoEsperaTotal / clientesAtendidos;
    }

    public double getTiempoServicioPromedio() {
        if (clientesAtendidos == 0) return 0;
        return (double) tiempoServicioTotal / clientesAtendidos;
    }

    public int getTiempoEsperaTotal() {
        return tiempoEsperaTotal;
    }

    public int getTiempoServicioTotal() {
        return tiempoServicioTotal;
    }
}