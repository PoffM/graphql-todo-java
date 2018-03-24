package ca.poffm.graphql.todo.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ca.poffm.graphql.todo.input.InputConverter;
import ca.poffm.graphql.todo.input.TaskInput;
import ca.poffm.graphql.todo.model.Task;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;

/**
 * This GraphQL API's mutation root.
 * 
 * @author PoffM
 */
@Component
@Transactional
public class MutationRoot {
  
  @Autowired private InputConverter converter;
  @PersistenceContext private EntityManager em;
  
  @GraphQLMutation
  public Task createTask(
      @GraphQLNonNull @GraphQLArgument(name="task") TaskInput task
      ) {
    Task entity = converter.convert(task, Task.class);
    this.em.persist(entity);
    return entity;
  }
  
  @GraphQLMutation
  public Task updateTask(
      @GraphQLNonNull @GraphQLArgument(name="id") Long id,
      @GraphQLNonNull @GraphQLArgument(name="patch") TaskInput patch
      ) {
    Task task = this.em.find(Task.class, id);
    converter.patch(patch, task);
    return task;
  }
  
  @GraphQLMutation
  public Long deleteTask(
    @GraphQLNonNull @GraphQLArgument(name="id") Long id
  ) {
    this.em.remove(this.em.getReference(Task.class, id));
    return id;
  }
  
  
}
