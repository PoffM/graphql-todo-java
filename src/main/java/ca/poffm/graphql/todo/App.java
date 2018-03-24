package ca.poffm.graphql.todo;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ca.poffm.graphql.todo.service.MutationRoot;
import ca.poffm.graphql.todo.service.QueryRoot;
import graphql.ExecutionInput;
import graphql.GraphQL;
import io.leangen.graphql.GraphQLRuntime;
import io.leangen.graphql.GraphQLSchemaGenerator;

/**
 * GraphQL to-do list API.
 * 
 * @author PoffM
 */
@SpringBootApplication
@RestController
public class App {

  private final GraphQL graphQL;

  /**
   * Main method.
   * 
   * @param args launch arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
  
  @Autowired
  public App(
      QueryRoot queryRoot,
      MutationRoot mutationRoot
      ) {
    
    graphQL = GraphQLRuntime.newGraphQL(
        new GraphQLSchemaGenerator()
            .withOperationsFromSingleton(queryRoot)
            .withOperationsFromSingleton(mutationRoot, MutationRoot.class)
            .withRelayCompliantMutations()
            .generate()
        ).build();
  }
  
  @PostMapping(
      value = "/graphql", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE
      )
  @ResponseBody
  public Map<String, Object> indexFromAnnotated(@RequestBody Map<String, String> request, HttpServletRequest raw) {
    return graphQL.execute(
        ExecutionInput.newExecutionInput()
            .query(request.get("query"))
            .operationName(request.get("operationName"))
            .context(raw)
            .build()
        )
        .toSpecification();
  }

}
