package ca.poffm.graphql.todo.paging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GraphQL relay connection arguments.
 * 
 * @author PoffM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionArgs {
  private Integer first;
  private String after;
  private Integer last;
  private String before;
}
