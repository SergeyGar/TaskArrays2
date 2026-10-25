# Java array processing

Requires JDK 17 or newer and Maven 3.9 or newer. Run commands from the project root.

```shell
mvn clean verify
mvn exec:java
mvn exec:java -Dexec.args="data/arrays.txt"
```

The default input is `data/arrays.txt`. Custom input paths must be relative.
UTF-8 lines contain 32-bit signed integers separated by commas, semicolons,
whitespace, or hyphens. For example:

```text
1, 2, 3
1 - 2 - 3
3 4 7
-5, 0, -2
```

A hyphen between values acts as a delimiter; a minus at the start of a token
acts as its sign. Use commas or semicolons for unambiguous negative values.
Empty lines are valid empty definitions for the parser and are skipped by the launcher.
Malformed and out-of-range lines are logged at WARN and skipped.
Unreadable files are logged at ERROR and cause an unsuccessful exit.

The registered array API is organized under `com.arrays.demo` into `entity`,
`factory`, `repository`, `warehouse`, `observer`, `specification`, `comparator`,
and `util` packages. `NumberArray` has a public constructor for direct creation;
use `NumberArrayFactory` when the array should be registered automatically:

```java
import com.arrays.demo.entity.NumberArray;
import com.arrays.demo.factory.NumberArrayFactory;
import com.arrays.demo.repository.ArrayRepository;
import com.arrays.demo.warehouse.Warehouse;
import com.arrays.demo.specification.ByMetricSpec;
import com.arrays.demo.specification.Metric;
import com.arrays.demo.specification.Comparison;
import com.arrays.demo.comparator.ArrayComparators;
import java.util.List;

NumberArray array = new NumberArrayFactory().create("sample", "Example", new int[] {1, 2, 6});
ArrayRepository repository = ArrayRepository.getInstance();
Warehouse warehouse = Warehouse.getInstance();
array.set(0, 4); // The observer updates warehouse statistics automatically.
long sum = warehouse.getStats("sample").orElseThrow().getSum();
List<NumberArray> matches = repository.find(
    new ByMetricSpec(Metric.AVG, Comparison.GREATER, 2.0));
List<NumberArray> sorted = repository.sort(ArrayComparators.BY_NAME);
repository.remove("sample"); // Unsubscribes the observer and removes statistics.
```

IDs and names are immutable. Input arrays, snapshots, and repository result lists
are defensively copied. Duplicate IDs and null arguments are rejected with
`IllegalArgumentException`; invalid element indices throw `IndexOutOfBoundsException`.
Statistics use `long` sums, `double` averages, and `int` extrema and counts.
Integral metric specifications require a `long` threshold; AVG requires a finite
`double` threshold. Metric searches require the currently registered object and
existing statistics. `ByIdSpec` and `ByNameSpec` match case-sensitive exact strings.
`ArrayComparators` supplies ascending ordering by ID, name, first element, and count.

The repository and warehouse are lazily initialized singletons for single-threaded
use. `ArrayObserver` bridges array changes to the warehouse. The launcher registers
each input array through the new factory with a generated ID and a line-based name.
JUnit parallel execution is disabled in Maven; lifecycle tests reset singleton
state and unsubscribe arrays between tests.

The original `IntegerArrayEntity`, `ArrayFactory`, `IntegerArrayBuilder`, calculation,
and sorting APIs remain available. These legacy entities support empty arrays,
whose statistics return empty optionals. Sums use `OptionalLong` to avoid integer
overflow. Bubble sort and insertion sort implement `SortService` and return new
legacy entities without mutating their inputs.

Source packages separate entities, factory, builder, services, validator,
reader, parser, exception, and launcher. Every service implementation has
an interface in the service package.

Log4J2 configuration lives in `config/log4j2.xml`; Maven copies it to the runtime
classpath. Logging writes to STDOUT and `logs/app.log`.
Data files live in `data/`, and test fixtures live in `test-data/`.
Maven also copies test fixtures to the test classpath. Tests use JUnit 5.

To publish the implementation to the configured Git remote:

```shell
git push origin HEAD
```
