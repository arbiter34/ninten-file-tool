package com.arbiter34.byml.nodes;

import com.arbiter34.byml.serde.BymlNodeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = BymlNodeDeserializer.class)
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

    /**
     * Get size of node with all children
     * @return
     */
    default long getSize() {
        return 0;
    }

    default boolean contains(final Node node) {
        return false;
    }
}
