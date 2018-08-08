package cn.fxlcy.util;

/**
 * Created by fxlcy on 18-8-8.
 */

public class Lazy<T> {
    private T obj;
    private Getter<T> getter;

    public Lazy(Getter<T> getter) {
        this.getter = getter;
    }

    public T get() {
        if (obj == null) {
            try {
                obj = getter.get();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return obj;
    }

    public T get(Object... args) {
        if (args.length == 0) {
            return get();
        } else {
            try {
                obj = getter.get(args);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return obj;
    }

    public interface Getter<T> {
        T get() throws Throwable;

        T get(Object... args) throws Throwable;
    }

    public static abstract class SimpleGetter<T> implements Getter<T> {
        @Override
        public T get() throws Throwable {
            throw new UnsupportedOperationException("get");
        }

        @Override
        public T get(Object... args) {
            throw new UnsupportedOperationException("get");
        }
    }
}
