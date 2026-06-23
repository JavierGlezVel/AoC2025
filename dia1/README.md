# Dia 1

## Problema

El problema plantea una caja fuerte con un dial circular numerado del `0` al `99`.
El dial empieza en la posicion `50` y recibe una lista de rotaciones, una por linea.
Cada rotacion empieza por:

- `L`: giro hacia la izquierda, hacia numeros menores.
- `R`: giro hacia la derecha, hacia numeros mayores.

Despues aparece el numero de clicks que debe avanzar el dial. Como el dial es
circular, al girar a la izquierda desde `0` se pasa a `99`, y al girar a la derecha
desde `99` se pasa a `0`.

La entrada esta en:

```text
src/main/resources/input.txt
```

## Parte 1

En la primera parte se pide contar cuantas veces el dial queda apuntando a `0`
despues de completar una rotacion.

La solucion modela el dial como un objeto con estado interno:

```java
private int position = 50;
```

Cada rotacion actualiza la posicion usando aritmetica modular:

```java
position = (position - steps % 100 + 100) % 100;
position = (position + steps) % 100;
```

Despues de cada rotacion, `PasswordCalculatorPart1` comprueba si la posicion final
es `0` y, en ese caso, incrementa el contador.

Con el ejemplo del enunciado, la parte 1 devuelve `3`.

## Parte 2

En la segunda parte aparece un nuevo documento de seguridad. El metodo
`0x434C49434B` cambia la forma de calcular la password: ahora hay que contar el
numero de veces que cualquier click hace que el dial apunte a `0`, tanto si ocurre al
final de una rotacion como si ocurre durante la rotacion.

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

El dial termina en `0` tres veces, como en la parte 1. Ademas, durante algunas
rotaciones pasa por `0` otras tres veces:

- `L68`: pasa por `0` una vez durante la rotacion.
- `R60`: pasa por `0` una vez durante la rotacion.
- `L82`: pasa por `0` una vez durante la rotacion.

Por tanto, el resultado del ejemplo para la parte 2 es `6`.

`PasswordCalculatorPart2` cuenta los cruces por `0` antes de aplicar la rotacion al
dial. Para evitar simular click a click, calcula matematicamente cuantos ceros se
cruzan segun:

- posicion inicial del dial;
- direccion de la rotacion;
- numero total de clicks.

Ejemplo: desde `50`, una rotacion `R1000` pasa por `0` diez veces antes de volver a
`50`. Esto coincide con la advertencia del enunciado: no basta con comprobar la
posicion final de la rotacion.

Otro ejemplo mas pequeno: desde `50`, una rotacion `R250` pasa por `0` tres veces:

- al llegar a `0` por primera vez;
- tras una vuelta completa;
- tras otra vuelta completa.

Por eso el test de esa rotacion espera `3`.

## Diseno de clases

La solucion esta dividida en tres paquetes:

```text
application/
domain/
infrastructure/
```

### `domain`

Contiene las reglas del problema.

- `Dial`: representa el dial circular y encapsula su posicion.
- `Rotation`: representa una orden de giro con direccion y numero de pasos.
- `PasswordCalculatorPart1`: calcula la respuesta de la primera parte.
- `PasswordCalculatorPart2`: calcula la respuesta de la segunda parte.

### `application`

Coordina el caso de uso.

- `RotationParser`: transforma las lineas del fichero en objetos `Rotation`.
- `SafeSolver`: obtiene las lineas de entrada, las parsea y delega el calculo en la clase correspondiente.

### `infrastructure`

Contiene los detalles externos al dominio.

- `RotationSource`: interfaz para obtener las lineas de entrada.
- `FileRotationSource`: implementacion que lee las rotaciones desde un fichero.

## Principios de Ingenieria del Software II aplicados

### Abstraccion

La teoria define la abstraccion como ocultar detalles complejos detras de una interfaz
simple. En esta solucion, el dominio trabaja con conceptos del problema:

- dial;
- rotacion;
- calculador de password;
- fuente de rotaciones.

El resto del codigo no necesita conocer como se leen los ficheros ni como se calcula
internamente el modulo del dial.

### Diseno por contrato

`Rotation` actua como objeto de valor y valida su propio contrato:

```java
if (direction != 'L' && direction != 'R') {
    throw new IllegalArgumentException("Direction must be L or R");
}
if (steps < 0) {
    throw new IllegalArgumentException("Steps must be >= 0");
}
```

Esto deja claro que una rotacion valida solo puede tener direccion `L` o `R`, y que
los pasos no pueden ser negativos. El resto del dominio puede confiar en esa
precondicion.

### Alta cohesion y SRP

La teoria de cohesion y SRP indica que una clase debe tener una sola responsabilidad
y una unica razon para cambiar. En esta solucion:

- `Dial` solo sabe moverse y exponer su posicion.
- `RotationParser` solo parsea texto.
- `FileRotationSource` solo lee lineas de un fichero.
- `PasswordCalculatorPart1` solo resuelve la parte 1.
- `PasswordCalculatorPart2` solo resuelve la parte 2.
- `SafeSolver` solo coordina el flujo de la aplicacion.

Esto evita que una clase concentre lectura de ficheros, parseo, reglas del dial y
salida por consola.

### Bajo acoplamiento

`SafeSolver` depende de la abstraccion `RotationSource`, no directamente de
`FileRotationSource`:

```java
public SafeSolver(RotationSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la logica de aplicacion. Por
ejemplo, se podria crear una fuente en memoria para tests o una fuente que lea desde
red.

### Inversion de dependencias e inyeccion de dependencias

La dependencia de entrada se inyecta por constructor. Esto separa la creacion del
objeto concreto (`FileRotationSource`) de su uso (`SafeSolver`). Es una aplicacion
sencilla del principio de inversion de dependencias: la logica de alto nivel trabaja
contra una interfaz.

### Modularidad

La division en paquetes separa responsabilidades:

- `domain`: reglas puras del problema.
- `application`: orquestacion.
- `infrastructure`: detalles tecnicos de entrada.

Esto hace que el codigo sea mas facil de extender para otros dias o para nuevas
formas de entrada.

### Polimorfismo

El uso de la interfaz `RotationSource` permite tratar cualquier implementacion como
una fuente de rotaciones. `FileRotationSource` es la implementacion actual, pero el
codigo cliente solo necesita conocer el contrato de la interfaz.

## Patrones y tecnicas usadas

### Repository / Source

`RotationSource` funciona como una abstraccion de acceso a datos. No es un patron
GoF estricto, pero sigue la idea habitual de aislar el origen de los datos para que el
dominio no dependa del sistema de ficheros.

### Value Object

`Rotation` se modela como `record`, por lo que representa un dato del dominio con
identidad basada en sus valores (`direction` y `steps`). Ademas, concentra la
validacion de una rotacion valida.

### Service

`PasswordCalculatorPart1` y `PasswordCalculatorPart2` actuan como servicios de
dominio: no representan entidades con identidad propia, sino operaciones del dominio
que calculan el password a partir de una lista de rotaciones.

### Fachada de caso de uso

`SafeSolver` ofrece metodos simples (`solvePart1` y `solvePart2`) que ocultan los
pasos internos: leer entrada, parsear rotaciones y calcular la respuesta.

## Tests

Los tests estan en:

```text
src/test/java/domain/
```

Cubren:

- el ejemplo oficial de la parte 1, cuyo resultado esperado es `3`;
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `6`;
- el comportamiento de la parte 2 con varias vueltas completas (`R250` y `R1000`).

Para ejecutar los tests desde la raiz del repositorio:

```bash
mvn test
```

## Ejecucion

Desde la raiz del repositorio:

```bash
mvn -pl dia1 compile
```

Tambien se puede ejecutar `Main` desde IntelliJ. Si se ejecuta desde la carpeta raiz
`AOC`, el programa busca el input en:

```text
dia1/src/main/resources/input.txt
```

Si se ejecuta directamente desde `dia1`, lo busca en:

```text
src/main/resources/input.txt
```
