import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class SimulacionBase {
    protected List<Caja> cajas;
    protected int tiempoSimulacion;
    protected int tiempoActual;
    protected int clientesAtendidosTotal;
    protected int tiempoEsperaTotal;
    protected Random random;
    protected boolean pausada;
    protected boolean terminada;

    public SimulacionBase() {
        this.cajas = new ArrayList<>();
        this.tiempoSimulacion = 600;
        this.tiempoActual = 0;
        this.clientesAtendidosTotal = 0;
        this.tiempoEsperaTotal = 0;
        this.random = new Random();
        this.pausada = false;
        this.terminada = false;
    }

    public abstract void ejecutarPaso();

    protected int generarTiempoEntreLlegadas() {
        return random.nextInt(2) == 0 ? 1 : 2; // 0.5-1 minuto (1-2 pasos de simulación)
    }

    protected void abrirCajaSiEsNecesario() {
        int cajasAbiertas = 0;
        boolean todasLlenas = true;

        // Contar cajas abiertas y verificar si todas están llenas
        for (Caja caja : cajas) {
            if (caja.isAbierta()) {
                cajasAbiertas++;
                // En modo pandemia: máximo 2 clientes por caja (1 siendo atendido + 1 en espera)
                // En modo normal: máximo 4 clientes en cola
                int maxPermitido = (this instanceof SimulacionPandemia) ? 1 : 4;
                if (caja.getTamanioCola() < maxPermitido) {
                    todasLlenas = false;
                }
            }
        }

        // Si todas las cajas abiertas están llenas y hay cajas cerradas disponibles
        if (todasLlenas && cajasAbiertas < cajas.size()) {
            for (Caja caja : cajas) {
                if (!caja.isAbierta()) {
                    caja.setAbierta(true);
                    caja.setTiempoOcioso(0); // Reiniciar tiempo ocioso
                    break; // Abrir solo una caja a la vez
                }
            }
        }
    }

    protected void cerrarCajasOciosas() {
        for (Caja caja : cajas) {
            if (caja.isAbierta() && caja.necesitaCerrarse()) {
                caja.setAbierta(false);
            }
        }
    }

    public void pausar() { pausada = true; }
    public void reanudar() { pausada = false; }
    public boolean isPausada() { return pausada; }
    public boolean isTerminada() { return terminada; }
    public int getTiempoActual() { return tiempoActual; }
    public int getTiempoSimulacion() { return tiempoSimulacion; }
    public List<Caja> getCajas() { return cajas; }
    public int getClientesAtendidosTotal() { return clientesAtendidosTotal; }
    public int getTiempoEsperaTotal() { return tiempoEsperaTotal; }
}