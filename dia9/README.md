# Día 9

## Problema

La entrada contiene posiciones de baldosas rojas en una cuadrícula:

```text
7,1
11,1
```

Cada línea es una posición `X,Y`. Se pueden elegir dos baldosas rojas como esquinas
opuestas de un rectángulo.

En la parte 2, las baldosas rojas delimitan una zona. El rectángulo elegido debe
quedar dentro de esa zona.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

Hay que encontrar el rectángulo de mayor área usando dos baldosas rojas como esquinas.
El área cuenta las baldosas incluidas:

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

La idea es la misma, pero ahora el rectángulo completo debe estar dentro del área
válida marcada por las baldosas.

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


## Uso de Streams

En este día los Streams se usan para parsear, comprobar contención y ordenar o buscar
intervalos.

El parser convierte cada línea en una baldosa roja:

```java
return lines.stream()
        .map(this::parseLine)
        .toList();
```

`map(this::parseLine)` transforma texto en `RedTile`, y `toList()` devuelve la lista
de vértices que usan las dos partes.

La comprobación de si un rectángulo está dentro del área usa `filter` y `allMatch`:

```java
return rowCoverages.stream()
        .filter(rowCoverage -> rowCoverage.intersectsRows(firstY, lastY))
        .allMatch(rowCoverage -> rowCoverage.containsXInterval(xInterval));
```

El stream recorre las coberturas por filas. `filter` se queda solo con las coberturas
que afectan al rango vertical del rectángulo. `allMatch` exige que todas esas
coberturas contengan el intervalo horizontal completo; si alguna fila relevante no
lo contiene, el rectángulo no cabe.

`RowCoverage` usa `anyMatch` para comprobar si algún intervalo horizontal contiene
otro intervalo:

```java
return xIntervals.stream()
        .anyMatch(xInterval -> xInterval.contains(interval));
```

Aquí basta con encontrar un intervalo válido, por eso se usa `anyMatch`.

También ordena intervalos con un stream:

```java
List<ClosedInterval> sortedIntervals = intervals.stream()
        .sorted(Comparator.comparingLong(ClosedInterval::start))
        .toList();
```

`sorted` coloca los intervalos por su inicio. Esa lista ordenada permite validarlos y
trabajar con coberturas horizontales de forma predecible.

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

## Clases principales

### `Main` - `Main.java`

1. Calcula la ruta del fichero de entrada del día 9.
2. Crea `FileRedTileSource` y `MovieTheaterSolver`.
3. Ejecuta la parte 1 y la parte 2, mostrando sus resultados por consola.

### `MovieTheaterSolver` - `application/MovieTheaterSolver.java`

1. Pide las líneas del input a `RedTileSource`.
2. Convierte esas líneas en una lista de `RedTile` mediante `RedTileParser`.
3. Delega cada parte en su calculadora correspondiente.

### `RedTileParser` - `application/RedTileParser.java`

1. Recorre las líneas con coordenadas de baldosas rojas.
2. Separa los valores `x` e `y` de cada coordenada.
3. Crea objetos `RedTile` ya validados para el dominio.

### `ClosedInterval` - `domain/common/ClosedInterval.java`

1. Representa un intervalo cerrado con inicio y fin.
2. Comprueba si contiene otro intervalo.
3. Detecta y fusiona intervalos que se solapan o se tocan.

### `RedGreenTileArea` - `domain/common/RedGreenTileArea.java`

1. Recibe las baldosas rojas que delimitan el área.
2. Construye coberturas horizontales por filas.
3. Comprueba si un rectángulo completo queda dentro de la zona válida.

### `RedTile` - `domain/common/RedTile.java`

1. Representa una baldosa mediante sus coordenadas.
2. Calcula el área del rectángulo formado con otra baldosa.
3. Actúa como esquina candidata para formar rectángulos.

### `RowCoverage` - `domain/common/RowCoverage.java`

1. Agrupa los intervalos horizontales cubiertos en un rango de filas.
2. Fusiona intervalos compatibles para simplificar la cobertura.
3. Comprueba si una fila cubre completamente un intervalo de columnas.

### `LargestRectangleAreaCalculatorPart1` - `domain/part1/LargestRectangleAreaCalculatorPart1.java`

1. Prueba combinaciones de dos baldosas rojas.
2. Calcula el área del rectángulo que formarían como esquinas opuestas.
3. Devuelve el mayor área encontrada.

### `LargestContainedRectangleAreaCalculatorPart2` - `domain/part2/LargestContainedRectangleAreaCalculatorPart2.java`

1. Construye un `RedGreenTileArea` para saber qué posiciones son válidas.
2. Prueba pares de baldosas como en la parte 1.
3. Solo acepta rectángulos que estén completamente contenidos en el área.

### `RedTileSource` - `infrastructure/RedTileSource.java`

1. Define la operación para obtener líneas de coordenadas.
2. Permite que la aplicación dependa de una abstracción y no de un fichero concreto.

### `FileRedTileSource` - `infrastructure/FileRedTileSource.java`

1. Guarda la ruta del fichero de entrada.
2. Lee todas sus líneas.
3. Implementa `RedTileSource` para alimentar al solver desde disco.

## Flujo del programa

1. `Main` crea `FileRedTileSource`.
2. `MovieTheaterSolver` lee coordenadas y las transforma en `RedTile` mediante `RedTileParser`.
3. La parte 1 prueba pares de baldosas rojas como esquinas opuestas de un rectángulo.
4. La parte 2 construye `RedGreenTileArea` para saber qué zonas están cubiertas.
5. En la parte 2 solo se acepta un rectángulo si `containsRectangle` confirma que está completamente dentro del área válida.

```java
var redTiles = parser.parse(source.getLines());
return new LargestContainedRectangleAreaCalculatorPart2().calculate(redTiles);
```

Ambas partes comparten la búsqueda por pares, pero la segunda añade la comprobación de contención:

```java
long rectangleArea = firstCorner.rectangleAreaWith(secondCorner);

if (rectangleArea > largestArea && area.containsRectangle(firstCorner, secondCorner)) {
    largestArea = rectangleArea;
}
```

## Fundamentos de diseño aplicados

### Alta Cohesión

`RedTile` representa puntos y áreas entre esquinas, `RedGreenTileArea` responde a
consultas de contención, `RowCoverage` modela cobertura horizontal y cada calculadora
resuelve una parte.

### Bajo Acoplamiento

`MovieTheaterSolver` depende de `RedTileSource`. Las calculadoras trabajan con
`RedTile` y `RedGreenTileArea`, no con el parser ni con el fichero.

### Modularidad

La geometría compartida se separa en `domain/common`. La búsqueda sin contención y la
búsqueda con contención viven en clases distintas dentro de `domain/part1` y
`domain/part2`.

### Código Expresivo

Métodos como `rectangleAreaWith`, `containsRectangle`, `intersectsRows` y
`containsXInterval` explican la intención geométrica de cada operación.

### Abstracción

`RedGreenTileArea` oculta la construcción de coberturas por filas. La calculadora de
la parte 2 solo pregunta si un rectángulo está contenido, sin conocer los detalles
del barrido geométrico.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

Cada clase tiene una responsabilidad clara:

- `RedTileParser` parsea coordenadas.
- `RedTile` representa una baldosa y calcula áreas con otra.
- `RedGreenTileArea` modela la zona contenida.
- `RowCoverage` representa cobertura horizontal por filas.
- `LargestRectangleAreaCalculatorPart1` resuelve la parte 1.
- `LargestContainedRectangleAreaCalculatorPart2` resuelve la parte 2.
- `MovieTheaterSolver` coordina el caso de uso.

### Principio Abierto/Cerrado (OCP)

La parte 2 se añade como una clase nueva que reutiliza `RedTile`, `RedGreenTileArea`, el parser y la fuente de entrada. La calculadora de la parte 1 permanece cerrada a cambios.

### Principio de Sustitución de Liskov (LSP)

`MovieTheaterSolver` depende de `RedTileSource`. Cualquier fuente que proporcione las líneas de baldosas puede sustituir a `FileRedTileSource` sin alterar el solver.

### Principio de Segregación de la Interfaz (ISP)

`RedTileSource` solo representa la capacidad de leer líneas. La interfaz no obliga a implementar parseo, validación geométrica ni escritura.

### Principio de Inversión de Dependencias (DIP)

El solver depende de la abstracción `RedTileSource`:

```java
public MovieTheaterSolver(RedTileSource source) {
    this.source = source;
}
```

La infraestructura concreta queda fuera de la lógica de aplicación.

### Principio de Composición sobre Herencia (COI)

La geometría se construye componiendo `RedTile`, `ClosedInterval`, `RowCoverage` y `RedGreenTileArea`. No se crea una jerarquía general de figuras geométricas.

### Principio DRY

`RedTile` centraliza coordenadas y cálculo de área; `RedGreenTileArea` centraliza la comprobación de contención. Las dos partes no duplican parseo ni representación de baldosas.

### Convención sobre Configuración (CoC)

El día respeta la estructura Maven común: fuentes, recursos y tests están en las rutas convencionales.

### Principio YAGNI

No se añade un motor geométrico general. Solo se implementa la geometría necesaria para rectángulos formados por baldosas del enunciado.

## Patrones de diseño aplicados

### Creacionales

No se aplica ningún patrón creacional de forma explícita. No hace falta `Singleton`
porque no existe ningún recurso global que deba tener una única instancia, y tampoco
se usa `Factory Method` porque la creación de objetos es simple y directa.

### Estructurales

Se refleja `Adapter` en `FileRedTileSource`. La aplicación trabaja con
`RedTileSource`, mientras que `FileRedTileSource` adapta `Files.readAllLines` a esa
interfaz propia del proyecto.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`, por ejemplo
al recorrer baldosas e intervalos. En Java este recorrido se apoya en
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
