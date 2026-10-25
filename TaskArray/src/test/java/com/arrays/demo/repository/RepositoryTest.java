package com.arrays.demo.repository;

import com.arrays.demo.comparator.ArrayComparators;
import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.factory.NumberArrayFactory;
import com.arrays.demo.observer.ArrayObserver;
import com.arrays.demo.observer.Observer;
import com.arrays.demo.specification.ByIdSpec;
import com.arrays.demo.specification.ByMetricSpec;
import com.arrays.demo.specification.ByNameSpec;
import com.arrays.demo.specification.Comparison;
import com.arrays.demo.specification.Metric;
import com.arrays.demo.warehouse.Warehouse;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RepositoryTest {
  private final ArrayRepository repository = ArrayRepository.getInstance();
  private final Warehouse warehouse = Warehouse.getInstance();
  private final NumberArrayFactory factory = new NumberArrayFactory();

  @BeforeEach
  @AfterEach
  void clear() {
    repository.reset();
    // This ID is also used for statistics created directly without registration.
    warehouse.remove("a");
  }

  @Test
  void creationMutationAndRemovalMaintainStatistics() {
    NumberArray array = factory.create("a", "Numbers", new int[] {1, 2, 6});
    assertSame(array, repository.findById("a").orElseThrow());
    assertStats("a", 9, 3.0, 6, 1, 3);
    array.set(0, -2);
    assertStats("a", 6, 2.0, 6, -2, 3);
    repository.remove("a");
    array.set(0, 100);
    assertTrue(repository.findById("a").isEmpty());
    assertTrue(warehouse.getStats("a").isEmpty());
  }

  @Test
  void duplicateIdsPreserveObjectStatisticsAndSubscription() {
    NumberArray array = factory.create("a", "original", new int[] {1});
    Warehouse.Statistics stats = warehouse.getStats("a").orElseThrow();
    NumberArray duplicate = new NumberArray("a", "duplicate", new int[] {99});
    assertThrows(IllegalArgumentException.class, () -> repository.add(array));
    assertThrows(IllegalArgumentException.class, () -> repository.add(duplicate));
    assertThrows(IllegalArgumentException.class,
        () -> factory.create("a", "duplicate", new int[] {99}));
    assertSame(array, repository.findById("a").orElseThrow());
    assertSame(stats, warehouse.getStats("a").orElseThrow());
    duplicate.set(0, 88);
    assertSame(stats, warehouse.getStats("a").orElseThrow());
    array.set(0, 2);
    assertEquals(2, warehouse.getStats("a").orElseThrow().getSum());
    repository.remove("a");
    array.set(0, 3);
    assertTrue(warehouse.getStats("a").isEmpty());
  }

  @Test
  void copiesAndObserverSubscriptionsAreDefensive() {
    int[] input = {1, 2};
    NumberArray array = factory.create("a", "n", input);
    input[0] = 99;
    array.snapshot()[0] = 88;
    assertArrayEquals(new int[] {1, 2}, array.snapshot());
    AtomicInteger calls = new AtomicInteger();
    Observer observer = changed -> calls.incrementAndGet();
    array.addObserver(observer);
    array.addObserver(observer);
    array.set(0, 1);
    assertEquals(0, calls.get());
    array.set(0, 3);
    assertEquals(1, calls.get());
    array.removeObserver(observer);
    array.removeObserver(observer);
    array.set(0, 4);
    assertEquals(1, calls.get());
  }

  @Test
  void rejectsInvalidArguments() {
    assertThrows(IllegalArgumentException.class, () -> factory.create(null, "n", new int[] {1}));
    assertThrows(IllegalArgumentException.class, () -> factory.create("a", null, new int[] {1}));
    assertThrows(IllegalArgumentException.class, () -> factory.create("a", "n", null));
    assertThrows(IllegalArgumentException.class, () -> factory.create("a", "n", new int[0]));
    assertTrue(repository.getAll().isEmpty());
    NumberArray array = factory.create("a", "n", new int[] {1});
    assertThrows(IndexOutOfBoundsException.class, () -> array.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> array.get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> array.set(-1, 2));
    assertThrows(IndexOutOfBoundsException.class, () -> array.set(1, 2));
    assertThrows(IllegalArgumentException.class, () -> array.addObserver(null));
    assertThrows(IllegalArgumentException.class, () -> array.removeObserver(null));
    assertThrows(IllegalArgumentException.class, () -> repository.add(null));
    assertThrows(IllegalArgumentException.class, () -> repository.remove(null));
    assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    assertThrows(IllegalArgumentException.class, () -> repository.find(null));
    assertThrows(IllegalArgumentException.class, () -> repository.sort(null));
    assertThrows(IllegalArgumentException.class, () -> warehouse.recalculate(null));
    assertThrows(IllegalArgumentException.class, () -> warehouse.remove(null));
    assertThrows(IllegalArgumentException.class, () -> warehouse.getStats(null));
    assertThrows(IllegalArgumentException.class, () -> new ArrayObserver(null));
    assertThrows(IllegalArgumentException.class, () -> new ArrayObserver(warehouse).update(null));
    assertThrows(IllegalArgumentException.class, () -> new ByIdSpec(null));
    assertThrows(IllegalArgumentException.class, () -> new ByNameSpec(null));
    assertThrows(IllegalArgumentException.class, () -> new ByIdSpec("a").isSatisfiedBy(null));
    assertThrows(IllegalArgumentException.class, () -> new ByNameSpec("n").isSatisfiedBy(null));
    assertThrows(IllegalArgumentException.class,
        () -> new ByMetricSpec(Metric.SUM, Comparison.EQUAL, 1L).isSatisfiedBy(null));
    for (Comparator<NumberArray> comparator : List.of(ArrayComparators.BY_ID,
        ArrayComparators.BY_NAME, ArrayComparators.BY_FIRST_ELEMENT, ArrayComparators.BY_COUNT)) {
      assertThrows(IllegalArgumentException.class, () -> comparator.compare(null, array));
      assertThrows(IllegalArgumentException.class, () -> comparator.compare(array, null));
    }
  }

  @Test
  void missingIdsAndUnmatchedSearchesAreEmpty() {
    repository.remove("missing");
    warehouse.remove("missing");
    assertTrue(repository.findById("missing").isEmpty());
    assertTrue(warehouse.getStats("missing").isEmpty());
    assertTrue(repository.find(new ByIdSpec("missing")).isEmpty());
    NumberArray array = factory.create("a", "Name", new int[] {1});
    assertEquals(List.of(array), repository.find(new ByIdSpec("a")));
    assertEquals(List.of(array), repository.find(new ByNameSpec("Name")));
    assertTrue(repository.find(new ByIdSpec("A")).isEmpty());
    assertTrue(repository.find(new ByNameSpec("name")).isEmpty());
  }

  @ParameterizedTest
  @EnumSource(Metric.class)
  void searchesEveryMetricWithEveryComparison(Metric metric) {
    NumberArray array = factory.create("a", "n", new int[] {1, 2, 6});
    long value = switch (metric) {
      case SUM -> 9;
      case AVG, COUNT -> 3;
      case MAX -> 6;
      case MIN -> 1;
    };
    for (Comparison comparison : Comparison.values()) {
      for (int offset = -1; offset <= 1; offset++) {
        long threshold = value + offset;
        ByMetricSpec spec = metric == Metric.AVG
            ? new ByMetricSpec(metric, comparison, (double) threshold)
            : new ByMetricSpec(metric, comparison, threshold);
        boolean matches = switch (comparison) {
          case EQUAL -> offset == 0;
          case GREATER -> offset < 0;
          case LESS -> offset > 0;
        };
        assertEquals(matches ? List.of(array) : List.of(), repository.find(spec));
      }
    }
  }

  @Test
  void validatesMetricThresholdTypesAndFiniteAverages() {
    assertThrows(IllegalArgumentException.class, () -> new ByMetricSpec(null, Comparison.EQUAL, 1L));
    assertThrows(IllegalArgumentException.class, () -> new ByMetricSpec(null, Comparison.EQUAL, 1.0));
    assertThrows(IllegalArgumentException.class, () -> new ByMetricSpec(Metric.SUM, null, 1L));
    assertThrows(IllegalArgumentException.class, () -> new ByMetricSpec(Metric.AVG, null, 1.0));
    assertThrows(IllegalArgumentException.class, () -> new ByMetricSpec(Metric.AVG, Comparison.EQUAL, 1L));
    for (Metric metric : List.of(Metric.SUM, Metric.MAX, Metric.MIN, Metric.COUNT)) {
      assertThrows(IllegalArgumentException.class, () -> new ByMetricSpec(metric, Comparison.EQUAL, 1.0));
    }
    for (double value : new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
      assertThrows(IllegalArgumentException.class, () -> new ByMetricSpec(Metric.AVG, Comparison.EQUAL, value));
    }
  }

  @Test
  void handlesOverflowFractionalAverageAndExactIntegralThresholds() {
    factory.create("large", "n", new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE});
    assertStats("large", 4294967294L, 2147483647.0, Integer.MAX_VALUE, Integer.MAX_VALUE, 2);
    NumberArray fractional = factory.create("fractional", "n", new int[] {1, 2});
    assertTrue(new ByMetricSpec(Metric.AVG, Comparison.EQUAL, 1.5).isSatisfiedBy(fractional));
    assertFalse(new ByMetricSpec(Metric.SUM, Comparison.EQUAL, Long.MAX_VALUE).isSatisfiedBy(fractional));
    assertTrue(new ByMetricSpec(Metric.SUM, Comparison.GREATER, Long.MIN_VALUE).isSatisfiedBy(fractional));
    NumberArray zero = factory.create("zero", "n", new int[] {0});
    assertFalse(new ByMetricSpec(Metric.AVG, Comparison.EQUAL, -0.0).isSatisfiedBy(zero));
    factory.create("negative", "n", new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE});
    assertStats("negative", -4294967296L, -2147483648.0, Integer.MIN_VALUE, Integer.MIN_VALUE, 2);
  }

  @Test
  void metricSearchRequiresRegisteredIdentityAndExistingStatistics() {
    ByMetricSpec spec = new ByMetricSpec(Metric.SUM, Comparison.EQUAL, 1L);
    NumberArray detached = new NumberArray("a", "n", new int[] {1});
    warehouse.recalculate(detached);
    assertFalse(spec.isSatisfiedBy(detached));
    NumberArray old = factory.create("a", "n", new int[] {1});
    repository.remove("a");
    NumberArray current = factory.create("a", "n", new int[] {1});
    assertFalse(spec.isSatisfiedBy(old));
    assertFalse(spec.isSatisfiedBy(detached));
    old.set(0, 9);
    assertTrue(spec.isSatisfiedBy(current));
    warehouse.remove("a");
    assertFalse(spec.isSatisfiedBy(current));
  }

  @Test
  void sortingAndListCopiesPreserveStorage() {
    NumberArray a = factory.create("a", "z", new int[] {9});
    NumberArray b = factory.create("b", "a", new int[] {1, 2});
    NumberArray c = factory.create("c", "m", new int[] {4, 5, 6});
    assertEquals(List.of(a, b, c), repository.sort(ArrayComparators.BY_ID));
    assertEquals(List.of(b, c, a), repository.sort(ArrayComparators.BY_NAME));
    assertEquals(List.of(b, c, a), repository.sort(ArrayComparators.BY_FIRST_ELEMENT));
    assertEquals(List.of(a, b, c), repository.sort(ArrayComparators.BY_COUNT));
    repository.getAll().clear();
    repository.find(item -> true).clear();
    repository.sort(ArrayComparators.BY_ID).clear();
    assertEquals(3, repository.getAll().size());
    NumberArray equal = new NumberArray("a", "z", new int[] {9});
    for (Comparator<NumberArray> comparator : List.of(ArrayComparators.BY_ID,
        ArrayComparators.BY_NAME, ArrayComparators.BY_FIRST_ELEMENT, ArrayComparators.BY_COUNT)) {
      assertEquals(0, comparator.compare(a, equal));
    }
  }

  @Test
  void resetUnsubscribesAndSingletonsHaveExpectedStructure() throws Exception {
    NumberArray array = factory.create("a", "n", new int[] {1});
    repository.reset();
    array.set(0, 2);
    assertTrue(repository.getAll().isEmpty());
    assertTrue(warehouse.getStats("a").isEmpty());
    assertSame(repository, ArrayRepository.getInstance());
    assertSame(warehouse, Warehouse.getInstance());
    assertFalse(Observer.class.isAssignableFrom(Warehouse.class));
    int modifiers = NumberArray.class.getDeclaredConstructor(String.class, String.class, int[].class).getModifiers();
    assertTrue(Modifier.isPublic(modifiers));
    for (Class<?> type : List.of(Warehouse.class, ArrayRepository.class)) {
      for (var method : type.getDeclaredMethods()) {
        assertFalse(Modifier.isSynchronized(method.getModifiers()));
      }
    }
  }

  private void assertStats(String id, long sum, double avg, int max, int min, int count) {
    Warehouse.Statistics stats = warehouse.getStats(id).orElseThrow();
    assertEquals(sum, stats.getSum());
    assertEquals(avg, stats.getAvg());
    assertEquals(max, stats.getMax());
    assertEquals(min, stats.getMin());
    assertEquals(count, stats.getCount());
  }
}
