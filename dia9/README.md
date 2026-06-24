# Día 9

## Problema

El problema ocurre en un cine de la base del Polo Norte. La entrada contiene las
posiciones de baldosas rojas en una cuadrícula:

```text
7,1
11,1
```

Cada línea representa una posición `X,Y`. Se puede elegir cualquier pareja de
baldosas rojas como esquinas opuestas de un rectángulo. El objetivo es encontrar el
área máxima posible.

En la segunda parte, las baldosas rojas forman un bucle ortogonal: cada baldosa roja
está conectada con la anterior y la siguiente mediante una línea recta de baldosas
verdes. También son verdes las baldosas interiores al bucle. El rectángulo elegido
solo puede contener baldosas rojas o verdes.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

El área se calcula contando baldosas, por lo que las dimensiones son inclusivas:

```text
área = (|x1 - x2| + 1) * (|y1 - y2| + 1)
```

Con el ejemplo oficial:

```text
7,1
11,1
11,7
9,7
9,5
2,5
2,3
7,3
```

El resultado es:

```text
50
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
4764078684
```

## Parte 2

El rectángulo sigue teniendo que usar dos baldosas rojas como esquinas opuestas, pero
ahora todo su contenido debe quedar dentro del área roja o verde del bucle.

Con el mismo ejemplo oficial, el resultado es:

```text
24
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
1652344888
```

## Enfoque de la solución

### Parte 1

`LargestRectangleAreaCalculatorPart1` recorre todas las parejas posibles de baldosas
rojas. Para cada pareja delega el cálculo del área en `RedTile`:

```java
long area = redTiles.get(first).rectangleAreaWith(redTiles.get(second));
```

El número de puntos del input permite una solución directa `O(n^2)`, que es simple y
exacta para esta parte.

### Parte 2

`LargestContainedRectangleAreaCalculatorPart2` también recorre parejas de baldosas
rojas, pero solo acepta un rectángulo si `RedGreenTileArea` confirma que todo el
rectángulo queda dentro del bucle.

Para evitar enumerar todas las baldosas del rectángulo, `RedGreenTileArea` comprime
el polígono por filas:

- en filas que coinciden con vértices rojos, calcula la unión de borde e interior
  adyacente;
- en tramos de filas entre dos coordenadas `Y` consecutivas, calcula una sola vez
  los intervalos de `X` que están dentro del bucle;
- un rectángulo es válido si su intervalo horizontal cabe completo en todos los
  tramos de filas que ocupa.

Esta técnica mantiene el cálculo exacto sin depender del tamaño real de las
coordenadas.

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

- `RedTile`: representa una baldosa roja y calcula el área del rectángulo formado
  con otra baldosa.
- `ClosedInterval`: representa un intervalo cerrado de coordenadas.
- `RowCoverage`: representa los intervalos de `X` cubiertos en una o varias filas.
- `RedGreenTileArea`: representa el área roja o verde delimitada por el bucle.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `LargestRectangleAreaCalculatorPart1`: busca el mayor rectángulo posible.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `LargestContainedRectangleAreaCalculatorPart2`: busca el mayor rectángulo que
  queda completamente dentro del área roja o verde.

### `application`

Coordina el caso de uso.

- `RedTileParser`: transforma las líneas del fichero en baldosas del dominio.
- `MovieTheaterSolver`: lee la entrada, la parsea y delega el cálculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `RedTileSource`: interfaz para obtener las líneas de entrada.
- `FileRedTileSource`: implementación que lee las baldosas desde un fichero.

## Principios aplicados

### Abstracción

El dominio trabaja con conceptos propios del problema: baldosa roja y área de
rectángulo, intervalos de filas y área roja o verde. La lógica no depende de rutas
de ficheros ni de consola.

### Diseño por contrato

`RedTileParser` rechaza entradas vacías, líneas nulas, líneas sin dos coordenadas y
coordenadas que no sean numéricas. `LargestRectangleAreaCalculatorPart1` exige al
menos dos baldosas rojas. `RedGreenTileArea` exige un bucle con al menos cuatro
vértices y que cada par consecutivo comparta fila o columna.

### Alta cohesión y SRP

Cada clase tiene una responsabilidad concreta:

- `RedTileParser` solo parsea coordenadas.
- `RedTile` solo representa una posición y calcula áreas con otra baldosa.
- `RedGreenTileArea` solo modela la zona roja o verde y responde consultas de
  contención.
- `RowCoverage` solo representa cobertura horizontal para filas comprimidas.
- `LargestRectangleAreaCalculatorPart1` solo aplica la regla de la parte 1.
- `LargestContainedRectangleAreaCalculatorPart2` solo aplica la regla de la parte 2.
- `FileRedTileSource` solo lee líneas de un fichero.
- `MovieTheaterSolver` solo coordina el caso de uso.
- `Main` solo prepara dependencias y muestra la salida.

Esto sigue la idea de cohesión y responsabilidad única vista en teoría: cada módulo
tiene una razón principal para cambiar.

### Bajo acoplamiento

`MovieTheaterSolver` depende de `RedTileSource`, no de `FileRedTileSource`:

```java
public MovieTheaterSolver(RedTileSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la lógica de aplicación.

### Inversión e inyección de dependencias

La lógica de alto nivel depende de una abstracción (`RedTileSource`). La
implementación concreta se crea fuera y se inyecta por constructor:

```java
RedTileSource source = new FileRedTileSource(inputPath);
MovieTheaterSolver solver = new MovieTheaterSolver(source);
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

La parte 2 se añade creando `LargestContainedRectangleAreaCalculatorPart2` y
reutilizando `RedTile`, el parser y la infraestructura. La regla nueva se incorpora
como extensión en `domain/part2`, sin modificar la calculadora de la parte 1.

## Patrones y técnicas usadas

### Source / Adapter

`RedTileSource` abstrae el origen de datos. `FileRedTileSource` adapta
`Files.readAllLines` a una interfaz propia del proyecto.

### Value Object

`RedTile` se modela como `record`, por lo que representa un valor del dominio
definido por sus coordenadas.

### Service

`LargestRectangleAreaCalculatorPart1` y `LargestContainedRectangleAreaCalculatorPart2`
actúan como servicios de dominio: no representan entidades con identidad propia,
sino operaciones que calculan los resultados de cada parte.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de coordenadas válidas;
- el rechazo de líneas inválidas;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `50`;
- el cálculo inclusivo de ancho y alto.
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `24`;
- el rechazo de rectángulos que cruzan una concavidad del bucle.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia9 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia9 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 4764078684
Parte 2: 1652344888
```
