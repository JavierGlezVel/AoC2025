# Día 7

## Problema

El problema ocurre en un laboratorio de teleportacion. La entrada representa un
diagrama de un colector de taquiones:

- `S` indica por donde entra el haz inicial.
- `.` indica espacio vacío.
- `^` indica un divisor.

Los haces siempre avanzan hacia abajo. Si un haz llega a un divisor, ese haz se
detiene y se emiten dos haces nuevos desde las columnas inmediatamente izquierda y
derecha del divisor. Si varios haces llegan a la misma posición, se comportan como un
único haz a partir de ahí.

En la segunda parte el colector se interpreta como un colector cuántico: no se
fusionan haces clásicos, sino líneas temporales. Dos caminos distintos que llegan a
la misma posición siguen representando dos líneas temporales distintas.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

El objetivo es contar cuántas veces se divide un haz.

Con el ejemplo oficial:

```text
.......S.......
...............
.......^.......
...............
......^.^......
...............
.....^.^.^.....
...............
....^.^...^....
...............
...^.^...^.^...
...............
..^...^.....^..
...............
.^.^.^.^.^...^.
...............
```

El resultado del ejemplo es:

```text
21
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
1541
```

## Parte 2

El objetivo es contar cuántas líneas temporales quedan activas después de que una
partícula complete todos sus recorridos posibles por el colector.

Con el mismo ejemplo oficial, el resultado es:

```text
40
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
80158285728929
```

## Enfoque de la solución

### Parte 1

`BeamSplitCounterPart1` simula el avance del haz fila a fila. En cada fila mantiene
un conjunto de columnas activas:

```java
Set<Integer> activeColumns = Set.of(start.column());
```

Para cada columna activa:

- si la celda contiene `^`, se suma una división y se activan las columnas izquierda
  y derecha para la siguiente fila;
- si la celda contiene `.`, la misma columna sigue activa en la siguiente fila.

Se usa un `Set` porque dos divisores pueden emitir haces hacia la misma columna. En
ese caso, los haces se fusionan y solo hace falta procesar esa columna una vez en la
siguiente fila.

### Parte 2

`TimelineCounterPart2` usa la misma idea de recorrer el colector fila a fila, pero
mantiene multiplicidad de líneas temporales con un mapa:

```java
Map<Integer, BigInteger> activeTimelines = Map.of(start.column(), BigInteger.ONE);
```

La clave es la columna activa y el valor es cuántas líneas temporales llegan a esa
columna. Para cada entrada del mapa:

- si la celda contiene `^`, cada línea temporal se divide en dos y se acumula en las
  columnas izquierda y derecha;
- si la celda contiene `.`, las mismas líneas temporales siguen en la misma columna;
- si una rama sale lateralmente del diagrama, esa línea temporal se considera
  completada.

Se usa `BigInteger` porque el número de líneas temporales crece de forma
exponencial con los divisores alcanzados y puede superar el rango de tipos enteros
pequeños.

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

- `TachyonManifold`: representa el diagrama, valida sus invariantes y permite
  consultar el inicio y los divisores.
- `GridPosition`: representa una posición del diagrama mediante fila y columna.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `BeamSplitCounterPart1`: cuenta las divisiones del haz.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `TimelineCounterPart2`: cuenta las líneas temporales finales conservando la
  multiplicidad de caminos.

### `application`

Coordina el caso de uso.

- `TachyonManifoldParser`: transforma las líneas del fichero en un `TachyonManifold`.
- `LaboratorySolver`: lee la entrada, la parsea y delega el cálculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `DiagramSource`: interfaz para obtener las líneas de entrada.
- `FileDiagramSource`: implementación que lee el diagrama desde un fichero.

## Principios aplicados

### Abstracción

El dominio trabaja con conceptos propios del problema: colector, posición y contador
de divisiones o líneas temporales. La lógica de simulación no depende de rutas de
ficheros ni de consola.

### Diseño por contrato

`TachyonManifold` valida que el diagrama tenga al menos una fila y una columna, que
todas las filas tengan la misma anchura, que solo aparezcan los caracteres `.`, `S` y
`^`, y que exista exactamente un inicio `S`.

### Alta cohesión y SRP

Cada clase tiene una responsabilidad concreta:

- `TachyonManifoldParser` solo parsea líneas de entrada.
- `TachyonManifold` solo representa y valida el diagrama.
- `GridPosition` solo representa una coordenada.
- `BeamSplitCounterPart1` solo aplica la regla de simulación de la parte 1.
- `TimelineCounterPart2` solo aplica la regla de simulación de la parte 2.
- `FileDiagramSource` solo lee líneas de un fichero.
- `LaboratorySolver` solo coordina el caso de uso.
- `Main` solo prepara dependencias y muestra la salida.

Esto sigue la idea de cohesión y responsabilidad única vista en teoría: cada módulo
tiene una razón principal para cambiar.

### Bajo acoplamiento

`LaboratorySolver` depende de `DiagramSource`, no de `FileDiagramSource`:

```java
public LaboratorySolver(DiagramSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la lógica de aplicación.

### Inversión e inyección de dependencias

La lógica de alto nivel depende de una abstracción (`DiagramSource`). La
implementación concreta se crea fuera y se inyecta por constructor:

```java
DiagramSource source = new FileDiagramSource(inputPath);
LaboratorySolver solver = new LaboratorySolver(source);
```

Así se separa la creación del objeto concreto de su uso, reduciendo acoplamiento.

### Modularidad

La división en paquetes separa responsabilidades:

- `domain/common`: conceptos compartidos del problema.
- `domain/part1`: regla específica de la primera parte.
- `domain/part2`: regla específica de la segunda parte.
- `application`: coordinación del caso de uso.
- `infrastructure`: detalles técnicos de entrada.

## Patrones y técnicas usadas

### Source / Adapter

`DiagramSource` abstrae el origen de datos. `FileDiagramSource` adapta
`Files.readAllLines` a una interfaz propia del proyecto.

### Value Object

`TachyonManifold` y `GridPosition` se modelan como `record`, por lo que representan
valores del dominio definidos por sus datos. `TachyonManifold` además valida sus
invariantes al construirse.

### Service

`BeamSplitCounterPart1` y `TimelineCounterPart2` actúan como servicios de dominio:
no representan entidades con identidad propia, sino operaciones que calculan los
resultados de cada parte.

### Fachada de caso de uso

`LaboratorySolver` ofrece `solvePart1` y `solvePart2`, ocultando los pasos internos:
leer entrada, parsear el diagrama y calcular la respuesta.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de un diagrama válido;
- el rechazo de diagramas sin un único inicio;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `21`;
- la fusion de haces que llegan a la misma columna.
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `40`;
- la conservación de líneas temporales distintas aunque lleguen a la misma columna;
- las líneas temporales que salen lateralmente del diagrama.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia7 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia7 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 1541
Parte 2: 80158285728929
```
