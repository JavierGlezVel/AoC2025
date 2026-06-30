# Día 8

## Problema

El problema ocurre en un parque subterráneo. La entrada contiene posiciones de cajas
de conexión en un espacio tridimensional. Cada línea tiene coordenadas `X,Y,Z`:

```text
162,817,812
57,618,57
```

Los elfos conectan parejas de cajas empezando por las que están más cerca en
distancia recta. Cuando dos cajas se conectan, sus circuitos se unen. Si las dos cajas
ya estaban en el mismo circuito, esa conexión no cambia la red, pero sigue formando
parte de la lista de conexiones más cortas procesadas.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

El objetivo es procesar las 1000 parejas de cajas más cercanas y multiplicar los
tamaños de los tres circuitos más grandes que queden después.

Con el ejemplo oficial, tras procesar las 10 conexiones más cortas, los tres mayores
circuitos tienen tamaños `5`, `4` y `2`. El resultado es:

```text
40
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
244188
```

## Parte 2

Ahora hay que seguir conectando parejas, siempre en orden de cercania, hasta que
todas las cajas queden dentro de un único circuito. El resultado pedido es el
producto de las coordenadas `X` de las dos cajas de la conexión que consigue unirlo
todo.

Con el ejemplo oficial, la conexión final es entre `216,146,977` y `117,168,530`, y
el resultado es:

```text
25272
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
8361881885
```

## Enfoque de la solución

`ConnectionCandidateGenerator` genera todas las parejas posibles de cajas y calcula
su distancia al cuadrado:

```java
long distanceSquared = junctionBoxes.get(first).distanceSquaredTo(junctionBoxes.get(second));
```

No hace falta calcular la raíz cuadrada porque comparar distancias al cuadrado
mantiene el mismo orden y evita trabajar con números decimales.

Después ordena las parejas por distancia. Las dos partes reutilizan esa lista
ordenada, pero aplican reglas distintas:

- `CircuitSizeProductCalculatorPart1` procesa las primeras 1000 conexiones y
  multiplica los tres tamaños de circuito más grandes.
- `FinalConnectionXProductCalculatorPart2` procesa conexiones hasta que
  `CircuitNetwork` queda reducido a un único circuito y devuelve el producto de las
  coordenadas `X` de esa última conexión necesaria.

`CircuitNetwork` implementa unión-búsqueda. Esta estructura mantiene para cada caja
su representante de circuito, el tamaño de cada componente conectada y cuántos
circuitos quedan activos.

## Resolución detallada

### Parte 1

Cada caja de conexión tiene coordenadas tridimensionales. Primero se generan todas
las conexiones posibles entre pares de cajas y se ordenan por distancia euclídea al
cuadrado. No se necesita la raíz cuadrada, porque comparar distancias al cuadrado
mantiene el mismo orden y evita operaciones de coma flotante.

```java
for (int first = 0; first < junctionBoxes.size(); first++) {
    for (int second = first + 1; second < junctionBoxes.size(); second++) {
        long distanceSquared = junctionBoxes.get(first)
                .distanceSquaredTo(junctionBoxes.get(second));
        candidates.add(new ConnectionCandidate(first, second, distanceSquared));
    }
}

candidates.sort(Comparator.comparingLong(ConnectionCandidate::distanceSquared)
        .thenComparingInt(ConnectionCandidate::firstIndex)
        .thenComparingInt(ConnectionCandidate::secondIndex));
```

La parte 1 procesa las primeras 1000 conexiones ordenadas y une las cajas con una
estructura de unión-búsqueda (`CircuitNetwork`). Al final toma los tres circuitos
más grandes y multiplica sus tamaños:

```java
for (int i = 0; i < processedConnections; i++) {
    ConnectionCandidate candidate = candidates.get(i);
    network.connect(candidate.firstIndex(), candidate.secondIndex());
}

return network.largestCircuitSizes(3).stream()
        .mapToLong(Integer::longValue)
        .reduce(1L, (left, right) -> left * right);
```

La unión-búsqueda mantiene el representante de cada circuito y comprime caminos
para que las consultas posteriores sean más rápidas:

```java
private int find(int element) {
    if (parents[element] != element) {
        parents[element] = find(parents[element]);
    }
    return parents[element];
}
```

### Parte 2

La segunda parte procesa conexiones en el mismo orden, pero se detiene justo cuando
todas las cajas quedan conectadas en un único circuito. La respuesta se calcula con
las coordenadas `x` de las dos cajas de la conexión que provoca esa unión final.

```java
for (ConnectionCandidate candidate : candidates) {
    boolean connectedDifferentCircuits =
            network.connect(candidate.firstIndex(), candidate.secondIndex());

    if (connectedDifferentCircuits && network.isSingleCircuit()) {
        JunctionBox first = junctionBoxes.get(candidate.firstIndex());
        JunctionBox second = junctionBoxes.get(candidate.secondIndex());
        return first.x() * second.x();
    }
}
```

La llamada a `connect` devuelve `false` cuando la conexión une dos cajas que ya
pertenecían al mismo circuito. Ese detalle evita aceptar como conexión final una
arista redundante.

```java
public boolean connect(int first, int second) {
    int firstRoot = find(first);
    int secondRoot = find(second);

    if (firstRoot == secondRoot) {
        return false;
    }

    parents[secondRoot] = firstRoot;
    circuitCount--;
    return true;
}
```

## Uso de Streams

En este día los Streams aparecen en el parseo, en la selección de circuitos grandes
y en el producto final de la parte 1.

El parser convierte cada línea del input en una caja de conexión:

```java
return lines.stream()
        .map(this::parseLine)
        .toList();
```

El stream parte de las líneas del fichero. `map(this::parseLine)` transforma cada
línea textual en un `JunctionBox`, y `toList()` devuelve la lista de cajas ya
parseadas.

`CircuitNetwork` usa otro stream para devolver los circuitos más grandes:

```java
return circuitSizes.stream()
        .sorted(Comparator.reverseOrder())
        .limit(count)
        .toList();
```

Primero se han recogido los tamaños de todos los componentes conectados. El stream
los ordena de mayor a menor con `sorted(Comparator.reverseOrder())`, toma solo los
`count` primeros con `limit(count)` y materializa el resultado con `toList()`.

La parte 1 multiplica los tamaños de los tres circuitos mayores:

```java
return network.largestCircuitSizes(CIRCUITS_TO_MULTIPLY).stream()
        .mapToLong(Integer::longValue)
        .reduce(1L, (left, right) -> left * right);
```

El stream parte de una lista de `Integer`. `mapToLong` convierte cada tamaño a `long`
para evitar trabajar con envoltorios, y `reduce` acumula el producto empezando en
`1L`.

## Diseño de clases

La solución está dividida en tres paquetes principales:

```text
application/
domain/
  common/
  part1/
  part2/
infrastructure/
```

### `domain/common`

Contiene conceptos compartidos del problema.

- `JunctionBox`: representa una caja de conexión y calcula distancias al cuadrado.
- `ConnectionCandidate`: representa una posible conexión entre dos cajas.
- `ConnectionCandidateGenerator`: genera las posibles conexiones ordenadas por
  distancia.
- `CircuitNetwork`: representa la red de circuitos mediante unión-búsqueda.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `CircuitSizeProductCalculatorPart1`: procesa las conexiones más cortas y calcula
  el producto pedido.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `FinalConnectionXProductCalculatorPart2`: conecta cajas hasta que queda un único
  circuito y calcula el producto de las `X` de la conexión final.

### `application`

Coordina el caso de uso.

- `JunctionBoxParser`: transforma las líneas del fichero en cajas del dominio.
- `PlaygroundSolver`: lee la entrada, la parsea y delega el cálculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `JunctionBoxSource`: interfaz para obtener las líneas de entrada.
- `FileJunctionBoxSource`: implementación que lee las cajas desde un fichero.

## Clases principales

### `Main` - `dia8/src/main/java/Main.java`

1. Calcula la ruta del input.
2. Crea `FileJunctionBoxSource` y `PlaygroundSolver`.
3. Ejecuta las dos partes.

### `PlaygroundSolver` - `dia8/src/main/java/application/PlaygroundSolver.java`

1. Lee las cajas de conexión.
2. Las parsea con `JunctionBoxParser`.
3. Ejecuta la calculadora correspondiente a cada parte.

### `JunctionBoxParser` - `dia8/src/main/java/application/JunctionBoxParser.java`

1. Recorre las líneas del input.
2. Separa las tres coordenadas.
3. Crea objetos `JunctionBox`.

### `CircuitNetwork` - `dia8/src/main/java/domain/common/CircuitNetwork.java`

1. Mantiene componentes conectadas mediante unión-búsqueda.
2. Une circuitos con `connect`.
3. Calcula tamaños de los circuitos resultantes.

### `ConnectionCandidate` - `dia8/src/main/java/domain/common/ConnectionCandidate.java`

1. Representa una posible conexión entre dos cajas.
2. Guarda los índices de las cajas.
3. Guarda la distancia al cuadrado usada para ordenar candidatos.

### `ConnectionCandidateGenerator` - `dia8/src/main/java/domain/common/ConnectionCandidateGenerator.java`

1. Genera todos los pares posibles de cajas.
2. Calcula la distancia al cuadrado de cada par.
3. Devuelve los candidatos ordenados.

### `JunctionBox` - `dia8/src/main/java/domain/common/JunctionBox.java`

1. Representa una caja con coordenadas `x`, `y`, `z`.
2. Calcula la distancia al cuadrado respecto a otra caja.
3. Sirve como nodo del problema de conexiones.

### `CircuitSizeProductCalculatorPart1` - `dia8/src/main/java/domain/part1/CircuitSizeProductCalculatorPart1.java`

1. Procesa un número fijo de conexiones más cercanas.
2. Une cajas en `CircuitNetwork`.
3. Multiplica los tamaños de los tres circuitos mayores.

### `FinalConnectionXProductCalculatorPart2` - `dia8/src/main/java/domain/part2/FinalConnectionXProductCalculatorPart2.java`

1. Procesa conexiones ordenadas por distancia.
2. Se detiene cuando toda la red queda conectada.
3. Devuelve el producto de las coordenadas `x` de la conexión final.

### `JunctionBoxSource` - `dia8/src/main/java/infrastructure/JunctionBoxSource.java`

1. Define cómo obtener las líneas de cajas.
2. Separa la entrada de la lógica de conexión.

### `FileJunctionBoxSource` - `dia8/src/main/java/infrastructure/FileJunctionBoxSource.java`

1. Guarda la ruta del input.
2. Lee todas las líneas del fichero.
3. Implementa `JunctionBoxSource`.

## Flujo del programa

1. `Main` crea `FileJunctionBoxSource`.
2. `PlaygroundSolver` lee las cajas y las convierte en `JunctionBox`.
3. `ConnectionCandidateGenerator` genera todos los pares posibles y los ordena por distancia.
4. `CircuitNetwork` mantiene los grupos conectados mediante una estructura de unión-búsqueda.
5. La parte 1 procesa las primeras 1000 conexiones y multiplica los tres tamaños de circuito mayores.
6. La parte 2 sigue conectando hasta que todo queda en un único circuito y devuelve el producto de las coordenadas `x` de la última conexión útil.

```java
List<ConnectionCandidate> candidates = new ConnectionCandidateGenerator().generateSorted(junctionBoxes);
CircuitNetwork network = new CircuitNetwork(junctionBoxes.size());
```

La conexión devuelve `true` solo cuando une circuitos distintos, lo que permite detectar la última unión relevante.

```java
boolean connectedDifferentCircuits = network.connect(candidate.firstIndex(), candidate.secondIndex());
if (connectedDifferentCircuits && network.isSingleCircuit()) {
    JunctionBox first = junctionBoxes.get(candidate.firstIndex());
    JunctionBox second = junctionBoxes.get(candidate.secondIndex());
    return first.x() * second.x();
}
```

## Fundamentos de diseño aplicados

### Alta Cohesión

`JunctionBox` representa coordenadas y distancias, `ConnectionCandidateGenerator`
genera conexiones posibles, `CircuitNetwork` gestiona componentes conectadas y cada
calculadora aplica una regla de resultado.

### Bajo Acoplamiento

`PlaygroundSolver` depende de `JunctionBoxSource`. Las calculadoras no conocen el
parser ni la fuente de entrada; trabajan con `List<JunctionBox>`.

### Modularidad

El dominio común contiene las cajas, candidatos y red de circuitos. La parte 1 y la
parte 2 reutilizan esas piezas desde calculadoras separadas.

### Código Expresivo

`distanceSquaredTo`, `generateSorted`, `connect`, `isSingleCircuit` y
`largestCircuitSizes` comunican claramente las operaciones principales del algoritmo.

### Abstracción

`CircuitNetwork` oculta los arrays internos de unión-búsqueda (`parents`, `sizes`).
Las partes solo llaman a `connect`, `isSingleCircuit` y `largestCircuitSizes`.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

`JunctionBoxParser` parsea coordenadas, `JunctionBox` representa puntos 3D, `ConnectionCandidateGenerator` genera aristas ordenadas, `CircuitNetwork` gestiona unión-búsqueda y cada calculadora aplica una regla del enunciado.

### Principio Abierto/Cerrado (OCP)

La parte 2 se añadió con `FinalConnectionXProductCalculatorPart2` reutilizando `JunctionBox`, `ConnectionCandidateGenerator` y `CircuitNetwork`. La parte 1 no necesitó cambios.

### Principio de Sustitución de Liskov (LSP)

`PlaygroundSolver` depende de `JunctionBoxSource`. Otra fuente que entregue las líneas de cajas de conexión puede sustituir a `FileJunctionBoxSource`.

### Principio de Segregación de la Interfaz (ISP)

`JunctionBoxSource` mantiene una interfaz mínima. No mezcla lectura de datos con generación de conexiones ni cálculo de circuitos.

### Principio de Inversión de Dependencias (DIP)

La coordinación de la aplicación depende de `JunctionBoxSource` y no de la fuente concreta:

```java
public PlaygroundSolver(JunctionBoxSource source) {
    this.source = source;
}
```

### Principio de Composición sobre Herencia (COI)

Las calculadoras componen `ConnectionCandidateGenerator` y `CircuitNetwork`. No existe herencia entre redes ni entre calculadores.

### Principio DRY

La generación y ordenación de conexiones posibles está centralizada en `ConnectionCandidateGenerator`. Las dos partes reutilizan el mismo orden de candidatos.

### Convención sobre Configuración (CoC)

El módulo usa la convención Maven estándar y se ejecuta como parte del reactor sin configuración adicional.

### Principio YAGNI

No se implementa una biblioteca general de grafos. La estructura `CircuitNetwork` contiene solo lo necesario: unir componentes, saber si queda un circuito y obtener tamaños.

## Patrones de diseño aplicados

### Creacionales

No se aplica ningún patrón creacional de forma explícita. No hace falta `Singleton`
porque no existe ningún recurso global que deba tener una única instancia, y tampoco
se usa `Factory Method` porque la creación de objetos es simple y directa.

### Estructurales

Se refleja `Adapter` en `FileJunctionBoxSource`. La aplicación trabaja con
`JunctionBoxSource`, mientras que `FileJunctionBoxSource` adapta `Files.readAllLines`
a esa interfaz propia del proyecto.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`, por ejemplo
al recorrer candidatos de conexión. En Java este recorrido se apoya en
`Iterable`/`Iterator`, aunque el código no cree el iterador manualmente.

No se aplica `Command`, porque no hay objetos que encapsulen acciones ejecutables.
Tampoco se aplica `Observer`, porque no hay suscripciones ni notificación de cambios.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de coordenadas válidas;
- el rechazo de líneas inválidas;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `40`;
- que una pareja ya conectada siga contando como conexión procesada.
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `25272`;
- que la parte 2 ignore conexiones internas al mismo circuito y pare en la unión
  real que deja un único circuito.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia8 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia8 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 244188
Parte 2: 8361881885
```
