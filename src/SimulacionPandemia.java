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

        for (int i = 0; i < 12; i++) {
            int x = 50 + (i % 6) * 180;
            int y = 150 + (i / 6) * 120;
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
                    clientesAtendidosTotal++;
                    tiempoEsperaTotal += caja.getClienteActual().getTiempoEspera();
                    caja.setClienteActual(null);
                }
            }
        }

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
                    .append(" | Ocioso: ").append(caja.getTiempoOcioso()).append(" min")
                    .append(" | Tiempo abierta: ").append(caja.getTiempoAbierta()).append(" min\n");
        }

        return stats.toString();
    }

    public List<Cliente> getClientesTerminados() {
        List<Cliente> terminados = new ArrayList<>();
        // En modo pandemia, no hay una lista específica de clientes terminados
        // Podemos devolver una lista vacía o implementar lógica si es necesario
        return terminados;
    }
}