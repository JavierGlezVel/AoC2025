# Día 6

## Problema

La entrada es una hoja con varios ejercicios escritos en columnas. Cada ejercicio
ocupa un bloque de columnas:

- las filas superiores contienen los números del ejercicio, uno debajo de otro;
- la última fila contiene la operación (`+` o `*`);
- los ejercicios están separados por una columna completamente vacía.

La separación por columnas vacías permite saber dónde empieza y termina cada
ejercicio.

Aunque visualmente la hoja parece rara, el programa la trata como una cuadrícula de
caracteres. Primero detecta los bloques de columnas y luego interpreta cada bloque
como un ejercicio.

La entrada debe estar en:

```text
src/main/resources/input.txt
```

## Parte 1

Hay que resolver cada ejercicio y sumar todos los resultados.

En esta parte los números se leen de arriba hacia abajo dentro de cada bloque, tal y
como aparecen escritos en la hoja.

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

Ahora los números se leen de otra forma: por columnas y de derecha a izquierda dentro
de cada ejercicio. Después se resuelve cada ejercicio igual que antes.

La operación sigue siendo `+` o `*`, pero cambian los números que se forman a partir
de la hoja.

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

`MathWorksheetParser` trata la hoja como una cuadrícula de caracteres. Para poder
leerla bien, primero hace que todas las líneas tengan la misma longitud.

Normalizar significa rellenar con espacios las líneas más cortas para poder leer
cualquier columna sin salirse. Así todas las filas tienen la misma longitud.

Después recorre la hoja por columnas. Cuando encuentra una columna completamente
vacía, entiende que ahí termina un ejercicio y empieza otro.

Para cada bloque de columnas no vacío:

- lee los números de las filas superiores;
- lee la operación de la última fila;
- crea un `MathProblem` con esos datos.

Para la parte 1, los números se leen tal como aparecen en cada bloque. Para la parte
2, `parseRightToLeft` usa los mismos bloques, pero lee las columnas desde la derecha
hacia la izquierda.

En la parte 2, cada columna forma un número usando los dígitos de arriba abajo. Así
la hoja se interpreta de otra manera, pero se reutiliza la misma idea de separar
ejercicios por columnas vacías.

`WorksheetGrandTotalCalculatorPart1` y `WorksheetGrandTotalCalculatorPart2` calculan
cada problema usando `MathOperation`. Esa clase sabe si debe sumar o multiplicar.
Los resultados se guardan en `BigInteger` porque algunas multiplicaciones pueden dar
números muy grandes.


## Uso de Streams

Este día usa Streams en el parser y en las operaciones matemáticas.

Para normalizar la hoja, primero se calcula la anchura máxima de todas las líneas:

```java
int width = lines.stream()
        .mapToInt(String::length)
        .max()
        .orElse(0);
```

El stream recorre las líneas de entrada. `mapToInt(String::length)` convierte cada
línea en su longitud y `max()` obtiene la mayor. Ese ancho se usa para rellenar con
espacios las líneas más cortas.

La normalización también usa un stream:

```java
return lines.stream()
        .map(line -> line + " ".repeat(width - line.length()))
        .toList();
```

Aquí `map` transforma cada línea añadiendo los espacios que faltan para alcanzar la
anchura común. `toList()` devuelve la lista normalizada que luego puede recorrerse
por columnas sin salirse de rango.

Para detectar columnas vacías se usa `allMatch`:

```java
return lines.stream().allMatch(line -> line.charAt(column) == ' ');
```

El stream comprueba la misma columna en todas las líneas. `allMatch` solo devuelve
`true` si todas tienen un espacio en esa posición; por eso sirve para separar
bloques de problemas.

Las operaciones `+` y `*` también se expresan con Streams sobre los números del
problema:

```java
return numbers.stream()
        .reduce(BigInteger.ZERO, BigInteger::add);
```

`reduce` empieza en `BigInteger.ZERO` y va sumando cada número. Para la multiplicación
se usa el mismo patrón, pero empezando en `BigInteger.ONE` y aplicando
`BigInteger::multiply`.

Finalmente, las dos partes calculan el total de la hoja aplicando cada operación y
sumando los resultados:

```java
return problems.stream()
        .map(problem -> problem.operation().apply(problem.numbers()))
        .reduce(BigInteger.ZERO, BigInteger::add);
```

El stream parte de `List<MathProblem>`. `map` transforma cada problema en su resultado
numérico y `reduce` suma todos esos resultados para obtener el total final.

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

## Clases principales

### `Main` - `Main.java`

1. Resuelve la ruta del input.
2. Crea `FileWorksheetSource` y `TrashCompactorSolver`.
3. Imprime el total de cada parte.

### `TrashCompactorSolver` - `application/TrashCompactorSolver.java`

1. Lee las líneas de la hoja con `WorksheetSource`.
2. Usa `MathWorksheetParser` para obtener problemas matemáticos.
3. Ejecuta la calculadora de la parte correspondiente.

### `MathWorksheetParser` - `application/MathWorksheetParser.java`

1. Normaliza el ancho de las líneas.
2. Detecta bloques de columnas que forman problemas.
3. Parsea números y operación en orden horizontal o vertical según la parte.

### `MathOperation` - `domain/common/MathOperation.java`

1. Representa las operaciones permitidas (`+` y `*`).
2. Convierte símbolos de entrada en operaciones.
3. Aplica la operación a una lista de números.

### `MathProblem` - `domain/common/MathProblem.java`

1. Agrupa los números de un problema y su operación.
2. Valida que haya al menos un número.
3. Copia la lista de números para mantener el objeto estable.

### `WorksheetGrandTotalCalculatorPart1` - `domain/part1/WorksheetGrandTotalCalculatorPart1.java`

1. Recibe los problemas parseados de izquierda a derecha.
2. Aplica la operación de cada problema.
3. Suma todos los resultados en un `BigInteger`.

### `WorksheetGrandTotalCalculatorPart2` - `domain/part2/WorksheetGrandTotalCalculatorPart2.java`

1. Recibe los problemas parseados de derecha a izquierda.
2. Aplica la misma lógica de operación que la parte 1.
3. Suma el total final de la hoja.

### `WorksheetSource` - `infrastructure/WorksheetSource.java`

1. Define cómo obtener las líneas de la hoja.
2. Aísla el solver de la fuente concreta.

### `FileWorksheetSource` - `infrastructure/FileWorksheetSource.java`

1. Guarda la ruta del fichero.
2. Lee todas las líneas.
3. Implementa `WorksheetSource`.

## Flujo del programa

1. `Main` crea `FileWorksheetSource`.
2. `TrashCompactorSolver` lee la hoja de operaciones.
3. En la parte 1, `MathWorksheetParser.parse` divide la hoja en problemas por rangos de columnas.
4. En la parte 2, `parseRightToLeft` interpreta los problemas desde la derecha hacia la izquierda.
5. Cada problema queda representado como `MathProblem`, con una lista de números y una `MathOperation`.
6. La calculadora aplica la operación de cada problema y suma todos los resultados con `BigInteger`.

```java
var problems = parser.parseRightToLeft(source.getLines());
return new WorksheetGrandTotalCalculatorPart2().calculate(problems);
```

La suma final de problemas se expresa como stream: cada problema se evalúa y luego se acumula en un total.

```java
return problems.stream()
        .map(problem -> problem.operation().apply(problem.numbers()))
        .reduce(BigInteger.ZERO, BigInteger::add);
```

## Fundamentos de diseño aplicados

### Alta Cohesión

`MathWorksheetParser` se encarga del parseo de la hoja, `MathProblem` representa un
ejercicio y `MathOperation` contiene las operaciones matemáticas. Las calculadoras
solo suman resultados ya parseados.

### Bajo Acoplamiento

`TrashCompactorSolver` depende de `WorksheetSource`. Las operaciones matemáticas no
dependen del parser, y el parser no depende de la forma en la que se mostrará la
respuesta.

### Modularidad

El parseo vive en `application`, los conceptos matemáticos en `domain/common`, las
reglas de cada parte en sus paquetes y la lectura del fichero en `infrastructure`.

### Código Expresivo

Métodos como `parseRightToLeft`, `findProblemRanges` e `isBlankColumn` explican el
proceso de lectura de la hoja. `MathOperation.ADD` y `MathOperation.MULTIPLY`
expresan las dos operaciones válidas.

### Abstracción

`MathOperation.apply` oculta cómo se calcula una suma o una multiplicación. Las
calculadoras solo aplican la operación asociada a cada `MathProblem`.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

`MathWorksheetParser` parsea la hoja, `MathProblem` representa un ejercicio, `MathOperation` encapsula las operaciones, cada calculadora resuelve una parte y `TrashCompactorSolver` coordina el caso de uso.

### Principio Abierto/Cerrado (OCP)

La parte 2 cambia la forma de parsear los operandos, pero reutiliza `MathProblem`, `MathOperation` y el cálculo final. Una nueva parte podría añadir otro método de parseo o calculadora sin modificar las reglas ya cerradas.

### Principio de Sustitución de Liskov (LSP)

`TrashCompactorSolver` depende de `WorksheetSource`. Cualquier implementación que proporcione líneas de hoja puede reemplazar a `FileWorksheetSource`.

### Principio de Segregación de la Interfaz (ISP)

`WorksheetSource` tiene una responsabilidad mínima: leer líneas. No obliga a implementar escritura, parseo ni operaciones matemáticas.

### Principio de Inversión de Dependencias (DIP)

El solver depende de la abstracción `WorksheetSource` y recibe la implementación por constructor:

```java
public TrashCompactorSolver(WorksheetSource source) {
    this.source = source;
}
```

### Principio de Composición sobre Herencia (COI)

El solver compone fuente, parser y calculadoras concretas. No se usa herencia para compartir la suma final ni para representar operaciones.

### Principio DRY

`MathProblem` y `MathOperation` son comunes a las dos partes. Cambia el parseo de los operandos, pero la aplicación de `+` y `*` no se duplica.

### Convención sobre Configuración (CoC)

El módulo respeta las convenciones Maven de carpetas, lo que permite ejecutarlo dentro del proyecto sin configuración adicional.

### Principio YAGNI

No se implementa un parser de expresiones general. El código soporta exactamente lo que pide el reto: bloques de números y operaciones `+` o `*`.

## Patrones de diseño aplicados

### Creacionales

No se aplica ningún patrón creacional de forma explícita. No hace falta `Singleton`
porque no existe ningún recurso global que deba tener una única instancia, y tampoco
se usa `Factory Method` porque la creación de objetos es simple y directa.

### Estructurales

Se refleja `Adapter` en `FileWorksheetSource`. La aplicación trabaja con
`WorksheetSource`, mientras que `FileWorksheetSource` adapta `Files.readAllLines` a
esa interfaz propia del proyecto.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`, por ejemplo
al recorrer problemas y operaciones. En Java este recorrido se apoya en
`Iterable`/`Iterator`, aunque el código no cree el iterador manualmente.

No se aplica `Command`, porque no hay objetos que encapsulen acciones ejecutables.
Tampoco se aplica `Observer`, porque no hay suscripciones ni notificación de cambios.

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
