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

## Resolución detallada

### Parte 1

La primera parte interpreta cada rotación como una actualización de posición en un
dial circular de 100 posiciones. El dial empieza en `50`; después de cada rotación
se comprueba si la posición final es `0`. Solo cuentan los ceros que aparecen al
terminar una instrucción, no los que se cruzan por el camino.

La operación circular se encapsula en `Dial`, por lo que el cálculo de la contraseña
no necesita saber cómo se normaliza una posición negativa o una vuelta completa:

```java
public void rotate(Rotation r) {
    int steps = r.steps();
    switch (r.direction()) {
        case 'L':
            position = (position - steps % 100 + 100) % 100;
            break;
        case 'R':
            position = (position + steps) % 100;
            break;
    }
}
```

Con esa abstracción, la parte 1 queda como una lectura secuencial del enunciado:
rotar, mirar la posición final y sumar si es cero.

```java
public int calculate(List<Rotation> rotations) {
    Dial dial = new Dial();
    int count = 0;

    for (Rotation r : rotations) {
        dial.rotate(r);
        if (dial.getPosition() == 0) {
            count++;
        }
    }

    return count;
}
```

### Parte 2

La segunda parte cambia la regla de conteo: ahora cuenta cada click que deja el
dial en `0`, aunque ese cero ocurra durante la rotación. La solución evita simular
click a click, porque una instrucción como `R1000` tendría muchos pasos. En su
lugar calcula cuántas veces se cruza el cero mediante aritmética modular.

El método recibe la posición inicial de la instrucción y calcula la distancia hasta
el primer cero. Si la rotación alcanza ese primer cero, cada 100 pasos adicionales
vuelve a pasar por `0`:

```java
private int countZeros(int start, Rotation r) {
    int steps = r.steps();
    if (steps <= 0) {
        return 0;
    }
    if (r.direction() == 'L') {
        if (start == 0) {
            return steps / 100;
        }
        if (steps < start) {
            return 0;
        }
        return 1 + (steps - start) / 100;
    }

    if (start == 0) {
        return steps / 100;
    }
    int toZero = 100 - start;
    if (steps < toZero) {
        return 0;
    }
    return 1 + (steps - toZero) / 100;
}
```

Después de contar los ceros intermedios de la instrucción, se rota el dial para que
la siguiente instrucción parta de la posición correcta:

```java
for (Rotation r : rotations) {
    count += countZeros(dial.getPosition(), r);
    dial.rotate(r);
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

Contiene conceptos compartidos por ambas partes.

- `Dial`: representa el dial circular y encapsula su posición.
- `Rotation`: representa una orden de giro con dirección y número de pasos.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `PasswordCalculatorPart1`: calcula la respuesta de la primera parte.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `PasswordCalculatorPart2`: calcula la respuesta de la segunda parte.

### `application`

Coordina el caso de uso.

- `RotationParser`: transforma las líneas del fichero en objetos `Rotation`.
- `SafeSolver`: obtiene las líneas de entrada, las parsea y delega el cálculo en la clase correspondiente.

### `infrastructure`

Contiene los detalles externos al dominio.

- `RotationSource`: interfaz para obtener las líneas de entrada.
- `FileRotationSource`: implementación que lee las rotaciones desde un fichero.

## Clases principales

### `Main` - `dia1/src/main/java/Main.java`

1. Calcula la ruta del input por defecto.
2. Crea `FileRotationSource` y `SafeSolver`.
3. Ejecuta ambas partes y muestra los resultados por consola.

### `SafeSolver` - `dia1/src/main/java/application/SafeSolver.java`

1. Pide las líneas de entrada a `RotationSource`.
2. Usa `RotationParser` para convertirlas en `Rotation`.
3. Llama a `PasswordCalculatorPart1` o `PasswordCalculatorPart2` según la parte.

### `RotationParser` - `dia1/src/main/java/application/RotationParser.java`

1. Recorre cada línea del input.
2. Extrae la dirección (`L` o `R`) y el número de pasos.
3. Construye una lista de objetos `Rotation`.

### `Dial` - `dia1/src/main/java/domain/common/Dial.java`

1. Guarda la posición actual del dial, que empieza en `50`.
2. Aplica rotaciones hacia izquierda o derecha.
3. Normaliza la posición para mantenerse entre `0` y `99`.

### `Rotation` - `dia1/src/main/java/domain/common/Rotation.java`

1. Representa una instrucción de giro.
2. Valida que la dirección sea `L` o `R`.
3. Valida que los pasos no sean negativos.

### `PasswordCalculatorPart1` - `dia1/src/main/java/domain/part1/PasswordCalculatorPart1.java`

1. Recorre las rotaciones en orden.
2. Gira el dial una vez por instrucción.
3. Cuenta cuántas veces la posición final queda en `0`.

### `PasswordCalculatorPart2` - `dia1/src/main/java/domain/part2/PasswordCalculatorPart2.java`

1. Recorre las rotaciones en orden.
2. Calcula cuántas veces se cruza el `0` durante cada giro.
3. Actualiza el dial para que la siguiente instrucción empiece en la posición correcta.

### `RotationSource` - `dia1/src/main/java/infrastructure/RotationSource.java`

1. Define la abstracción para obtener líneas de entrada.
2. Permite que el solver no dependa directamente de ficheros.

### `FileRotationSource` - `dia1/src/main/java/infrastructure/FileRotationSource.java`

1. Guarda la ruta del input.
2. Lee todas las líneas del fichero.
3. Adapta la API de ficheros de Java a la interfaz `RotationSource`.

## Flujo del programa

1. `Main` decide la ruta del input y crea `FileRotationSource`.
2. `SafeSolver` recibe la fuente como `RotationSource`, lee las líneas y usa `RotationParser`.
3. `RotationParser` transforma textos como `L68` o `R48` en objetos `Rotation`.
4. Para la parte 1, `PasswordCalculatorPart1` gira el `Dial` y cuenta si el dial termina en `0`.
5. Para la parte 2, `PasswordCalculatorPart2` cuenta primero cuántas veces se cruza el `0` durante la rotación y después actualiza el dial.

```java
var lines = source.getLines();
var rotations = parser.parse(lines);
return new PasswordCalculatorPart1().calculate(rotations);
```

La diferencia entre ambas partes está en qué se considera válido para sumar al contador:

```java
// Parte 1: solo cuenta el final de cada rotación.
dial.rotate(r);
if (dial.getPosition() == 0) {
    count++;
}

// Parte 2: cuenta todos los pasos que pasan por 0.
count += countZeros(dial.getPosition(), r);
dial.rotate(r);
```

## Fundamentos de diseño aplicados

### Alta Cohesión

Cada clase se centra en una tarea concreta del problema. `Dial` solo gestiona la
posición circular, `Rotation` solo representa una instrucción válida,
`RotationParser` solo convierte texto en rotaciones y cada calculador resuelve una
parte. Las responsabilidades relacionadas están juntas y no se mezclan con lectura
de ficheros o salida por consola.

### Bajo Acoplamiento

`SafeSolver` depende de `RotationSource`, no de `FileRotationSource`. Así, la lógica
que resuelve el día no queda atada a que la entrada venga de un fichero concreto.
También los calculadores dependen de `Rotation` y `Dial`, no del parser ni de la
infraestructura.

### Modularidad

El código está dividido en módulos por responsabilidad: `domain/common` contiene las
piezas compartidas, `domain/part1` y `domain/part2` separan las reglas de cada parte,
`application` coordina el caso de uso e `infrastructure` lee la entrada.

### Código Expresivo

Los nombres reflejan directamente el vocabulario del enunciado: `Dial`,
`Rotation`, `PasswordCalculatorPart1` y `PasswordCalculatorPart2`. Esto permite leer
la solución entendiendo qué papel cumple cada clase sin depender de comentarios
extensos.

### Abstracción

El giro circular se oculta detrás de `Dial.rotate`, y la entrada se oculta detrás de
`RotationSource`. Quien calcula la contraseña no necesita conocer la fórmula modular
ni cómo se leen las líneas del fichero.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

Cada clase tiene una única razón principal para cambiar:

- `Dial` cambiaría si cambia la mecánica del dial.
- `Rotation` cambiaría si cambia la representación de una rotación.
- `RotationParser` cambiaría si cambia el formato textual de entrada.
- `FileRotationSource` cambiaría si cambia la lectura desde fichero.
- `PasswordCalculatorPart1` y `PasswordCalculatorPart2` cambiarían si cambia la regla de cada parte.
- `SafeSolver` cambiaría si cambia la coordinación del caso de uso.

No hay una clase que mezcle lectura, parseo, aritmética circular y cálculo de las dos contraseñas.

### Principio Abierto/Cerrado (OCP)

La parte 2 se añadió como `PasswordCalculatorPart2` sin modificar `PasswordCalculatorPart1`, `Dial` ni `Rotation`. El diseño queda abierto a nuevas reglas de cálculo creando otra clase calculadora, pero las piezas comunes quedan cerradas frente a cambios innecesarios.

También se puede añadir otra fuente de entrada implementando `RotationSource` sin tocar `SafeSolver`.

### Principio de Sustitución de Liskov (LSP)

`SafeSolver` trabaja con el tipo base `RotationSource`. Cualquier implementación que respete ese contrato puede sustituir a `FileRotationSource` sin romper el solver, por ejemplo una fuente en memoria para tests.

### Principio de Segregación de la Interfaz (ISP)

`RotationSource` es una interfaz pequeña y específica: solo expone `readLines()`. Las clases que la implementan no están obligadas a soportar operaciones que el solver no necesita.

### Principio de Inversión de Dependencias (DIP)

`SafeSolver`, que contiene la lógica de alto nivel del caso de uso, depende de la abstracción `RotationSource` y no directamente de `FileRotationSource`:

```java
public SafeSolver(RotationSource source) {
    this.source = source;
}
```

La implementación concreta se crea fuera y se inyecta por constructor.

### Principio de Composición sobre Herencia (COI)

La solución compone objetos (`SafeSolver` usa una fuente, un parser y calculadores) en lugar de crear una jerarquía de solvers o calculadoras base. La variación entre partes se expresa con clases concretas, no con herencia artificial.

### Principio DRY

La aritmética circular del dial vive en `Dial` y la validación de cada instrucción vive en `Rotation`. Las dos partes reutilizan esas clases y no duplican las reglas de giro ni el formato de una rotación válida.

### Convención sobre Configuración (CoC)

El módulo sigue las convenciones de Maven: código en `src/main/java`, tests en `src/test/java` y entrada en `src/main/resources`. Gracias a eso no hace falta configurar rutas especiales para compilar o ejecutar tests.

### Principio YAGNI

No se añaden jerarquías, factories ni abstracciones generales para futuros diales que no existen en el enunciado. El diseño incluye solo las piezas necesarias para resolver las dos partes.

## Patrones de diseño aplicados

### Creacionales

No se aplica ningún patrón creacional de forma explícita. No hace falta `Singleton`
porque no existe ningún recurso global que deba tener una única instancia, y tampoco
se usa `Factory Method` porque la creación de objetos es simple y directa.

### Estructurales

Se refleja `Adapter` en `FileRotationSource`.

`SafeSolver` trabaja con la interfaz `RotationSource`, que representa lo que la
aplicación necesita: obtener líneas de rotaciones. `FileRotationSource` adapta el
sistema de ficheros (`Files.readAllLines`) a esa interfaz propia del proyecto.

```java
public interface RotationSource {
    List<String> getLines() throws IOException;
}
```

Así, la aplicación no depende directamente de `java.nio.file.Files`, sino de una
abstracción del dominio de entrada.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`. Por ejemplo,
los calculadores recorren una `List<Rotation>` sin conocer su representación interna:

```java
for (Rotation r : rotations) {
    dial.rotate(r);
    if (dial.getPosition() == 0) {
        count++;
    }
}
```

En Java, este recorrido se apoya en `Iterable`/`Iterator`, aunque el código no cree el
iterador manualmente.

No se aplica `Command`: `Rotation` representa una instrucción del problema, pero no
encapsula una acción ejecutable con un método tipo `execute` ni desacopla un emisor
de un receptor. Tampoco se aplica `Observer`, porque no hay suscripciones ni
notificación de cambios entre objetos.

## Tests

Los tests están en:

```text
src/test/java/domain/part1/
src/test/java/domain/part2/
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
