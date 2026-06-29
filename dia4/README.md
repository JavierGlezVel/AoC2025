# Día 4

## Problema

El problema ocurre en el departamento de impresión. La entrada representa un mapa
rectangular donde:

- `@` indica un rollo de papel.
- `.` indica un espacio vacío.

Los forklifts solo pueden acceder a un rollo si hay menos de cuatro rollos de papel
en sus ocho posiciones adyacentes. Las posiciones adyacentes incluyen horizontal,
vertical y diagonal.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

El objetivo es contar cuántos rollos de papel son accesibles.

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

En la segunda parte, cada rollo accesible puede retirarse. Al retirar un rollo, otros
rollos pueden pasar a tener menos vecinos y volverse accesibles. El proceso se repite
hasta que no queda ningún rollo accesible.

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

## Resolución detallada

### Parte 1

El mapa se modela como una cuadrícula rectangular donde `@` representa un rollo de
papel. Para cada celda se comprueba si contiene un rollo y cuántos rollos hay en sus
ocho posiciones vecinas. Un rollo es accesible si tiene como máximo tres rollos
adyacentes.

La responsabilidad de conocer los límites del mapa y las posiciones vecinas está en
`PaperRollMap`:

```java
public List<GridPosition> adjacentPositions(GridPosition position) {
    List<GridPosition> adjacentPositions = new ArrayList<>();

    for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
        for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
            if (rowOffset == 0 && columnOffset == 0) {
                continue;
            }

            GridPosition adjacent = new GridPosition(
                    position.row() + rowOffset,
                    position.column() + columnOffset
            );
            if (contains(adjacent)) {
                adjacentPositions.add(adjacent);
            }
        }
    }

    return adjacentPositions;
}
```

La calculadora de la parte 1 recorre toda la matriz y aplica la regla de acceso:

```java
for (int row = 0; row < map.height(); row++) {
    for (int column = 0; column < map.width(); column++) {
        GridPosition position = new GridPosition(row, column);
        if (map.isPaperRollAt(position)
                && map.countAdjacentPaperRolls(position) <= 3) {
            accessiblePaperRolls++;
        }
    }
}
```

### Parte 2

En la segunda parte los rollos accesibles se retiran, y esa retirada puede hacer
accesibles a otros rollos. La solución usa una cola: primero se encolan todos los
rollos accesibles, luego se extraen uno a uno, se marcan como retirados y se
actualiza el contador de adyacentes de sus vecinos.

La clave es no recalcular toda la cuadrícula tras cada retirada. Se mantiene una
matriz de rollos restantes y otra con el número actual de vecinos:

```java
boolean[][] remainingPaperRolls = copyPaperRolls(map);
int[][] adjacentPaperRolls = countInitialAdjacentPaperRolls(map);
Queue<GridPosition> accessiblePaperRolls = new ArrayDeque<>();
```

Cuando se retira un rollo, solo cambian sus vecinos inmediatos:

```java
while (!accessiblePaperRolls.isEmpty()) {
    GridPosition position = accessiblePaperRolls.remove();
    if (!remainingPaperRolls[position.row()][position.column()]) {
        continue;
    }

    remainingPaperRolls[position.row()][position.column()] = false;
    removedPaperRolls++;
    updateAdjacentPaperRolls(map, position, remainingPaperRolls,
            adjacentPaperRolls, queuedPaperRolls, accessiblePaperRolls);
}
```

Y cada vecino que pasa a cumplir la regla se mete en la cola:

```java
if (remainingPaperRolls[position.row()][position.column()]
        && adjacentPaperRolls[position.row()][position.column()] <= 3
        && !queuedPaperRolls[position.row()][position.column()]) {
    accessiblePaperRolls.add(position);
    queuedPaperRolls[position.row()][position.column()] = true;
}
```

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
