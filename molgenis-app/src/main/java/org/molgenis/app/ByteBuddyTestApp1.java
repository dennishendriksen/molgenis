package org.molgenis.app;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBuddyTestApp1 {
  private static final Logger LOGGER = LoggerFactory.getLogger(ByteBuddyTestApp1.class);

  private static final String LAZY_LOADED = "_lazyLoaded";

  public static void main(String[] args) throws InstantiationException, IllegalAccessException {
    new ByteBuddyTestApp1();
  }

  private ByteBuddyTestApp1() throws IllegalAccessException, InstantiationException {

    Class<? extends MyEntity> proxyClass =
        new ByteBuddy()
            .subclass(MyEntity.class)
            .defineField(LAZY_LOADED, boolean.class)
            .method(not(isDeclaredBy(Object.class)).and(not(named("setId"))))
            .intercept(
                MethodDelegation.withDefaultConfiguration()
                    .withBinders(FieldProxy.Binder.install(Get.class, Set.class))
                    .to(LazyLoadingInterceptor.class))
            .make()
            .load(MyEntity.class.getClassLoader(), Default.WRAPPER)
            .getLoaded();

    LOGGER.info("*** MyEntity: create ");
    MyEntity interceptedEntity0 = proxyClass.newInstance();
    LOGGER.info("*** MyEntity: getId #1 ");
    interceptedEntity0.getId();
    LOGGER.info("*** MyEntity: getId #2 ");
    interceptedEntity0.getId();
  }

  public interface Get<T> {
    T get();
  }

  public interface Set<T> {
    void set(T value);
  }

  public static class LazyLoadingInterceptor {
    @RuntimeType
    public static Object intercept(
        @This MyEntity entity,
        @SuperCall Callable<?> superMethod,
        @Origin Method method,
        // TODO FieldProxy: Allows to access fields via getter and setter proxies. This annotation needs to be installed and explicitly registered before it can be used. Note that any field access requires boxing such that a use of FieldAccessor in combination with andThen(Implementation) might be a more performant alternative for implementing field getters and setters.
        @FieldProxy(LAZY_LOADED) Get<Boolean> lazyLoadedGetter,
        @FieldProxy(LAZY_LOADED) Set<Boolean> lazyLoadedSetter)
        throws Exception {
      LOGGER.info("Call to '{}' intercepted ({})", method.getName());
      if (!lazyLoadedGetter.get()) {
        /**
         *
         * TODO do something like this
         * public List<String> log(@Pipe Forwarder<List<String>, MemoryDatabase> pipe) {
         * println("Calling database"); try { return pipe.to(memoryDatabase); } finally {
         * println("Returned from database"); } }
         */
        LOGGER.info("Lazy loading ...");
        entity.setId("MyId");
        lazyLoadedSetter.set(true);
        LOGGER.info("Lazy loading completed");
      } else {
        LOGGER.info("Lazy loading not needed");
      }
      return superMethod.call();
    }
  }

  public static class MyEntity {
    private String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }
}
