import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SimulacionPandemia extends SimulacionBase {
    private Queue<Cliente> filaUnica;
    private int siguienteIdCliente;

    public SimulacionPandemia() {
        super();
        this.filaUnica = new LinkedList<>();
        this.siguienteIdCliente = 1;

        // MOVER CAJAS 20 PIXELES HACIA ARRIBA (150 -> 130)
        for (int i = 0; i < 12; i++) {
            int x = 50 + (i % 6) * 180;
            int y = 130 + (i / 6) * 120;  // Cambiado de 150 a 130
            cajas.add(new Caja(i + 1, x, y));
        }

        cajas.get(random.nextInt(12)).setAbierta(true); // CORREGIDO: 6 cajas
    }

    @Override
    public void ejecutarPaso() {
        if (pausada || terminada || tiempoActual >= tiempoSimulacion) {
            if (filaUnica.isEmpty() && todasCajasVacias()) {
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
            filaUnica.add(nuevoCliente);
            programarProximaLlegada(); // Programar próxima llegada
        }

        asignarClientesACajas();

        // Actualizar cajas y contabilizar clientes terminados
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
                    caja.setClienteActual(null);
                }
            }
        }

        // Actualizar cola de salida (remover clientes que llevan 1 minuto)
        actualizarColaSalida();

        abrirCajaSiEsNecesario();
        cerrarCajasOciosas();

        tiempoActual++;

        if (tiempoActual >= tiempoSimulacion && filaUnica.isEmpty() && todasCajasVacias()) {
            terminada = true;
        }
    }

    private void asignarClientesACajas() {
        for (Caja caja : cajas) {
            // MODO PANDEMIA: Máximo 1 cliente en cola + 1 siendo atendido
            if (caja.isAbierta() && caja.getTamanioCola() < 1 && !filaUnica.isEmpty()) {
                Cliente cliente = filaUnica.poll();
                if (cliente != null) {
                    caja.agregarCliente(cliente);
                    cliente.setTiempoEspera(tiempoActual - cliente.getTiempoLlegada());
                }
            }
        }
    }

    private boolean todasCajasVacias() {
        for (Caja caja : cajas) {
            if (caja.isAbierta() && (caja.getClienteActual() != null || !caja.getCola().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    public Queue<Cliente> getFilaUnica() { return filaUnica; }

    public String getEstadisticasDetalladas() {
        StringBuilder stats = new StringBuilder();

        for (Caja caja : cajas) {
            stats.append("Caja ").append(caja.getId())
                    .append(": ").append(caja.isAbierta() ? "ABIERTA" : "CERRADA")
                    .append(" | Atendidos: ").append(caja.getClientesAtendidos())
                    .append(" | Cola: ").append(caja.getTamanioCola())
                    .append(" | Tiempo abierta: ").append(caja.getTiempoAbierta()).append(" min")
                    .append(" | Espera prom: ").append(String.format("%.1f", caja.getTiempoEsperaPromedio())).append(" min")
                    .append(" | Pago prom: ").append(String.format("%.1f", caja.getTiempoServicioPromedio())).append(" min\n");
        }

        return stats.toString();
    }

    public List<Cliente> getClientesTerminados() {
        // Ahora devolvemos la cola de salida en lugar de lista vacía
        return new ArrayList<>(colaSalida);
    }
}