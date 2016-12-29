package adamsro;

/**
 * Created by adamsro on 12/19/16.
 */
public interface MultiKey<K1, K2> extends Comparable {
    K1 getKey1();
    void setKey1(K1 key1);
    K2 getKey2();
    void setKey2(K2 key2);
}
