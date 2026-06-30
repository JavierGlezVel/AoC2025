# Día 8

## Problema

La entrada contiene cajas de conexión en un espacio 3D. Cada línea tiene coordenadas
`X,Y,Z`:

```text
162,817,812
57,618,57
```

Las cajas se conectan por parejas, empezando por las parejas más cercanas. Cuando se
conectan dos cajas de circuitos distintos, esos circuitos se unen.

El problema consiste en simular ese proceso de conexión y observar cómo van creciendo
los circuitos.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

Hay que procesar las 1000 conexiones más cortas y multiplicar los tamaños de los
tres circuitos más grandes que queden.

No se pide saber qué cajas concretas forman cada circuito, solo el tamaño de los tres
grupos más grandes al terminar esas conexiones.

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

Ahora se siguen conectando cajas hasta que todas formen un único circuito. La
respuesta es el producto de las coordenadas `X` de las dos cajas de la conexión que
lo consigue.

La conexión importante es la última que realmente une dos circuitos separados y deja
toda la red conectada.

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

La solución empieza generando todas las parejas posibles de cajas. Para cada pareja
calcula qué tan lejos están.

Se usan todas las parejas porque una conexión puede darse entre cualquier par de
cajas si están entre las más cercanas.

```java
long distanceSquared = junctionBoxes.get(first).distanceSquaredTo(junctionBoxes.get(second));
```

La distancia se guarda al cuadrado. No hace falta calcular la raíz cuadrada, porque
para ordenar de menor a mayor da el mismo resultado y se evita trabajar con decimales.

Después se ordenan las parejas por distancia. A partir de ahí, las dos partes usan
esa misma lista ordenada:

- `CircuitSizeProductCalculatorPart1` procesa las primeras 1000 conexiones y
  multiplica los tres tamaños de circuito más grandes.
- `FinalConnectionXProductCalculatorPart2` procesa conexiones hasta que
  `CircuitNetwork` queda reducido a un único circuito y devuelve el producto de las
  coordenadas `X` de esa última conexión necesaria.

`CircuitNetwork` se encarga de recordar qué cajas están conectadas entre sí. Cuando
se conecta una pareja de cajas de circuitos distintos, esos dos circuitos pasan a ser
uno solo.

La ventaja es que no hay que reconstruir toda la red cada vez. Solo se pregunta si
dos cajas ya estaban juntas o si al conectarlas se han unido dos grupos distintos.


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

### `Main` - `Main.java`

1. Calcula la ruta del input.
2. Crea `FileJunctionBoxSource` y `PlaygroundSolver`.
3. Ejecuta las dos partes.

### `PlaygroundSolver` - `application/PlaygroundSolver.java`

1. Lee las cajas de conexión.
2. Las parsea con `JunctionBoxParser`.
3. Ejecuta la calculadora correspondiente a cada parte.

### `JunctionBoxParser` - `application/JunctionBoxParser.java`

1. Recorre las líneas del input.
2. Separa las tres coordenadas.
3. Crea objetos `JunctionBox`.

### `CircuitNetwork` - `domain/common/CircuitNetwork.java`

1. Mantiene componentes conectadas mediante unión-búsqueda.
2. Une circuitos con `connect`.
3. Calcula tamaños de los circuitos resultantes.

### `ConnectionCandidate` - `domain/common/ConnectionCandidate.java`

1. Representa una posible conexión entre dos cajas.
2. Guarda los índices de las cajas.
3. Guarda la distancia al cuadrado usada para ordenar candidatos.

### `ConnectionCandidateGenerator` - `domain/common/ConnectionCandidateGenerator.java`

1. Genera todos los pares posibles de cajas.
2. Calcula la distancia al cuadrado de cada par.
3. Devuelve los candidatos ordenados.

### `JunctionBox` - `domain/common/JunctionBox.java`

1. Representa una caja con coordenadas `x`, `y`, `z`.
2. Calcula la distancia al cuadrado respecto a otra caja.
3. Sirve como nodo del problema de conexiones.

### `CircuitSizeProductCalculatorPart1` - `domain/part1/CircuitSizeProductCalculatorPart1.java`

1. Procesa un número fijo de conexiones más cercanas.
2. Une cajas en `CircuitNetwork`.
3. Multiplica los tamaños de los tres circuitos mayores.

### `FinalConnectionXProductCalculatorPart2` - `domain/part2/FinalConnectionXProductCalculatorPart2.java`

1. Procesa conexiones ordenadas por distancia.
2. Se detiene cuando toda la red queda conectada.
3. Devuelve el producto de las coordenadas `x` de la conexión final.

### `JunctionBoxSource` - `infrastructure/JunctionBoxSource.java`

1. Define cómo obtener las líneas de cajas.
2. Separa la entrada de la lógica de conexión.

### `FileJunctionBoxSource` - `infrastructure/FileJunctionBoxSource.java`

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
