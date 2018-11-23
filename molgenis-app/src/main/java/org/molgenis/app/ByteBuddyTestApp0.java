package org.molgenis.app;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBuddyTestApp0 {
  private static final Logger LOGGER = LoggerFactory.getLogger(ByteBuddyTestApp0.class);

  public static void main(String[] args) throws InstantiationException, IllegalAccessException {
    new ByteBuddyTestApp0();
  }

  private ByteBuddyTestApp0() throws IllegalAccessException, InstantiationException {

    Class<? extends MyEntity> proxyClass =
        new ByteBuddy()
            .subclass(MyEntity.class)
            .method(ElementMatchers.any())
            .intercept(MethodDelegation.to(LoggingInterceptor.class))
            .make()
            .load(MyEntity.class.getClassLoader(), Default.WRAPPER)
            .getLoaded();

    MyEntity interceptedEntity0 = proxyClass.newInstance();
    interceptedEntity0.getId();
    interceptedEntity0.getId();

    MyEntity interceptedEntity1 = proxyClass.newInstance();
    interceptedEntity1.getId();
    interceptedEntity1.getId();
  }

  public static class LoggingInterceptor {
    private static AtomicInteger counter = new AtomicInteger();

    @RuntimeType
    public static Object intercept(@SuperCall Callable<?> superMethod, @Origin Method method)
        throws Exception {
      LOGGER.info("Call to '{}' intercepted ({})", method.getName(), counter.getAndIncrement());
      return superMethod.call();
    }
  }

  public static class MyEntity {
    private String id;

    public String getId() {
      LOGGER.info("Calling MyEntity.getId");
      return id;
    }

    public void setId(String id) {
      LOGGER.info("Calling MyEntity.setId");
      this.id = id;
    }
  }
}
