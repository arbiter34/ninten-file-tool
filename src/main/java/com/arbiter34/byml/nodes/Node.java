package com.arbiter34.byml.nodes;

public interface Node<T> {
    /**
     * Get node type short hex
     * @return
     */
    short getNodeType();

    /**
     * Check if node's value matches supplied
     * @param t
     * @return
     */
    boolean eq(T t);

    /**
     * Attempt to set value
     * @param t
     */
    void setValue(T t);

    /**
     * Get value
     * @return
     */
    T getValue();
}
