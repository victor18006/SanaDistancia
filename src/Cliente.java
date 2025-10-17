import java.awt.Color;
import java.util.Random;

public class Cliente {
    private int id;
    private int tiempoLlegada;
    private int tiempoServicio;
    private int tiempoEspera;
    private int tiempoInicioServicio;
    private int tiempoRestanteServicio;
    private Color color;
    private EstadoCliente estado;

    public enum EstadoCliente {
        ESPERANDO_FILA, ESPERANDO_CAJA, SIENDO_ATENDIDO, TERMINADO
    }

    public Cliente(int id, int tiempoActual) {
        this.id = id;
        this.tiempoLlegada = tiempoActual;
        this.tiempoServicio = generarTiempoServicio();
        this.tiempoRestanteServicio = tiempoServicio;
        this.tiempoEspera = 0;
        this.tiempoInicioServicio = -1;
        this.estado = EstadoCliente.ESPERANDO_FILA;
        this.color = generarColorAleatorio();
    }

    private int generarTiempoServicio() {
        Random rand = new Random();
        return rand.nextInt(3) + 3; // 3-5 minutos
    }

    private Color generarColorAleatorio() {
        Random rand = new Random();
        return new Color(rand.nextInt(200) + 55, rand.nextInt(200) + 55, rand.nextInt(200) + 55);
    }

    public void actualizar() {
        if (estado == EstadoCliente.SIENDO_ATENDIDO) {
            tiempoRestanteServicio--;
            if (tiempoRestanteServicio <= 0) {
                estado = EstadoCliente.TERMINADO;
            }
        }
    }

    public int getId() { return id; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getTiempoServicio() { return tiempoServicio; }
    public int getTiempoEspera() { return tiempoEspera; }
    public void setTiempoEspera(int tiempoEspera) { this.tiempoEspera = tiempoEspera; }
    public int getTiempoInicioServicio() { return tiempoInicioServicio; }
    public void setTiempoInicioServicio(int tiempoInicioServicio) {
        this.tiempoInicioServicio = tiempoInicioServicio;
    }
    public Color getColor() { return color; }
    public EstadoCliente getEstado() { return estado; }
    public void setEstado(EstadoCliente estado) { this.estado = estado; }
    public int getTiempoRestanteServicio() { return tiempoRestanteServicio; }

    @Override
    public String toString() {
        return "Cliente " + id;
    }
}