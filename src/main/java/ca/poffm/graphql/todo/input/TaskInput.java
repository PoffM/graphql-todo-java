package ca.poffm.graphql.todo.input;

import java.util.Date;
import java.util.Optional;

import lombok.Data;

/**
 * Model of the input object for Task mutations.
 * 
 * @author PoffM
 */
@Data
public class TaskInput {
  private Optional<String> name = Optional.empty();
  private Optional<String> description = Optional.empty();
  private Optional<Date> dueDate = Optional.empty();
  private Optional<Boolean> done = Optional.empty();
}
