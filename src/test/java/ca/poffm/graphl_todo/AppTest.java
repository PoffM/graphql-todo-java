package ca.poffm.graphl_todo;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ca.poffm.graphql.todo.App;
import ca.poffm.graphql.todo.input.TaskInput;
import ca.poffm.graphql.todo.model.Task;
import ca.poffm.graphql.todo.paging.ConnectionWithTotal;
import ca.poffm.graphql.todo.service.MutationRoot;
import ca.poffm.graphql.todo.service.QueryRoot;
import graphql.relay.Edge;
import junit.framework.TestCase;

/**
 * To-do list test.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class AppTest extends TestCase {
  
  @Autowired private QueryRoot query;
  @Autowired private MutationRoot mutation;
  
  private Task createTestTask() {
    TaskInput input = new TaskInput();
    input.setName(Optional.of("test task"));
    return mutation.createTask(input);
  }
  
  @Test
  public void createTask() {
    Task task = this.createTestTask();
    
    // Check values.
    assertNotNull(task.getId());
    assertEquals("test task", task.getName());
  }
  
  @Test
  public void updateTask() {
    Task task = this.createTestTask();
    
    // Update task
    TaskInput patch = new TaskInput();
    patch.setName(Optional.of("updated name"));
    Task updatedTask = mutation.updateTask(task.getId(), patch);
    
    // Check values
    assertEquals("updated name", updatedTask.getName());
  }
  
  @Test
  public void deleteTask() {
    Task task = this.createTestTask();
    assertEquals(task.getId(), mutation.deleteTask(task.getId()));
  }
  
  @Test
  public void findTaskById() {
    Task createdTask = this.createTestTask();
    
    // Assert correct values
    Task queriedTask = query.task(createdTask.getId());
    assertEquals("test task", queriedTask.getName());
    assertEquals(createdTask.getId(), queriedTask.getId());
  }
  
  /**
   * Run connection query tests using the example data inserted by ExampleDataLoader.
   */
  @Test
  public void exampleTasksFirst15Query() {
    ConnectionWithTotal<Edge<Task>> first15Conn = query.exampleTasks(15, null, null, null);
    
    // Assert correct size
    assertEquals(15, first15Conn.getEdges().size());
    
    // Assert false hasPreviousPage
    assertFalse(first15Conn.getPageInfo().isHasPreviousPage());
    
    // Assert correct total count
    assertEquals(Long.valueOf(10000), first15Conn.getTotalCount());
    
    // Assert correct ids
    assertEquals(
        IntStream.range(1, 16).boxed().collect(Collectors.toList()),
        first15Conn
          .getEdges()
          .stream()
          .map(edge -> edge.getNode().getId().intValue())
          .collect(Collectors.toList())
    );
    
  }
  
  @Test
  public void exampleTasksNext15Query() {
    // Get first page
    ConnectionWithTotal<Edge<Task>> first15Conn = query.exampleTasks(15, null, null, null);
    
    // Assert true hasNextPage
    assertTrue(first15Conn.getPageInfo().isHasNextPage());
    
    // Get second page
    String endCursor = first15Conn.getPageInfo().getEndCursor().getValue();
    ConnectionWithTotal<Edge<Task>> next15Conn = query.exampleTasks(15, endCursor, null, null);
    
    // Assert correct size
    assertEquals(15, next15Conn.getEdges().size());
    
    // Assert correct total count
    assertEquals(Long.valueOf(10000), next15Conn.getTotalCount());
    
    // Assert correct ids
    assertEquals(
        IntStream.range(16, 31).boxed().collect(Collectors.toList()),
        next15Conn
          .getEdges()
          .stream()
          .map(edge -> edge.getNode().getId().intValue())
          .collect(Collectors.toList())
    );
    
  }
  
  @Test
  public void exampleTasksLast15Query() {
    ConnectionWithTotal<Edge<Task>> last15Conn = query.exampleTasks(null, null, 15, null);
    
    // Assert correct size
    assertEquals(15, last15Conn.getEdges().size());
    
    // Assert correct total count
    assertEquals(Long.valueOf(10000), last15Conn.getTotalCount());
    
    // Assert correct ids
    assertEquals(
        IntStream.range(9986, 10001).boxed().collect(Collectors.toList()),
        last15Conn
          .getEdges()
          .stream()
          .map(edge -> edge.getNode().getId().intValue())
          .collect(Collectors.toList())
    );
    
  }
  
  @Test
  public void exampleTasksPrevious15Query() {
    // Get last page
    ConnectionWithTotal<Edge<Task>> last15Conn = query.exampleTasks(null, null, 15, null);
    
    // Assert true hasPreviousPage
    assertTrue(last15Conn.getPageInfo().isHasPreviousPage());
    
    // Get next-to-last page
    String startCursor = last15Conn.getPageInfo().getStartCursor().getValue();
    ConnectionWithTotal<Edge<Task>> next15Conn = query.exampleTasks(null, null, 15, startCursor);
    
    // Assert correct size
    assertEquals(15, next15Conn.getEdges().size());
    
    // Assert correct total count
    assertEquals(Long.valueOf(10000), next15Conn.getTotalCount());
    
    // Assert correct ids
    assertEquals(
        IntStream.range(9971, 9986).boxed().collect(Collectors.toList()),
        next15Conn
          .getEdges()
          .stream()
          .map(edge -> edge.getNode().getId().intValue())
          .collect(Collectors.toList())
    );
    
  }
  
}
