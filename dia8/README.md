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

## Principios aplicados

### Abstracción

El dominio trabaja con conceptos propios del problema: caja de conexión, red de
circuitos, conexiones candidatas y cálculos de cada parte. La lógica no depende de
rutas de ficheros ni de consola.

### Diseño por contrato

`JunctionBoxParser` rechaza entradas vacías, líneas nulas, líneas sin tres
coordenadas y coordenadas que no sean numéricas. `CircuitNetwork` exige al menos un
elemento, `CircuitSizeProductCalculatorPart1` exige al menos tres cajas y
`FinalConnectionXProductCalculatorPart2` exige al menos dos cajas.

### Alta cohesión y SRP

Cada clase tiene una responsabilidad concreta:

- `JunctionBoxParser` solo parsea coordenadas.
- `JunctionBox` solo representa una posición 3D y calcula distancias.
- `ConnectionCandidateGenerator` solo genera conexiones candidatas ordenadas.
- `CircuitNetwork` solo gestiona componentes conectadas.
- `CircuitSizeProductCalculatorPart1` solo aplica la regla de la parte 1.
- `FinalConnectionXProductCalculatorPart2` solo aplica la regla de la parte 2.
- `FileJunctionBoxSource` solo lee líneas de un fichero.
- `PlaygroundSolver` solo coordina el caso de uso.
- `Main` solo prepara dependencias y muestra la salida.

Esto sigue la idea de cohesión y responsabilidad única vista en teoría: cada módulo
tiene una razón principal para cambiar.

### Bajo acoplamiento

`PlaygroundSolver` depende de `JunctionBoxSource`, no de `FileJunctionBoxSource`:

```java
public PlaygroundSolver(JunctionBoxSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la lógica de aplicación.

### Inversión e inyección de dependencias

La lógica de alto nivel depende de una abstracción (`JunctionBoxSource`). La
implementación concreta se crea fuera y se inyecta por constructor:

```java
JunctionBoxSource source = new FileJunctionBoxSource(inputPath);
PlaygroundSolver solver = new PlaygroundSolver(source);
```

Así se separa la creación del objeto concreto de su uso, reduciendo acoplamiento.

### Modularidad

La división en paquetes separa responsabilidades:

- `domain/common`: conceptos compartidos del problema.
- `domain/part1`: regla específica de la primera parte.
- `domain/part2`: regla específica de la segunda parte.
- `application`: coordinación del caso de uso.
- `infrastructure`: detalles técnicos de entrada.

### Abierto/cerrado

La parte 2 se ha anadido creando `FinalConnectionXProductCalculatorPart2` y
reutilizando `JunctionBox`, `CircuitNetwork` y `ConnectionCandidateGenerator`. Esto
mantiene las clases del dominio comun abiertas a reutilizacion por nuevas reglas y
cerradas frente a cambios innecesarios cuando aparece una nueva parte del problema.

## Patrones y técnicas usadas

### Source / Adapter

`JunctionBoxSource` abstrae el origen de datos. `FileJunctionBoxSource` adapta
`Files.readAllLines` a una interfaz propia del proyecto.

### Value Object

`JunctionBox` se modela como `record`, por lo que representa un valor del dominio
definido por sus coordenadas.

### Service

`CircuitSizeProductCalculatorPart1` y `FinalConnectionXProductCalculatorPart2`
actúan como servicios de dominio: no representan entidades con identidad propia,
sino operaciones que calculan los resultados de cada parte.

### Unión-búsqueda

`CircuitNetwork` usa unión-búsqueda con compresión de caminos y unión por tamaño.
Esto permite unir circuitos y consultar componentes de forma eficiente.

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
