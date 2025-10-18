import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public abstract class SimulacionBase {
    protected List<Caja> cajas;
    protected Queue<Cliente> colaSalida; // Nueva cola para clientes en salida
    protected int tiempoSimulacion;
    protected int tiempoActual;
    protected int clientesAtendidosTotal;
    protected int tiempoEsperaTotal;
    protected Random random;
    protected boolean pausada;
    protected boolean terminada;

    // Control de tiempo entre llegadas con tiempo REAL
    protected int tiempoUltimaLlegada;
    protected int tiempoEntreLlegadas;

    public SimulacionBase() {
        this.cajas = new ArrayList<>();
        this.colaSalida = new LinkedList<>(); // Inicializar cola de salida
        this.tiempoSimulacion = 600;
        this.tiempoActual = 0;
        this.clientesAtendidosTotal = 0;
        this.tiempoEsperaTotal = 0;
        this.random = new Random();
        this.pausada = false;
        this.terminada = false;

        //Inicializar control de llegadas
        this.tiempoUltimaLlegada = 0;
        this.tiempoEntreLlegadas = generarTiempoEntreLlegadas();
    }

    public abstract void ejecutarPaso();

    protected int generarTiempoEntreLlegadas() {
        //0.5-1 minuto en tiempo REAL (no contador que se reduce)
        return random.nextInt(2) + 1; // 1-2 pasos = 0.5-1 minuto
    }

    //Método para verificar si debe llegar un cliente
    protected boolean debeLlegarCliente() {
        int tiempoDesdeUltimaLlegada = tiempoActual - tiempoUltimaLlegada;
        return tiempoDesdeUltimaLlegada >= tiempoEntreLlegadas;
    }

    //Método para programar próxima llegada
    protected void programarProximaLlegada() {
        tiempoUltimaLlegada = tiempoActual;
        tiempoEntreLlegadas = generarTiempoEntreLlegadas();
    }

    // Nuevo método para gestionar la cola de salida
    protected void actualizarColaSalida() {
        // Remover clientes que llevan más de 1 minuto en la salida
        Queue<Cliente> nuevaCola = new LinkedList<>();
        for (Cliente cliente : colaSalida) {
            int tiempoEnSalida = tiempoActual - cliente.getTiempoSalida();
            if (tiempoEnSalida < 1) { // Mantener en salida por 1 minuto
                nuevaCola.add(cliente);
            }
            // Los que tienen 1 minuto o más simplemente no se añaden (desaparecen)
        }
        colaSalida = nuevaCola;
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
                int maxPermitido = (this instanceof SimulacionPandemia) ? 4 : 4;
                if (caja.getTamanioCola() < maxPermitido) {
                    todasLlenas = false;
                }
            }
        }

        // Si todas las cajas abiertas están llenas y hay cajas cerradas disponibles
        if (todasLlenas && cajasAbiertas < cajas.size()) {
            // Crear lista de cajas cerradas disponibles
            List<Caja> cajasCerradas = new ArrayList<>();
            for (Caja caja : cajas) {
                if (!caja.isAbierta()) {
                    cajasCerradas.add(caja);
                }
            }

            // Seleccionar una caja aleatoria de las cerradas
            if (!cajasCerradas.isEmpty()) {
                Caja cajaAleatoria = cajasCerradas.get(random.nextInt(cajasCerradas.size()));
                cajaAleatoria.setAbierta(true);
                cajaAleatoria.setTiempoOcioso(0); // Reiniciar tiempo ocioso
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
    public Queue<Cliente> getColaSalida() { return colaSalida; } // Nuevo getter
    public int getClientesAtendidosTotal() { return clientesAtendidosTotal; }
    public int getTiempoEsperaTotal() { return tiempoEsperaTotal; }

    //Getters para el control de llegadas
    public int getTiempoUltimaLlegada() { return tiempoUltimaLlegada; }
    public int getTiempoEntreLlegadas() { return tiempoEntreLlegadas; }
}