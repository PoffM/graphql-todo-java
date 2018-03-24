package ca.poffm.graphql.todo.paging;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.ObjectUtils;

import ca.poffm.graphql.todo.model.JpaNode;
import graphql.relay.ConnectionCursor;
import graphql.relay.DefaultEdge;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import io.leangen.graphql.execution.relay.Connection;
import lombok.NonNull;

/**
 * JPA implementation of GraphQL relay connection.
 * 
 * @author PoffM
 *
 * @param <T> The node type.
 * @param <E> The edge type.
 */
public class JpaConnection<T extends JpaNode, E extends Edge<T>> implements Connection<E> {

  protected final Class<T> resultClass;
  protected final EntityManager entityManager;
  
  protected final CriteriaBuilder cb;
  
  protected final ConnectionArgs connectionArgs;
  protected final JpaConnectionOptions connectionOptions;
  
  protected final List<T> results;

  /**
   * Create new connection.
   * 
   * @param resultClass Result JPA Entity class
   * @param entityManager JPA Entity Manager
   * @param connectionArgs Connection arguments
   */
  public JpaConnection(
      @NonNull Class<T> resultClass,
      @NonNull EntityManager entityManager,
      @NonNull ConnectionArgs connectionArgs
  ) {
    this(resultClass, entityManager, connectionArgs, JpaConnectionOptions.builder().build());
  }
  
  /**
   * Create a new connection with extra options object.
   * 
   * @param resultClass Result JPA Entity class
   * @param entityManager JPA Entity Manager
   * @param connectionArgs Connection arguments
   * @param connectionOptions Optional configuration
   */
  public JpaConnection(
      @NonNull Class<T> resultClass,
      @NonNull EntityManager entityManager,
      @NonNull ConnectionArgs connectionArgs,
      @NonNull JpaConnectionOptions connectionOptions
  ) {
    
    this.resultClass = resultClass;
    this.entityManager = entityManager;
    this.connectionArgs = connectionArgs;
    this.connectionOptions = connectionOptions;
    
    // Init JPA Criteria objects.
    this.cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<T> nodeQuery = this.cb.createQuery(resultClass);
    Root<T> root = nodeQuery.from(resultClass);
    Path<Number> id = root.get("id");
    
    // Set default order
    nodeQuery.orderBy(this.cb.asc(id));
    
    // Apply custom query config
    connectionOptions.getQueryConfig().accept(nodeQuery, this.cb, root);
    
    // Add the restriction for "after" and "before" args.
    Predicate restriction = nodeQuery.getRestriction();
    if (connectionArgs.getAfter() != null) {
      restriction = this.cb.and(restriction, this.cb.gt(id, JpaCursor.decodeId(connectionArgs.getAfter())));
    }
    if (connectionArgs.getBefore() != null) {
      restriction = this.cb.and(restriction, this.cb.lt(id, JpaCursor.decodeId(connectionArgs.getBefore())));
    }
    nodeQuery.where(restriction);
    
    // If "last" arg is set, reverse the sort order so the query starts by getting the
    // results at the back.
    if (connectionArgs.getFirst() == null && connectionArgs.getLast() != null) {
      nodeQuery.orderBy(
          nodeQuery.getOrderList()
          .stream()
          .map(Order::reverse)
          .collect(Collectors.toList())
      );
    }
    
    // Get the result list from the database.
    this.results = this.entityManager
        .createQuery(nodeQuery)
        .setMaxResults(ObjectUtils.firstNonNull(
            connectionArgs.getFirst(), connectionArgs.getLast(), 10)
            )
        .getResultList();
    
    //Reverse the order back to normal if last was set.
    if (connectionArgs.getFirst() == null && connectionArgs.getLast() != null) {
      Collections.reverse(this.results);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<E> getEdges() {
    return (List<E>) this.results
        .stream()
        .map(o -> new DefaultEdge<>(o, new JpaCursor(o)))
        .collect(Collectors.toList());
  }

  @Override
  public PageInfo getPageInfo() {
    JpaConnection<T, E> outer = JpaConnection.this;
    
    return new PageInfo() {

      @Override
      public ConnectionCursor getStartCursor() {
        return !outer.results.isEmpty()
            ? new JpaCursor(outer.results.get(0))
            : null;
      }

      @Override
      public ConnectionCursor getEndCursor() {
        return !outer.results.isEmpty()
            ? new JpaCursor(outer.results.get(outer.results.size() - 1))
            : null;
      }

      @Override
      public boolean isHasPreviousPage() {
        if (outer.connectionArgs.getAfter() != null) {
          return true;
        }
        if (outer.results.isEmpty()) {
          return false;
        }
        
        CriteriaQuery<T> hasPreviousPageQuery = outer.cb.createQuery(outer.resultClass);
        
        Root<T> prevRoot = hasPreviousPageQuery.from(outer.resultClass);
        
        hasPreviousPageQuery.where(
            outer.cb.lt(
                prevRoot.get("id"),
                outer.results.get(0).getId())
            );
        
        return !outer.entityManager.createQuery(hasPreviousPageQuery)
            .setMaxResults(1)
            .getResultList()
            .isEmpty();
      }

      @Override
      public boolean isHasNextPage() {
        if (outer.connectionArgs.getBefore() != null) {
          return true;
        }
        if (outer.results.isEmpty()) {
          return false;
        }
        
        CriteriaQuery<T> hasNextPageQuery = outer.cb.createQuery(outer.resultClass);
        
        Root<T> nextRoot = hasNextPageQuery.from(outer.resultClass);
        
        hasNextPageQuery.where(
            outer.cb.gt(
                nextRoot.get("id"),
                outer.results.get(outer.results.size() - 1).getId()
                )
            );
        
        return !outer.entityManager.createQuery(hasNextPageQuery)
            .setMaxResults(1)
            .getResultList()
            .isEmpty();
      }
      
    };
  }

}
