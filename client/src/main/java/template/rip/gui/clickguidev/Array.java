package template.rip.gui.clickguidev;

import java.util.Arrays;

/**
 * A List like array with better performance and less memory usage.
 * However, JVM limitations makes this more difficult to use.
 *
 * @author deadLORD135
 */
public class Array<T> {

    private final T[] array;

    @SuppressWarnings("unchecked")
    public Array(Class<T> clazz, int size) {
        array = (T[]) java.lang.reflect.Array.newInstance(clazz, size);
    }

    public void add(T value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                array[i] = value;
                return;
            }
        }
    }

    public void add(int index, T value) {
        for (int i = array.length - 1; i > index; i--) {
            array[i] = array[i - 1];
        }
        array[index] = value;
    }

    public void set(int index, T value) {
        array[index] = value;
    }

    public T get(int index) {
        return array[index];
    }

    public void remove(T value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                array[i] = null;
                for (int j = i; j < array.length - 1; j++) {
                    array[j] = array[j + 1];
                }
                break;
            }
        }
    }

    public void reverse() {
        for (int i = 0; i < array.length / 2; i++) {
            T temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    public void clear() {
        Arrays.fill(array, null);
    }

    public int length() {
        return array.length;
    }

    public T[] toArray() {
        return array;
    }
}
