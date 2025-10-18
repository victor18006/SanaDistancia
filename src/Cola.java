import java.util.Iterator;

public class Cola<T> implements Iterable<T> {
    private static final int MAX = 1000; // Tamaño máximo por defecto
    private Object[] elementos;
    private int inicio;
    private int fin;
    private int tamaño;

    public Cola() {
        this.elementos = new Object[MAX];
        this.inicio = 0;
        this.fin = -1;
        this.tamaño = 0;
    }

    public Cola(int capacidad) {
        this.elementos = new Object[capacidad];
        this.inicio = 0;
        this.fin = -1;
        this.tamaño = 0;
    }

    // Insertar dato (equivalente a add/offer)
    public void insertar(T dato) {
        if (fin < elementos.length - 1) {
            fin = fin + 1;
            elementos[fin] = dato;
            tamaño++;

            if (fin == 0) {
                inicio = 0;
            }
        } else {
            // "Desbordamiento" como en el PDF
            System.out.println("Desbordamiento - Cola llena");
        }
    }

    // Eliminar primer elemento (equivalente a remove/poll)
    public T eliminarPrimero() {
        if (inicio <= fin && tamaño > 0) {
            @SuppressWarnings("unchecked")
            T dato = (T) elementos[inicio];

            if (inicio == fin) {
                inicio = 0;
                fin = -1;
            } else {
                inicio = inicio + 1;
            }
            tamaño--;
            return dato;
        } else {
            // "Subdesbordamiento" como en el PDF
            System.out.println("Subdesbordamiento - Cola vacía");
            return null;
        }
    }

    // Ver primer elemento sin eliminarlo (equivalente a element/peek)
    public T verPrimero() {
        if (inicio <= fin && tamaño > 0) {
            @SuppressWarnings("unchecked")
            T dato = (T) elementos[inicio];
            return dato;
        } else {
            System.out.println("Cola vacía");
            return null;
        }
    }

    // Métodos adicionales para compatibilidad
    public boolean add(T elemento) {
        if (fin < elementos.length - 1) {
            insertar(elemento);
            return true;
        }
        return false;
    }

    public boolean offer(T elemento) {
        return add(elemento);
    }

    public T remove() {
        return eliminarPrimero();
    }

    public T poll() {
        return eliminarPrimero();
    }

    public T element() {
        return verPrimero();
    }

    public T peek() {
        return verPrimero();
    }

    // Métodos utilitarios
    public int size() {
        return tamaño;
    }

    public boolean estaVacia() {
        return tamaño == 0;
    }

    public void clear() {
        inicio = 0;
        fin = -1;
        tamaño = 0;
    }

    // Implementar Iterable para poder usar foreach
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int currentIndex = inicio;

            @Override
            public boolean hasNext() {
                return currentIndex <= fin;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) {
                    return null;
                }
                T elemento = (T) elementos[currentIndex];
                currentIndex++;
                return elemento;
            }
        };
    }

    // Método para convertir a ArrayList (para compatibilidad)
    public java.util.ArrayList<T> toArrayList() {
        java.util.ArrayList<T> lista = new java.util.ArrayList<>();
        for (int i = inicio; i <= fin; i++) {
            @SuppressWarnings("unchecked")
            T elemento = (T) elementos[i];
            lista.add(elemento);
        }
        return lista;
    }

    @Override
    public String toString() {
        if (estaVacia()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = inicio; i <= fin; i++) {
            sb.append(elementos[i]);
            if (i < fin) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}