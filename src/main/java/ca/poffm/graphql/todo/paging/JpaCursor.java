package ca.poffm.graphql.todo.paging;

import ca.poffm.graphql.todo.model.JpaNode;
import graphql.relay.ConnectionCursor;
import lombok.Getter;

public class JpaCursor implements ConnectionCursor {

  @Getter
  private String value;

  public JpaCursor(JpaNode node) {
    this.value = JpaCursor.encodeId(node.getId());
  }
  
  public static String encodeId(Long value) {
    return value.toString();
  }
  
  public static Long decodeId(String value) {
    return Long.valueOf(value);
  }

  @Override
  public String toString() {
    return value;
  }

}
