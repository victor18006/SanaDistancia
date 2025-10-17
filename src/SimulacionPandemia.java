import java.util.LinkedList;
import java.util.Queue;

public class SimulacionPandemia extends SimulacionBase {
    private Queue<Cliente> filaUnica;
    private int siguienteIdCliente;
    private int proximaLlegada;

    public SimulacionPandemia() {
        super();
        this.filaUnica = new LinkedList<>();
        this.siguienteIdCliente = 1;
        this.proximaLlegada = generarTiempoEntreLlegadas();

        for (int i = 0; i < 12; i++) {
            int x = 50 + (i % 6) * 180;
            int y = 150 + (i / 6) * 120;
            cajas.add(new Caja(i + 1, x, y));
        }

        for (int i = 0; i < 3; i++) {
            cajas.get(i).setAbierta(true);
        }
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

        // Generar clientes
        if (tiempoActual >= proximaLlegada && siguienteIdCliente <= 1000) {
            Cliente nuevoCliente = new Cliente(siguienteIdCliente++, tiempoActual);
            filaUnica.add(nuevoCliente);
            proximaLlegada = tiempoActual + generarTiempoEntreLlegadas();
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
            // ✅ MODO PANDEMIA: Máximo 1 cliente en cola + 1 siendo atendido
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
    public int getProximaLlegada() { return proximaLlegada; }
}