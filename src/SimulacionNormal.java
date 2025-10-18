import java.util.ArrayList;
import java.util.List;

public class SimulacionNormal extends SimulacionBase {
    private int siguienteIdCliente;
    private List<Cliente> clientesEnSistema;

    public SimulacionNormal() {
        super();
        this.siguienteIdCliente = 1;
        this.clientesEnSistema = new ArrayList<>();

        // Configurar 12 cajas en 2 filas de 6 cajas cada una - MOVIDAS 20 PIXELES ARRIBA
        for (int i = 0; i < 12; i++) {
            int x = 50 + (i % 6) * 180;  // 6 cajas por fila
            int y = 130 + (i / 6) * 120;  // 2 filas (150 -> 130)
            cajas.add(new Caja(i + 1, x, y));
        }

        cajas.get(random.nextInt(12)).setAbierta(true);
    }

    @Override
    public void ejecutarPaso() {
        if (pausada || terminada || tiempoActual >= tiempoSimulacion) {
            if (clientesEnSistema.isEmpty() && todasCajasVacias()) {
                terminada = true;
            }
            return;
        }

        for (Caja caja : cajas) {
            caja.setTiempoSimulacionGlobal(tiempoActual);
        }

        // Generar clientes usando tiempo REAL
        if (debeLlegarCliente() && siguienteIdCliente <= 1000) {
            Cliente nuevoCliente = new Cliente(siguienteIdCliente++, tiempoActual);
            clientesEnSistema.add(nuevoCliente);
            asignarClienteACaja(nuevoCliente);
            programarProximaLlegada(); // Programar próxima llegada
        }

        for (Caja caja : cajas) {
            if (caja.isAbierta()) {
                caja.actualizar();
                if (caja.getClienteActual() != null &&
                        caja.getClienteActual().getEstado() == Cliente.EstadoCliente.TERMINADO) {
                    // En lugar de eliminar, mover a la cola de salida
                    Cliente clienteTerminado = caja.getClienteActual();
                    clienteTerminado.setEstado(Cliente.EstadoCliente.EN_SALIDA);
                    colaSalida.add(clienteTerminado); // Agregar a cola de salida
                    clientesAtendidosTotal++;
                    tiempoEsperaTotal += clienteTerminado.getTiempoEspera();
                    clientesEnSistema.remove(clienteTerminado);
                    caja.setClienteActual(null);
                }
            }
        }

        // Reasignar clientes que no encontraron caja inicialmente
        List<Cliente> clientesSinCaja = new ArrayList<>();
        for (Cliente cliente : clientesEnSistema) {
            if (cliente.getEstado() == Cliente.EstadoCliente.ESPERANDO_FILA) {
                clientesSinCaja.add(cliente);
            }
        }
        for (Cliente cliente : clientesSinCaja) {
            asignarClienteACaja(cliente);
        }

        // Actualizar cola de salida (remover clientes que llevan 1 minuto)
        actualizarColaSalida();

        abrirCajaSiEsNecesario();
        cerrarCajasOciosas();

        tiempoActual++;

        if (tiempoActual >= tiempoSimulacion && clientesEnSistema.isEmpty() && todasCajasVacias()) {
            terminada = true;
        }
    }

    private void asignarClienteACaja(Cliente cliente) {
        Caja cajaMenosOcupada = null;
        int menorLongitud = Integer.MAX_VALUE;

        for (Caja caja : cajas) {
            if (caja.isAbierta() && caja.getTamanioCola() < menorLongitud) {
                menorLongitud = caja.getTamanioCola();
                cajaMenosOcupada = caja;
            }
        }

        if (cajaMenosOcupada != null) {
            cajaMenosOcupada.agregarCliente(cliente);
            cliente.setEstado(Cliente.EstadoCliente.ESPERANDO_CAJA);
            cliente.setTiempoEspera(tiempoActual - cliente.getTiempoLlegada());
        }
    }

    private boolean todasCajasVacias() {
        for (Caja caja : cajas) {
            if (caja.isAbierta() && (caja.getClienteActual() != null || !caja.getCola().estaVacia())) {
                return false;
            }
        }
        return true;
    }

    public List<Cliente> getClientesEnSistema() { return clientesEnSistema; }

    public String getEstadisticasDetalladas() {
        StringBuilder stats = new StringBuilder();

        for (Caja caja : cajas) {
            stats.append("Caja ").append(caja.getId())
                    .append(": ").append(caja.isAbierta() ? "ABIERTA" : "CERRADA")
                    .append(" | Atendidos: ").append(caja.getClientesAtendidos())
                    .append(" | Cola: ").append(caja.getTamanioCola())
                    .append(" | Tiempo abierta: ").append(caja.getTiempoAbierta()).append(" min")
                    .append(" | Espera promedio: ").append(String.format("%.1f", caja.getTiempoEsperaPromedio())).append(" min")
                    .append(" | Pago promedio: ").append(String.format("%.1f", caja.getTiempoServicioPromedio())).append(" min\n");
        }

        return stats.toString();
    }

    public List<Cliente> getClientesTerminados() {
        // Ahora devolvemos la cola de salida en lugar de lista vacía
        //return new ArrayList<>(colaSalida);
        return colaSalida.toArrayList();
    }
}