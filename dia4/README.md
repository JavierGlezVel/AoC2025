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

## Principios aplicados

### Abstracción

El dominio trabaja con conceptos propios del problema: mapa, posición y contador de
rollos accesibles. El cálculo no necesita conocer cómo se lee el fichero ni cómo se
muestra la respuesta por consola.

`PaperRollMap` ofrece operaciones del dominio como `isPaperRollAt` y
`countAdjacentPaperRolls`, ocultando los detalles de índices y límites.

### Diseño por contrato

`PaperRollMap` valida que el mapa sea utilizable:

```java
if (rows == null || rows.isEmpty()) {
    throw new IllegalArgumentException("A paper roll map needs at least one row");
}
```

También exige que todas las filas tengan la misma anchura y que solo aparezcan los
caracteres `.` y `@`. Así, el contador puede trabajar confiando en que el mapa es
rectangular y válido.

### Alta cohesión y SRP

Cada clase tiene una responsabilidad concreta:

- `PaperRollMapParser` solo parsea líneas de entrada.
- `PaperRollMap` solo representa y valida el mapa.
- `GridPosition` solo representa una coordenada.
- `AccessiblePaperRollCounterPart1` solo aplica la regla de accesibilidad.
- `RemovablePaperRollCounterPart2` solo aplica el proceso iterativo de retirada.
- `FileDiagramSource` solo lee líneas de un fichero.
- `PrintingDepartmentSolver` solo coordina el caso de uso.
- `Main` solo prepara dependencias y muestra la salida.

Esto evita mezclar lectura de ficheros, validación del mapa, conteo de vecinos y
salida por consola en una única clase.

### Bajo acoplamiento

`PrintingDepartmentSolver` depende de `DiagramSource`, no de `FileDiagramSource`:

```java
public PrintingDepartmentSolver(DiagramSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la lógica de aplicación.

### Inversión e inyección de dependencias

La lógica de alto nivel depende de una abstracción (`DiagramSource`). La
implementación concreta se crea fuera y se inyecta por constructor:

```java
DiagramSource source = new FileDiagramSource(inputPath);
PrintingDepartmentSolver solver = new PrintingDepartmentSolver(source);
```

Así se separa la creación del objeto concreto de su uso.

### Modularidad

La división en paquetes separa responsabilidades:

- `domain/common`: conceptos compartidos del problema.
- `domain/part1`: regla específica de la primera parte.
- `domain/part2`: regla específica de la segunda parte.
- `application`: coordinación del caso de uso.
- `infrastructure`: detalles técnicos de entrada.

Esto deja claro qué código pertenece a cada parte y qué código es compartido.

### Polimorfismo

El polimorfismo aparece en `DiagramSource`. `FileDiagramSource` es la implementación
actual, pero `PrintingDepartmentSolver` solo conoce la interfaz. Podría usarse otra
implementación, como una fuente en memoria, sin cambiar el solver.

## Patrones y técnicas usadas

### Source / Adapter

`DiagramSource` abstrae el origen de datos. `FileDiagramSource` adapta la lectura de
`Files.readAllLines` a una interfaz propia del proyecto.

### Value Object

`GridPosition` se modela como `record`, por lo que representa un valor del dominio
definido por sus datos (`row` y `column`).

### Service

`AccessiblePaperRollCounterPart1` actúa como servicio de dominio: no representa una
entidad con identidad propia, sino una operación que calcula el resultado de la parte
1.

`RemovablePaperRollCounterPart2` también actúa como servicio de dominio, pero para
la regla iterativa de la parte 2.

### Cola de trabajo

La parte 2 usa una cola para procesar rollos que ya cumplen la regla de acceso. Esto
evita buscar desde cero en todo el mapa después de cada retirada.

### Fachada de caso de uso

`PrintingDepartmentSolver` ofrece métodos simples (`solvePart1` y `solvePart2`) que
ocultan los pasos internos: leer entrada, parsear el mapa y calcular la respuesta.

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
