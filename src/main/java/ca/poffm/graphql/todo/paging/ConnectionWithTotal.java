package ca.poffm.graphql.todo.paging;

import graphql.relay.Edge;
import io.leangen.graphql.execution.relay.Connection;

/**
 * GraphQL relay connection with additional totalCount field.
 * 
 * @author PoffM
 *
 * @param <E> The edge type.
 */
public interface ConnectionWithTotal<E extends Edge<?>> extends Connection<E> {
  public Long getTotalCount();
}
