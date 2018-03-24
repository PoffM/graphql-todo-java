package ca.poffm.graphql.todo.paging;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import ca.poffm.graphql.todo.model.JpaNode;
import graphql.relay.Edge;

/**
 * JPA implementation of GraphQL relay connection with additional totalCount field.
 * 
 * @author PoffM
 *
 * @param <T> The node type.
 * @param <E> The edge type.
 */
public class JpaConnectionWithTotal<T extends JpaNode, E extends Edge<T>>
    extends JpaConnection<T, E> implements ConnectionWithTotal<E> {
  
  public JpaConnectionWithTotal(
      Class<T> resultClass,
      EntityManager em,
      ConnectionArgs connectionArgs
      ) {
    super(resultClass, em, connectionArgs);
  }
  
  public JpaConnectionWithTotal(
      Class<T> resultClass,
      EntityManager em,
      ConnectionArgs connectionArgs,
      JpaConnectionOptions jpaPagingOptions
      ) {
    super(resultClass, em, connectionArgs, jpaPagingOptions);
  }
  
  @Override
  public Long getTotalCount() {
    CriteriaQuery<Long> query = this.cb.createQuery(Long.class);
    Root<T> root = query.from(this.resultClass);
    this.connectionOptions.getQueryConfig().accept(query, this.cb, root);
    query.select(cb.count(root));
    return this.entityManager.createQuery(query).getSingleResult();
  }
  
}
