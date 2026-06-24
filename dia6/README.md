# Día 6

## Problema

El problema ocurre dentro de un compactador de basura. La entrada representa una hoja
de ejercicios colocada en horizontal. Cada ejercicio ocupa varias columnas:

- las filas superiores contienen los números del ejercicio, uno debajo de otro;
- la última fila contiene la operación (`+` o `*`);
- los ejercicios están separados por una columna completamente vacía.

La alineación izquierda o derecha de los números dentro de cada ejercicio no importa.

La entrada debe estar en:

```text
src/main/resources/input.txt
```

## Parte 1

El objetivo es calcular el resultado de cada ejercicio y sumar todos esos resultados.

Con el ejemplo oficial:

```text
123 328  51 64
 45 64  387 23
  6 98  215 314
*   +   *   +
```

Los ejercicios son:

```text
123 * 45 * 6 = 33210
328 + 64 + 98 = 490
51 * 387 * 215 = 4243455
64 + 23 + 314 = 401
```

El resultado del ejemplo es:

```text
4277556
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
7098065460541
```

## Parte 2

En la segunda parte, la hoja se interpreta como matemática de cefalópodos: los
números se leen por columnas, de derecha a izquierda dentro de cada ejercicio. En
cada columna, el dígito más significativo está arriba y el menos significativo abajo.

Con el mismo ejemplo oficial, los ejercicios pasan a ser:

```text
4 + 431 + 623 = 1058
175 * 581 * 32 = 3253600
8 + 248 + 369 = 625
356 * 24 * 1 = 8544
```

El resultado del ejemplo es:

```text
3263827
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
13807151830618
```

## Enfoque de la solución

`MathWorksheetParser` normaliza todas las líneas a la misma anchura y recorre la hoja
por columnas. Cuando encuentra una columna completamente vacía, la usa como
separador entre ejercicios. Para cada bloque de columnas no vacío:

- lee los números de las filas superiores;
- lee la operación de la última fila;
- crea un `MathProblem` con esos datos.

Para la parte 2, `parseRightToLeft` reutiliza esos mismos bloques, pero los procesa
desde el bloque de la derecha hasta el de la izquierda. Dentro de cada bloque recorre
las columnas de derecha a izquierda y forma cada número con los dígitos que aparecen
de arriba abajo en esa columna.

`WorksheetGrandTotalCalculatorPart1` y `WorksheetGrandTotalCalculatorPart2` calculan
cada problema delegando en `MathOperation`, y suman los resultados con `BigInteger`
para evitar desbordamientos cuando un ejercicio contiene multiplicaciones grandes.

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

- `MathProblem`: representa un ejercicio vertical con sus números y su operación.
- `MathOperation`: representa las operaciones permitidas y sabe aplicarlas.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `WorksheetGrandTotalCalculatorPart1`: suma los resultados de todos los ejercicios.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `WorksheetGrandTotalCalculatorPart2`: suma los resultados de los ejercicios leídos por columnas.

### `application`

Coordina el caso de uso.

- `MathWorksheetParser`: transforma las líneas del fichero en problemas del dominio.
- `TrashCompactorSolver`: lee la entrada, la parsea y delega el cálculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `WorksheetSource`: interfaz para obtener las líneas de entrada.
- `FileWorksheetSource`: implementación que lee la hoja desde un fichero.

## Principios aplicados

### Abstracción

El dominio trabaja con conceptos propios del problema: ejercicio matemático,
operación y total de la hoja. La lógica de cálculo no depende de rutas de ficheros ni
de consola.

### Diseño por contrato

`MathProblem` valida sus invariantes al construirse: exige al menos un número y una
operación no nula. `MathOperation.fromSymbol` rechaza cualquier símbolo distinto de
`+` o `*`.

### Alta cohesión y SRP

Cada clase tiene una responsabilidad concreta:

- `MathWorksheetParser` solo parsea la entrada horizontal.
- `MathProblem` solo representa y valida un ejercicio.
- `MathOperation` solo encapsula las operaciones disponibles.
- `WorksheetGrandTotalCalculatorPart1` solo aplica la regla de la parte 1.
- `WorksheetGrandTotalCalculatorPart2` solo aplica la regla de la parte 2.
- `FileWorksheetSource` solo lee líneas de un fichero.
- `TrashCompactorSolver` solo coordina el caso de uso.
- `Main` solo prepara dependencias y muestra la salida.

Esto sigue la idea de cohesión y responsabilidad única vista en teoría: cada módulo
tiene una única razón principal para cambiar.

### Bajo acoplamiento

`TrashCompactorSolver` depende de `WorksheetSource`, no de `FileWorksheetSource`:

```java
public TrashCompactorSolver(WorksheetSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la lógica de aplicación.

### Inversión e inyección de dependencias

La lógica de alto nivel depende de una abstracción (`WorksheetSource`). La
implementación concreta se crea fuera y se inyecta por constructor:

```java
WorksheetSource source = new FileWorksheetSource(inputPath);
TrashCompactorSolver solver = new TrashCompactorSolver(source);
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

`WorksheetSource` abstrae el origen de datos. `FileWorksheetSource` adapta
`Files.readAllLines` a una interfaz propia del proyecto.

### Value Object

`MathProblem` se modela como `record`, por lo que representa un valor del dominio
definido por sus datos. Además, valida sus invariantes al construirse.

### Strategy con `enum`

`MathOperation` encapsula el algoritmo de cada operación. El calculador no necesita
un condicional para saber cómo sumar o multiplicar; solo delega en la operación del
problema.

### Service

`WorksheetGrandTotalCalculatorPart1` actúa como servicio de dominio: no representa
una entidad con identidad propia, sino una operación que calcula el resultado de la
parte 1.

`WorksheetGrandTotalCalculatorPart2` también actúa como servicio de dominio, pero
para la lectura por columnas de la segunda parte.

### Fachada de caso de uso

`TrashCompactorSolver` ofrece `solvePart1` y `solvePart2`, ocultando los pasos
internos: leer entrada, parsear la hoja y calcular la respuesta.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo del ejemplo oficial;
- problemas separados por columnas vacías;
- filas con anchuras distintas;
- rechazo de operaciones desconocidas;
- el resultado oficial del ejemplo de la parte 1.
- el resultado oficial del ejemplo de la parte 2.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia6 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia6 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 7098065460541
Parte 2: 13807151830618
```
