import java.util.ArrayList;
import java.util.List;

public class SimulacionNormal extends SimulacionBase {
    private int siguienteIdCliente;
    private int proximaLlegada;
    private List<Cliente> clientesEnSistema;

    public SimulacionNormal() {
        super();
        this.siguienteIdCliente = 1;
        this.proximaLlegada = generarTiempoEntreLlegadas();
        this.clientesEnSistema = new ArrayList<>();

        // Configurar 12 cajas en 2 filas de 6 cajas cada una
        for (int i = 0; i < 12; i++) {
            int x = 50 + (i % 6) * 180;  // 6 cajas por fila
            int y = 150 + (i / 6) * 120;  // 2 filas
            cajas.add(new Caja(i + 1, x, y));
        }

        // Abrir 2 cajas inicialmente (como en el código original)
        for (int i = 0; i < 2; i++) {
            cajas.get(i).setAbierta(true);
        }
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

        // Generar clientes continuamente
        if (tiempoActual >= proximaLlegada && siguienteIdCliente <= 1000) {
            Cliente nuevoCliente = new Cliente(siguienteIdCliente++, tiempoActual);
            clientesEnSistema.add(nuevoCliente);
            asignarClienteACaja(nuevoCliente);
            proximaLlegada = tiempoActual + generarTiempoEntreLlegadas();
        }

        for (Caja caja : cajas) {
            if (caja.isAbierta()) {
                caja.actualizar();
                if (caja.getClienteActual() != null &&
                        caja.getClienteActual().getEstado() == Cliente.EstadoCliente.TERMINADO) {
                    clientesAtendidosTotal++;
                    tiempoEsperaTotal += caja.getClienteActual().getTiempoEspera();
                    clientesEnSistema.remove(caja.getClienteActual());
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
            if (caja.isAbierta() && (caja.getClienteActual() != null || !caja.getCola().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    public List<Cliente> getClientesEnSistema() { return clientesEnSistema; }
    public int getProximaLlegada() { return proximaLlegada; }
}