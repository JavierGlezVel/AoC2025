# Día 1

## Problema

El problema plantea una caja fuerte con un dial circular numerado del `0` al `99`.
El dial empieza en la posición `50` y recibe una lista de rotaciones, una por línea.
Cada rotación empieza por:

- `L`: giro hacia la izquierda, números menores.
- `R`: giro hacia la derecha, números mayores.

Después aparece el número de clicks que debe avanzar el dial. Como el dial es
circular, al girar a la izquierda desde `0` se pasa a `99`, y al girar a la derecha
desde `99` se pasa a `0`.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

En la primera parte se pide contar cuántas veces el dial queda apuntando a `0`
después de completar una rotación.

La solución modela el dial como un objeto con estado interno:

```java
private int position = 50;
```

Cada rotación actualiza la posición usando aritmética modular:

```java
position = (position - steps % 100 + 100) % 100;
position = (position + steps) % 100;
```

Después de cada rotación, `PasswordCalculatorPart1` comprueba si la posición final
es `0` y, en ese caso, incrementa el contador.

Con el ejemplo del enunciado, la parte 1 devuelve `3`.

## Parte 2

En la segunda parte aparece un nuevo documento de seguridad. El método
`0x434C49434B` cambia la forma de calcular la contraseña: ahora hay que contar el
número de veces que cualquier click hace que el dial apunte a `0`, tanto si ocurre al
final de una rotación como si ocurre durante la rotación.

Con las mismas rotaciones del ejemplo de la parte 1:

```text
L68
L30
R48
L5
R60
L55
L1
L99
R14
L82
```

El dial termina en `0` tres veces, como en la parte 1. Además, durante algunas
rotaciones pasa por `0` otras tres veces:

- `L68`: pasa por `0` una vez durante la rotación.
- `R60`: pasa por `0` una vez durante la rotación.
- `L82`: pasa por `0` una vez durante la rotación.

Por tanto, el resultado del ejemplo para la parte 2 es `6`.

`PasswordCalculatorPart2` cuenta los cruces por `0` antes de aplicar la rotación al
dial. Para evitar simular click a click, calcula matemáticamente cuántos ceros se
cruzan según:

- posición inicial del dial;
- dirección de la rotación;
- número total de clicks.

Ejemplo: desde `50`, una rotación `R1000` pasa por `0` diez veces antes de volver a
`50`. Esto coincide con la advertencia del enunciado: no basta con comprobar la
posición final de la rotación.

Otro ejemplo más pequeño: desde `50`, una rotación `R250` pasa por `0` tres veces:

- al llegar a `0` por primera vez;
- tras una vuelta completa;
- tras otra vuelta completa.

Por eso el test de esa rotación espera `3`.

## Diseño de clases

La solución está dividida en tres paquetes:

```text
application/
domain/
infrastructure/
```

### `domain`

Contiene las reglas del problema.

- `Dial`: representa el dial circular y encapsula su posición.
- `Rotation`: representa una orden de giro con dirección y número de pasos.
- `PasswordCalculatorPart1`: calcula la respuesta de la primera parte.
- `PasswordCalculatorPart2`: calcula la respuesta de la segunda parte.

### `application`

Coordina el caso de uso.

- `RotationParser`: transforma las líneas del fichero en objetos `Rotation`.
- `SafeSolver`: obtiene las líneas de entrada, las parsea y delega el cálculo en la clase correspondiente.

### `infrastructure`

Contiene los detalles externos al dominio.

- `RotationSource`: interfaz para obtener las líneas de entrada.
- `FileRotationSource`: implementación que lee las rotaciones desde un fichero.

## Principios aplicados

### Abstracción

La abstracción consiste en representar solo lo esencial de un concepto y ocultar los
detalles que no necesita conocer quien lo utiliza. En esta solución, el dominio trabaja
con conceptos propios del problema:

- dial;
- rotación;
- calculador de contraseña;
- fuente de rotaciones.

El resto del código no necesita conocer cómo se leen los ficheros ni cómo se calcula
internamente el módulo del dial. Por ejemplo, quien usa `Dial` solo necesita llamar a
`rotate`, no conocer la fórmula exacta de actualización de la posición.

### Diseño por contrato

El diseño por contrato consiste en dejar claras las condiciones que debe cumplir un
objeto para ser válido y poder usarse con seguridad. `Rotation` actúa como objeto de
valor y valida su propio contrato:

```java
if (direction != 'L' && direction != 'R') {
    throw new IllegalArgumentException("Direction must be L or R");
}
if (steps < 0) {
    throw new IllegalArgumentException("Steps must be >= 0");
}
```

Esto deja claro que una rotación válida solo puede tener dirección `L` o `R`, y que
los pasos no pueden ser negativos. El resto del dominio puede confiar en esa
precondición y no repetir la misma validación en cada clase.

### Alta cohesión y SRP

Una clase tiene alta cohesión cuando todos sus métodos y datos están relacionados
con una misma responsabilidad. El principio de responsabilidad única refuerza esta
idea: cada clase debe tener una sola razón principal para cambiar.

En esta solución:

- `Dial` solo sabe moverse y exponer su posición.
- `RotationParser` solo parsea texto.
- `FileRotationSource` solo lee líneas de un fichero.
- `PasswordCalculatorPart1` solo resuelve la parte 1.
- `PasswordCalculatorPart2` solo resuelve la parte 2.
- `SafeSolver` solo coordina el flujo de la aplicación.

Esto evita que una clase concentre lectura de ficheros, parseo, reglas del dial y
salida por consola. Cada cambio queda localizado en la clase que tiene esa
responsabilidad.

### Bajo acoplamiento

El bajo acoplamiento busca que las clases dependan lo menos posible de detalles
concretos de otras clases. Así, un cambio interno en una pieza afecta menos al resto
del sistema.

`SafeSolver` depende de la abstracción `RotationSource`, no directamente de
`FileRotationSource`:

```java
public SafeSolver(RotationSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la lógica de aplicación. Por
ejemplo, se podría crear una fuente en memoria para tests o una fuente que lea desde
red.

### Inversión de dependencias e inyección de dependencias

La inversión de dependencias consiste en que la lógica de alto nivel dependa de
abstracciones, no de implementaciones concretas. Aquí `SafeSolver` depende de la
interfaz `RotationSource`.

La inyección de dependencias aparece porque esa dependencia se recibe por
constructor. Esto separa la creación del objeto concreto (`FileRotationSource`) de su
uso (`SafeSolver`), haciendo que el caso de uso sea más flexible y fácil de probar.

### Modularidad

La modularidad consiste en dividir el sistema en piezas con responsabilidades claras
que puedan entenderse, cambiarse y reutilizarse de forma independiente.

La división en paquetes separa responsabilidades:

- `domain`: reglas puras del problema.
- `application`: orquestación.
- `infrastructure`: detalles técnicos de entrada.

Esto hace que el código sea más fácil de extender para otros días o para nuevas
formas de entrada.

### Polimorfismo

El polimorfismo permite trabajar con objetos distintos a través de un mismo tipo
común. En este caso, el tipo común es la interfaz `RotationSource`.

`FileRotationSource` es la implementación actual, pero el código cliente solo conoce
el contrato de `RotationSource`. Si en el futuro se añade otra implementación, como
`InMemoryRotationSource`, `SafeSolver` podría usarla sin cambiar su código.

## Patrones y técnicas usadas

### Repository / Source

`RotationSource` funciona como una abstracción de acceso a datos. No es un patrón
GoF estricto, pero sigue la idea habitual de aislar el origen de los datos para que el
dominio no dependa del sistema de ficheros.

### Value Object

`Rotation` se modela como `record`, por lo que representa un dato del dominio con
identidad basada en sus valores (`direction` y `steps`). Además, concentra la
validación de una rotación válida.

### Service

`PasswordCalculatorPart1` y `PasswordCalculatorPart2` actúan como servicios de
dominio: no representan entidades con identidad propia, sino operaciones del dominio
que calculan la contraseña a partir de una lista de rotaciones.

### Fachada de caso de uso

`SafeSolver` ofrece métodos simples (`solvePart1` y `solvePart2`) que ocultan los
pasos internos: leer entrada, parsear rotaciones y calcular la respuesta.

## Tests

Los tests están en:

```text
src/test/java/domain/
```

Cubren:

- el ejemplo oficial de la parte 1, cuyo resultado esperado es `3`;
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `6`;
- el comportamiento de la parte 2 con varias vueltas completas (`R250` y `R1000`).

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia1 compile
```

También se puede ejecutar `Main` desde IntelliJ. Si se ejecuta desde la carpeta raíz
`AOC`, el programa busca el input en:

```text
dia1/src/main/resources/input.txt
```

Si se ejecuta directamente desde `dia1`, lo busca en:

```text
src/main/resources/input.txt
```
