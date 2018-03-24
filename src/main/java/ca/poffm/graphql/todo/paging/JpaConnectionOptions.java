package ca.poffm.graphql.todo.paging;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Additional optional arguments to be passed when creating a JpaConnection.
 * 
 * @author PoffM
 */
@Builder
@Data
public class JpaConnectionOptions {
  
  /**
   * TriConsumer interface for enhancing the connection's JPA query.
   * 
   * @author PoffM
   */
  @FunctionalInterface
  public static interface QueryConfig {
    void accept(CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Root<?> root);
  }
  
  /**
   * Function to run before the JPA query executes. Add filters, sorting, etc.
   */
  @NonNull
  @Builder.Default
  private QueryConfig queryConfig = (query, cb, root) -> {};
  
}
