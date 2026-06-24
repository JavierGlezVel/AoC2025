# Dia 9

## Problema

El problema ocurre en un cine de la base del Polo Norte. La entrada contiene las
posiciones de baldosas rojas en una cuadricula:

```text
7,1
11,1
```

Cada linea representa una posicion `X,Y`. Se puede elegir cualquier pareja de
baldosas rojas como esquinas opuestas de un rectangulo. El objetivo es encontrar el
area maxima posible.

En la segunda parte, las baldosas rojas forman un bucle ortogonal: cada baldosa roja
esta conectada con la anterior y la siguiente mediante una linea recta de baldosas
verdes. Tambien son verdes las baldosas interiores al bucle. El rectangulo elegido
solo puede contener baldosas rojas o verdes.

La entrada esta en:

```text
src/main/resources/input.txt
```

## Parte 1

El area se calcula contando baldosas, por lo que las dimensiones son inclusivas:

```text
area = (|x1 - x2| + 1) * (|y1 - y2| + 1)
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

El rectangulo sigue teniendo que usar dos baldosas rojas como esquinas opuestas, pero
ahora todo su contenido debe quedar dentro del area roja o verde del bucle.

Con el mismo ejemplo oficial, el resultado es:

```text
24
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
1652344888
```

## Enfoque de la solucion

### Parte 1

`LargestRectangleAreaCalculatorPart1` recorre todas las parejas posibles de baldosas
rojas. Para cada pareja delega el calculo del area en `RedTile`:

```java
long area = redTiles.get(first).rectangleAreaWith(redTiles.get(second));
```

El numero de puntos del input permite una solucion directa `O(n^2)`, que es simple y
exacta para esta parte.

### Parte 2

`LargestContainedRectangleAreaCalculatorPart2` tambien recorre parejas de baldosas
rojas, pero solo acepta un rectangulo si `RedGreenTileArea` confirma que todo el
rectangulo queda dentro del bucle.

Para evitar enumerar todas las baldosas del rectangulo, `RedGreenTileArea` comprime
el poligono por filas:

- en filas que coinciden con vertices rojos, calcula la union de borde e interior
  adyacente;
- en tramos de filas entre dos coordenadas `Y` consecutivas, calcula una sola vez
  los intervalos de `X` que estan dentro del bucle;
- un rectangulo es valido si su intervalo horizontal cabe completo en todos los
  tramos de filas que ocupa.

Esta tecnica mantiene el calculo exacto sin depender del tamano real de las
coordenadas.

## Diseno de clases

La solucion esta dividida en tres paquetes principales:

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

- `RedTile`: representa una baldosa roja y calcula el area del rectangulo formado
  con otra baldosa.
- `ClosedInterval`: representa un intervalo cerrado de coordenadas.
- `RowCoverage`: representa los intervalos de `X` cubiertos en una o varias filas.
- `RedGreenTileArea`: representa el area roja o verde delimitada por el bucle.

### `domain/part1`

Contiene la regla especifica de la primera parte.

- `LargestRectangleAreaCalculatorPart1`: busca el mayor rectangulo posible.

### `domain/part2`

Contiene la regla especifica de la segunda parte.

- `LargestContainedRectangleAreaCalculatorPart2`: busca el mayor rectangulo que
  queda completamente dentro del area roja o verde.

### `application`

Coordina el caso de uso.

- `RedTileParser`: transforma las lineas del fichero en baldosas del dominio.
- `MovieTheaterSolver`: lee la entrada, la parsea y delega el calculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `RedTileSource`: interfaz para obtener las lineas de entrada.
- `FileRedTileSource`: implementacion que lee las baldosas desde un fichero.

## Principios aplicados

### Abstraccion

El dominio trabaja con conceptos propios del problema: baldosa roja y area de
rectangulo, intervalos de filas y area roja o verde. La logica no depende de rutas
de ficheros ni de consola.

### Diseno por contrato

`RedTileParser` rechaza entradas vacias, lineas nulas, lineas sin dos coordenadas y
coordenadas que no sean numericas. `LargestRectangleAreaCalculatorPart1` exige al
menos dos baldosas rojas. `RedGreenTileArea` exige un bucle con al menos cuatro
vertices y que cada par consecutivo comparta fila o columna.

### Alta cohesion y SRP

Cada clase tiene una responsabilidad concreta:

- `RedTileParser` solo parsea coordenadas.
- `RedTile` solo representa una posicion y calcula areas con otra baldosa.
- `RedGreenTileArea` solo modela la zona roja o verde y responde consultas de
  contencion.
- `RowCoverage` solo representa cobertura horizontal para filas comprimidas.
- `LargestRectangleAreaCalculatorPart1` solo aplica la regla de la parte 1.
- `LargestContainedRectangleAreaCalculatorPart2` solo aplica la regla de la parte 2.
- `FileRedTileSource` solo lee lineas de un fichero.
- `MovieTheaterSolver` solo coordina el caso de uso.
- `Main` solo prepara dependencias y muestra la salida.

Esto sigue la idea de cohesion y responsabilidad unica vista en teoria: cada modulo
tiene una razon principal para cambiar.

### Bajo acoplamiento

`MovieTheaterSolver` depende de `RedTileSource`, no de `FileRedTileSource`:

```java
public MovieTheaterSolver(RedTileSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la logica de aplicacion.

### Inversion e inyeccion de dependencias

La logica de alto nivel depende de una abstraccion (`RedTileSource`). La
implementacion concreta se crea fuera y se inyecta por constructor:

```java
RedTileSource source = new FileRedTileSource(inputPath);
MovieTheaterSolver solver = new MovieTheaterSolver(source);
```

Asi se separa la creacion del objeto concreto de su uso, reduciendo acoplamiento.

### Modularidad

La division en paquetes separa responsabilidades:

- `domain/common`: conceptos compartidos del problema.
- `domain/part1`: regla especifica de la primera parte.
- `domain/part2`: regla especifica de la segunda parte.
- `application`: coordinacion del caso de uso.
- `infrastructure`: detalles tecnicos de entrada.

### Abierto/cerrado

La parte 2 se anade creando `LargestContainedRectangleAreaCalculatorPart2` y
reutilizando `RedTile`, el parser y la infraestructura. La regla nueva se incorpora
como extension en `domain/part2`, sin modificar la calculadora de la parte 1.

## Patrones y tecnicas usadas

### Source / Adapter

`RedTileSource` abstrae el origen de datos. `FileRedTileSource` adapta
`Files.readAllLines` a una interfaz propia del proyecto.

### Value Object

`RedTile` se modela como `record`, por lo que representa un valor del dominio
definido por sus coordenadas.

### Service

`LargestRectangleAreaCalculatorPart1` y `LargestContainedRectangleAreaCalculatorPart2`
actuan como servicios de dominio: no representan entidades con identidad propia,
sino operaciones que calculan los resultados de cada parte.

## Tests

Los tests estan en:

```text
src/test/java/
```

Cubren:

- el parseo de coordenadas validas;
- el rechazo de lineas invalidas;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `50`;
- el calculo inclusivo de ancho y alto.
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `24`;
- el rechazo de rectangulos que cruzan una concavidad del bucle.

Para ejecutar los tests desde la raiz del repositorio:

```bash
mvn -pl dia9 test
```

## Ejecucion

Desde la raiz del repositorio:

```bash
mvn -pl dia9 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 4764078684
Parte 2: 1652344888
```
