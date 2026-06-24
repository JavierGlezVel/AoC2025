# Dia 6

## Problema

El problema ocurre dentro de un compactador de basura. La entrada representa una hoja
de ejercicios colocada en horizontal. Cada ejercicio ocupa varias columnas:

- las filas superiores contienen los numeros del ejercicio, uno debajo de otro;
- la ultima fila contiene la operacion (`+` o `*`);
- los ejercicios estan separados por una columna completamente vacia.

La alineacion izquierda o derecha de los numeros dentro de cada ejercicio no importa.

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

En la segunda parte, la hoja se interpreta como matematica de cefalopodos: los
numeros se leen por columnas, de derecha a izquierda dentro de cada ejercicio. En
cada columna, el digito mas significativo esta arriba y el menos significativo abajo.

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

## Enfoque de la solucion

`MathWorksheetParser` normaliza todas las lineas a la misma anchura y recorre la hoja
por columnas. Cuando encuentra una columna completamente vacia, la usa como
separador entre ejercicios. Para cada bloque de columnas no vacio:

- lee los numeros de las filas superiores;
- lee la operacion de la ultima fila;
- crea un `MathProblem` con esos datos.

Para la parte 2, `parseRightToLeft` reutiliza esos mismos bloques, pero los procesa
desde el bloque de la derecha hasta el de la izquierda. Dentro de cada bloque recorre
las columnas de derecha a izquierda y forma cada numero con los digitos que aparecen
de arriba abajo en esa columna.

`WorksheetGrandTotalCalculatorPart1` y `WorksheetGrandTotalCalculatorPart2` calculan
cada problema delegando en `MathOperation`, y suman los resultados con `BigInteger`
para evitar desbordamientos cuando un ejercicio contiene multiplicaciones grandes.

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

- `MathProblem`: representa un ejercicio vertical con sus numeros y su operacion.
- `MathOperation`: representa las operaciones permitidas y sabe aplicarlas.

### `domain/part1`

Contiene la regla especifica de la primera parte.

- `WorksheetGrandTotalCalculatorPart1`: suma los resultados de todos los ejercicios.

### `domain/part2`

Contiene la regla especifica de la segunda parte.

- `WorksheetGrandTotalCalculatorPart2`: suma los resultados de los ejercicios leidos por columnas.

### `application`

Coordina el caso de uso.

- `MathWorksheetParser`: transforma las lineas del fichero en problemas del dominio.
- `TrashCompactorSolver`: lee la entrada, la parsea y delega el calculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `WorksheetSource`: interfaz para obtener las lineas de entrada.
- `FileWorksheetSource`: implementacion que lee la hoja desde un fichero.

## Principios aplicados

### Abstraccion

El dominio trabaja con conceptos propios del problema: ejercicio matematico,
operacion y total de la hoja. La logica de calculo no depende de rutas de ficheros ni
de consola.

### Diseno por contrato

`MathProblem` valida sus invariantes al construirse: exige al menos un numero y una
operacion no nula. `MathOperation.fromSymbol` rechaza cualquier simbolo distinto de
`+` o `*`.

### Alta cohesion y SRP

Cada clase tiene una responsabilidad concreta:

- `MathWorksheetParser` solo parsea la entrada horizontal.
- `MathProblem` solo representa y valida un ejercicio.
- `MathOperation` solo encapsula las operaciones disponibles.
- `WorksheetGrandTotalCalculatorPart1` solo aplica la regla de la parte 1.
- `WorksheetGrandTotalCalculatorPart2` solo aplica la regla de la parte 2.
- `FileWorksheetSource` solo lee lineas de un fichero.
- `TrashCompactorSolver` solo coordina el caso de uso.
- `Main` solo prepara dependencias y muestra la salida.

Esto sigue la idea de cohesion y responsabilidad unica vista en teoria: cada modulo
tiene una unica razon principal para cambiar.

### Bajo acoplamiento

`TrashCompactorSolver` depende de `WorksheetSource`, no de `FileWorksheetSource`:

```java
public TrashCompactorSolver(WorksheetSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la logica de aplicacion.

### Inversion e inyeccion de dependencias

La logica de alto nivel depende de una abstraccion (`WorksheetSource`). La
implementacion concreta se crea fuera y se inyecta por constructor:

```java
WorksheetSource source = new FileWorksheetSource(inputPath);
TrashCompactorSolver solver = new TrashCompactorSolver(source);
```

Asi se separa la creacion del objeto concreto de su uso, reduciendo acoplamiento.

### Modularidad

La division en paquetes separa responsabilidades:

- `domain/common`: conceptos compartidos del problema.
- `domain/part1`: regla especifica de la primera parte.
- `domain/part2`: regla especifica de la segunda parte.
- `application`: coordinacion del caso de uso.
- `infrastructure`: detalles tecnicos de entrada.

## Patrones y tecnicas usadas

### Source / Adapter

`WorksheetSource` abstrae el origen de datos. `FileWorksheetSource` adapta
`Files.readAllLines` a una interfaz propia del proyecto.

### Value Object

`MathProblem` se modela como `record`, por lo que representa un valor del dominio
definido por sus datos. Ademas, valida sus invariantes al construirse.

### Strategy con `enum`

`MathOperation` encapsula el algoritmo de cada operacion. El calculador no necesita
un condicional para saber como sumar o multiplicar; solo delega en la operacion del
problema.

### Service

`WorksheetGrandTotalCalculatorPart1` actua como servicio de dominio: no representa
una entidad con identidad propia, sino una operacion que calcula el resultado de la
parte 1.

`WorksheetGrandTotalCalculatorPart2` tambien actua como servicio de dominio, pero
para la lectura por columnas de la segunda parte.

### Fachada de caso de uso

`TrashCompactorSolver` ofrece `solvePart1` y `solvePart2`, ocultando los pasos
internos: leer entrada, parsear la hoja y calcular la respuesta.

## Tests

Los tests estan en:

```text
src/test/java/
```

Cubren:

- el parseo del ejemplo oficial;
- problemas separados por columnas vacias;
- filas con anchuras distintas;
- rechazo de operaciones desconocidas;
- el resultado oficial del ejemplo de la parte 1.
- el resultado oficial del ejemplo de la parte 2.

Para ejecutar los tests desde la raiz del repositorio:

```bash
mvn -pl dia6 test
```

## Ejecucion

Desde la raiz del repositorio:

```bash
mvn -pl dia6 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 7098065460541
Parte 2: 13807151830618
```
