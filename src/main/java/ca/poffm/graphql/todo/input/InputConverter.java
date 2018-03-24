package ca.poffm.graphql.todo.input;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import lombok.extern.java.Log;

/**
 * Converts an input object to a POJO object. Input objects are expected to have
 * all field types as Optionals, where every value is initialized as
 * Optional.empty(), because GraphQL SPQR serializes null as null and ignores
 * undefined values.
 * 
 * TODO rewrite with less code, less reflection. Use some other library like MapStruct
 * 
 * @author PoffM
 */
@Log
@Component
public class InputConverter {

  /**
   * Converts from the input object to a new object of the target class.
   * 
   * @param source
   * @param targetClass
   * @return
   */
  public <S, D> D convert(S source, Class<D> targetClass) {
    D output;
    try {
      output = targetClass.newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException(targetClass.getName(), e);
    }

    this.patch(source, output);

    return output;
  }

  /**
   * Apply values from the source input object to the target object.
   * Optional values are resolved and applied.
   * null is applied as null.
   * Optional.empty() is not applied.
   * 
   * @param source
   * @param target
   */
  public <S, D> void patch(S source, D target) {
    
    // Loop through source object properties
    for (PropertyDescriptor sourcePd : BeanUtils.getPropertyDescriptors(source.getClass())) {
      Method readMethod = sourcePd.getReadMethod();

      PropertyDescriptor destPd =
          BeanUtils.getPropertyDescriptor(target.getClass(), sourcePd.getName());
      if (destPd == null) {
        throw new IllegalArgumentException(
            "No property descriptor on " + target.getClass().getName() + " for " + sourcePd.getName()
            );
      }
      
      Method writeMethod = destPd.getWriteMethod();
      if (writeMethod == null) continue;

      try {

        Object inputValue = readMethod.invoke(source);

        if (inputValue instanceof Optional && ((Optional<?>) inputValue).isPresent()) {
          writeMethod.invoke(target, ((Optional<?>) inputValue).get());
        } else if (inputValue == null) {
          writeMethod.invoke(target, inputValue);
        }

      } catch (ReflectiveOperationException e) {
        log.severe(Arrays.toString(e.getStackTrace()));
      }

    }
  }

}
