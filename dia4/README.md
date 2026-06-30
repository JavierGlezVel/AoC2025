# Día 4

## Problema

La entrada es un mapa rectangular del departamento de impresión:

- `@` indica un rollo de papel.
- `.` indica un espacio vacío.

Un rollo es accesible si tiene como mucho tres rollos alrededor. Alrededor significa
las ocho posiciones vecinas: arriba, abajo, lados y diagonales.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

Hay que contar cuántos rollos son accesibles en el mapa inicial.

Con el ejemplo oficial:

```text
..@@.@@@@.
@@@.@.@.@@
@@@@@.@.@@
@.@@@@..@.
@@.@@@@.@@
.@@@@@@@.@
.@.@.@.@@@
@.@@@.@@@@
.@@@@@@@@.
@.@.@@@.@.
```

Hay `13` rollos accesibles.

Con el input del proyecto, la respuesta de la parte 1 es:

```text
1602
```

## Parte 2

Ahora los rollos accesibles se retiran. Al quitar uno, otros rollos pueden quedarse
con menos vecinos y volverse accesibles también. El proceso continúa hasta que ya no
se pueda retirar ninguno más.

Con el ejemplo oficial, se pueden retirar `43` rollos en total.

Con el input del proyecto, la respuesta de la parte 2 es:

```text
9518
```

## Enfoque de la solución

Para la parte 1, la solución recorre todas las posiciones del mapa. Cuando encuentra
un rollo (`@`), cuenta cuántos rollos hay alrededor en las ocho posiciones vecinas.

Un rollo es accesible si cumple:

```java
map.isPaperRollAt(position)
        && map.countAdjacentPaperRolls(position) <= 3
```

El conteo de vecinos está encapsulado en `PaperRollMap`, que comprueba también los
límites del mapa para no acceder fuera de la cuadrícula.

Para la parte 2, `RemovablePaperRollCounterPart2` mantiene:

- una matriz con los rollos que siguen presentes;
- una matriz con el número de vecinos de cada rollo;
- una cola con los rollos que ya son accesibles.

Cuando se retira un rollo, solo pueden cambiar sus vecinos. Por eso no hace falta
recalcular todo el mapa en cada ronda: se decrementa el contador de vecinos de los
rollos adyacentes y, si alguno pasa a ser accesible, se añade a la cola.

Esta solución aprovecha que el proceso es monotónico: retirar rollos nunca aumenta
el número de vecinos de otro rollo.


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

- `PaperRollMap`: representa el mapa rectangular y valida sus invariantes.
- `GridPosition`: representa una posición del mapa mediante fila y columna.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `AccessiblePaperRollCounterPart1`: cuenta los rollos accesibles.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `RemovablePaperRollCounterPart2`: cuenta cuántos rollos pueden retirarse aplicando el proceso repetidamente.

### `application`

Coordina el caso de uso.

- `PaperRollMapParser`: transforma las líneas del fichero en un `PaperRollMap`.
- `PrintingDepartmentSolver`: lee la entrada, la parsea y delega el cálculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `DiagramSource`: interfaz para obtener las líneas de entrada.
- `FileDiagramSource`: implementación que lee el mapa desde un fichero.

## Clases principales

### `Main` - `Main.java`

1. Decide de dónde leer el input. Si se pasa una ruta por argumentos, usa esa ruta; si no, busca el `input.txt` del módulo.
2. Crea una fuente de datos concreta (`FileDiagramSource`) y la entrega al solver como abstracción (`DiagramSource`).
3. Ejecuta `solvePart1()` y `solvePart2()` para imprimir las respuestas finales.

```java
DiagramSource source = new FileDiagramSource(inputPath);
PrintingDepartmentSolver solver = new PrintingDepartmentSolver(source);

System.out.println("Parte 1: " + solver.solvePart1());
System.out.println("Parte 2: " + solver.solvePart2());
```

### `PrintingDepartmentSolver` - `application/PrintingDepartmentSolver.java`

1. Recibe un `DiagramSource`, por lo que no necesita saber si el input viene de un fichero, de memoria o de otra fuente.
2. Lee las líneas del diagrama y las convierte en un `PaperRollMap` usando `PaperRollMapParser`.
3. En la parte 1 delega en `AccessiblePaperRollCounterPart1`; en la parte 2 delega en `RemovablePaperRollCounterPart2`.
4. Mantiene separado el flujo de aplicación de la lógica de dominio.

```java
public int solvePart1() throws IOException {
    var map = parser.parse(source.getLines());
    return new AccessiblePaperRollCounterPart1().count(map);
}
```

### `PaperRollMapParser` - `application/PaperRollMapParser.java`

1. Recibe las líneas crudas del fichero.
2. Aplica `trim()` para eliminar espacios laterales y descarta líneas vacías.
3. Guarda solo las filas reales del mapa.
4. Construye un `PaperRollMap`, dejando que el dominio valide si el mapa es rectangular y si contiene caracteres válidos.

```java
for (String line : lines) {
    String row = line.trim();
    if (!row.isEmpty()) {
        rows.add(row);
    }
}

return new PaperRollMap(rows);
```

### `GridPosition` - `domain/common/GridPosition.java`

1. Representa una coordenada concreta de la cuadrícula mediante fila y columna.
2. Da nombre de dominio a esos dos enteros, evitando llamadas poco expresivas como `count(row, column)`.
3. Se usa para consultar si hay rollo en una posición, para generar vecinos y para actualizar el proceso de retirada.

```java
GridPosition position = new GridPosition(row, column);
if (map.isPaperRollAt(position)) {
    // La posición contiene un rollo de papel.
}
```

### `PaperRollMap` - `domain/common/PaperRollMap.java`

1. Representa el mapa completo como una lista de filas inmutables.
2. Valida que haya al menos una fila, que todas tengan la misma anchura y que solo aparezcan `.` y `@`.
3. Expone operaciones propias del problema: altura, anchura, comprobación de límites, rollo en una posición y número de rollos vecinos.
4. Centraliza la lógica de vecindad para que las calculadoras no repitan cómo recorrer las ocho posiciones alrededor de una celda.

```java
public int countAdjacentPaperRolls(GridPosition position) {
    int adjacentPaperRolls = 0;

    for (GridPosition adjacent : adjacentPositions(position)) {
        if (isPaperRollAt(adjacent)) {
            adjacentPaperRolls++;
        }
    }

    return adjacentPaperRolls;
}
```

### `AccessiblePaperRollCounterPart1` - `domain/part1/AccessiblePaperRollCounterPart1.java`

1. Recorre todas las coordenadas del mapa usando dos bucles: uno para filas y otro para columnas.
2. Para cada celda crea un `GridPosition` y comprueba si realmente contiene un rollo (`@`).
3. Pide a `PaperRollMap` cuántos rollos adyacentes tiene esa posición.
4. Suma solo los rollos con tres o menos vecinos, que son los accesibles según la parte 1.

```java
private boolean isAccessiblePaperRoll(PaperRollMap map, GridPosition position) {
    return map.isPaperRollAt(position)
            && map.countAdjacentPaperRolls(position) <= MAXIMUM_ADJACENT_ROLLS_TO_ACCESS;
}
```

### `RemovablePaperRollCounterPart2` - `domain/part2/RemovablePaperRollCounterPart2.java`

1. Copia el mapa inicial a una matriz `boolean[][]` para saber qué rollos siguen presentes.
2. Calcula una matriz `int[][]` con el número inicial de vecinos de cada rollo.
3. Mete en una cola todos los rollos que ya son accesibles al comienzo.
4. Mientras la cola no esté vacía, retira un rollo, reduce el contador de vecinos de los rollos adyacentes y encola los que pasan a tener tres o menos vecinos.
5. Devuelve cuántos rollos se han podido retirar en total.

```java
while (!accessiblePaperRolls.isEmpty()) {
    GridPosition position = accessiblePaperRolls.remove();
    if (!remainingPaperRolls[position.row()][position.column()]) {
        continue;
    }

    remainingPaperRolls[position.row()][position.column()] = false;
    removedPaperRolls++;
    updateAdjacentPaperRolls(map, position, remainingPaperRolls, adjacentPaperRolls,
            queuedPaperRolls, accessiblePaperRolls);
}
```

### `DiagramSource` - `infrastructure/DiagramSource.java`

1. Define la operación mínima que necesita la aplicación: obtener una lista de líneas.
2. Actúa como frontera entre aplicación e infraestructura.
3. Permite que `PrintingDepartmentSolver` dependa de una interfaz en lugar de depender directamente de `Files.readAllLines`.

```java
public interface DiagramSource {
    List<String> getLines() throws IOException;
}
```

### `FileDiagramSource` - `infrastructure/FileDiagramSource.java`

1. Guarda la ruta del fichero recibida desde `Main`.
2. Convierte esa ruta en un `Path`.
3. Lee todas las líneas del input con la API estándar de Java.
4. Implementa `DiagramSource`, por lo que puede usarse sin que el solver conozca los detalles de lectura.

```java
@Override
public List<String> getLines() throws IOException {
    return Files.readAllLines(Path.of(path));
}
```

## Flujo del programa

1. `Main` crea `FileDiagramSource` con la ruta del mapa.
2. `PrintingDepartmentSolver` pide las líneas a `DiagramSource`.
3. `PaperRollMapParser` limpia líneas vacías y construye un `PaperRollMap`.
4. `PaperRollMap` valida el mapa y ofrece consultas de posiciones, límites y vecinos.
5. La parte 1 recorre todas las celdas y cuenta rollos con tres o menos vecinos.
6. La parte 2 usa una cola para retirar rollos accesibles y actualizar los vecinos que se vuelven accesibles después.

```java
var map = parser.parse(source.getLines());
return new RemovablePaperRollCounterPart2().count(map);
```

El recorrido de la parte 2 funciona como una propagación: al retirar un rollo, sus vecinos pueden pasar a cumplir la condición.

```java
remainingPaperRolls[position.row()][position.column()] = false;
removedPaperRolls++;
updateAdjacentPaperRolls(map, position, remainingPaperRolls, adjacentPaperRolls,
        queuedPaperRolls, accessiblePaperRolls);
```

## Fundamentos de diseño aplicados

### Alta Cohesión

`PaperRollMap` concentra las operaciones de cuadrícula, `GridPosition` representa
coordenadas y cada contador aplica una regla concreta. La lógica de vecinos no está
duplicada dentro de los contadores.

### Bajo Acoplamiento

`PrintingDepartmentSolver` depende de `DiagramSource`, no de la fuente de fichero.
Los contadores trabajan con `PaperRollMap`, no con líneas de texto crudo.

### Modularidad

El mapa y la posición están en `domain/common`, mientras que el conteo simple y la
retirada progresiva están separados en `domain/part1` y `domain/part2`.

### Código Expresivo

Métodos como `countAdjacentPaperRolls`, `adjacentPositions` e `isPaperRollAt`
describen la regla de negocio sin necesidad de interpretar índices de matriz en cada
uso.

### Abstracción

`PaperRollMap` oculta cómo se almacenan las filas y cómo se validan los límites. Los
contadores solo preguntan por posiciones, vecinos y rollos existentes.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

`PaperRollMapParser`, `PaperRollMap`, `GridPosition`, `AccessiblePaperRollCounterPart1`, `RemovablePaperRollCounterPart2`, `FileDiagramSource` y `PrintingDepartmentSolver` tienen responsabilidades separadas. La lectura, el modelo de cuadrícula y las reglas de conteo no están mezcladas.

### Principio Abierto/Cerrado (OCP)

La parte 2 se añade como `RemovablePaperRollCounterPart2` sin modificar el contador de la parte 1 ni el modelo `PaperRollMap`. Una nueva regla de conteo podría incorporarse como otra clase en `domain/partX`.

### Principio de Sustitución de Liskov (LSP)

`PrintingDepartmentSolver` usa `DiagramSource`. Cualquier implementación de esa interfaz que entregue líneas de diagrama puede sustituir a `FileDiagramSource`.

### Principio de Segregación de la Interfaz (ISP)

`DiagramSource` es una interfaz mínima para leer líneas. No obliga a las fuentes de diagramas a implementar parseo, validación o salida por consola.

### Principio de Inversión de Dependencias (DIP)

El solver depende de la abstracción `DiagramSource`:

```java
public PrintingDepartmentSolver(DiagramSource source) {
    this.source = source;
}
```

La lectura concreta desde fichero queda en infraestructura.

### Principio de Composición sobre Herencia (COI)

El solver compone el parser, el mapa y los contadores concretos. No se crea una jerarquía de contadores abstractos para compartir código.

### Principio DRY

`PaperRollMap` concentra las operaciones comunes sobre la cuadrícula, como dimensiones, consulta de rollos y posiciones adyacentes. Las dos partes reutilizan esas operaciones.

### Convención sobre Configuración (CoC)

El día usa la estructura Maven convencional, igual que el resto de módulos, evitando configuración explícita para compilar recursos y tests.

### Principio YAGNI

No se implementa un motor genérico de simulación de cuadrículas. Solo se modelan las operaciones que exige el problema: vecinos, accesibilidad y retirada.

## Patrones de diseño aplicados

### Creacionales

No se aplica ningún patrón creacional de forma explícita. No hace falta `Singleton`
porque no existe ningún recurso global que deba tener una única instancia, y tampoco
se usa `Factory Method` porque la creación de objetos es simple y directa.

### Estructurales

Se refleja `Adapter` en `FileDiagramSource`. La aplicación trabaja con
`DiagramSource`, mientras que `FileDiagramSource` adapta `Files.readAllLines` a esa
interfaz propia del proyecto.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`, por ejemplo
al recorrer posiciones adyacentes del mapa. En Java este recorrido se apoya en
`Iterable`/`Iterator`, aunque el código no cree el iterador manualmente.

No se aplica `Command`, porque no hay objetos que encapsulen acciones ejecutables.
Tampoco se aplica `Observer`, porque no hay suscripciones ni notificación de cambios.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de filas no vacías;
- el rechazo de mapas no rectangulares;
- el rechazo de caracteres no permitidos;
- el conteo de vecinos en bordes del mapa;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `13`;
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `43`;
- el caso donde retirar unos rollos hace accesibles otros rollos posteriormente.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia4 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia4 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 1602
Parte 2: 9518
```
