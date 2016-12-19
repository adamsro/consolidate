package adamsro;

/**
 * Created by adamsro on 12/19/16.
 */
public interface MultiKey<K1, K2> extends Comparable {
    public K1 getKey1();
    public K2 getKey2();
}
