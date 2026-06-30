# Día 10

## Problema

La entrada contiene varias máquinas, una por línea. Cada máquina tiene:

- un diagrama de luces entre corchetes, donde `.` significa apagada y `#` encendida;
- uno o más botones entre paréntesis, indicando qué luces conmuta cada botón;
- requisitos de joltage entre llaves, que en la parte 1 se ignoran.

Un ejemplo de línea es:

```text
[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
```

El objetivo es calcular cuántas pulsaciones hacen falta como mínimo.

Cada parte interpreta la máquina de una forma distinta, pero en ambos casos se busca
minimizar el número total de veces que se pulsan botones.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

Las luces empiezan apagadas. Cada botón cambia algunas luces de apagadas a encendidas
o al revés. Hay que llegar al patrón objetivo con el menor número de pulsaciones.

Como pulsar dos veces el mismo botón cancela su efecto, en esta parte cada botón solo
interesa como usado o no usado.

Con el ejemplo oficial:

```text
[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
[...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
[.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
```

El resultado es:

```text
7
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
545
```

## Parte 2

Ahora se ignoran las luces y se usan los requisitos de joltage. Cada botón suma `1`
a ciertos contadores, y puede pulsarse varias veces. Hay que alcanzar los valores
pedidos con el menor total de pulsaciones.

Esta parte ya no es de encender y apagar, sino de repartir incrementos entre varios
contadores hasta llegar exactamente a los valores pedidos.

Con el ejemplo oficial, el resultado es:

```text
33
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
22430
```

## Enfoque de la solución

Primero, `FactoryMachineParser` lee cada línea y la convierte en una `FactoryMachine`.
Esa clase guarda tres cosas: el objetivo de luces, los botones disponibles y los
requisitos de joltage.

Para la parte 1, las luces y los botones se guardan como máscaras de bits. Dicho de
forma simple, una máscara es un número que representa qué luces están encendidas.
Esto permite combinar botones rápido.

`MinimumButtonPressesCalculatorPart1` prueba todos los subconjuntos de botones de
cada máquina. Cada subconjunto significa: estos botones se pulsan una vez y estos no.
Para combinar el efecto de los botones usa XOR:

```java
currentMask ^= machine.buttonMasks().get(button);
```

Si al final las luces coinciden con el objetivo, se guarda cuántos botones se han
usado. La solución se queda con la opción que usa menos botones.

Para la parte 2, los botones ya no solo se pulsan una vez: pueden pulsarse muchas
veces. Cada botón suma `1` a algunos contadores de joltage.

La solución traduce eso a cuentas matemáticas: cuántas veces pulso cada botón para
llegar a los valores pedidos. Después busca una combinación válida y se queda con la
que usa menos pulsaciones.

Aunque internamente se usa álgebra con fracciones para hacerlo exacto, la idea del
problema es sencilla: encontrar cuántas veces hay que pulsar cada botón para que
todos los contadores lleguen justo al valor que pide la máquina.


## Uso de Streams

En este día los Streams se usan para parsear máquinas y para sumar el resultado de
cada máquina en las dos partes.

El parser convierte cada línea del manual en un `FactoryMachine`:

```java
return lines.stream()
        .map(this::parseLine)
        .toList();
```

El stream recorre las líneas de entrada. `map(this::parseLine)` transforma cada línea
en una máquina validada, y `toList()` devuelve la lista final que usan los
calculadores.

La parte 1 suma el mínimo de pulsaciones de todas las máquinas:

```java
return machines.stream()
        .mapToInt(this::minimumPresses)
        .sum();
```

`mapToInt` aplica el algoritmo de enumeración de subconjuntos a cada máquina y
obtiene un entero. `sum()` suma esos mínimos para producir la respuesta de la parte 1.

La parte 2 usa la misma idea, pero con `long`, porque el sistema de voltajes puede
producir resultados mayores:

```java
return machines.stream()
        .mapToLong(this::minimumPresses)
        .sum();
```

Cada elemento se transforma en el mínimo de pulsaciones calculado mediante el sistema
lineal, y `sum()` acumula el total de todas las máquinas.

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

- `FactoryMachine`: representa una máquina mediante número de luces, máscara
  objetivo, máscaras de botones y requisitos de joltage.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `MinimumButtonPressesCalculatorPart1`: calcula el menor número total de
  pulsaciones.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `MinimumJoltageButtonPressesCalculatorPart2`: calcula el menor número total de
  pulsaciones para alcanzar los requisitos de joltage.

### `application`

Coordina el caso de uso.

- `FactoryMachineParser`: transforma las líneas del fichero en máquinas del dominio.
- `FactorySolver`: lee la entrada, la parsea y delega el cálculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `FactoryMachineSource`: interfaz para obtener las líneas de entrada.
- `FileFactoryMachineSource`: implementación que lee las máquinas desde un fichero.

## Clases principales

### `Main` - `Main.java`

1. Calcula la ruta del input del día 10.
2. Crea `FileFactoryMachineSource` y `FactorySolver`.
3. Ejecuta las dos partes y muestra los resultados.

### `FactorySolver` - `application/FactorySolver.java`

1. Lee el input mediante `FactoryMachineSource`.
2. Convierte los bloques de texto en máquinas con `FactoryMachineParser`.
3. Llama a la calculadora de la parte 1 o de la parte 2 según corresponda.

### `FactoryMachineParser` - `application/FactoryMachineParser.java`

1. Divide el input en bloques, uno por máquina.
2. Extrae botones, máscaras de contadores y requisitos de voltaje.
3. Construye objetos `FactoryMachine` con la información ya validada.

### `FactoryMachine` - `domain/common/FactoryMachine.java`

1. Representa una máquina con sus botones y contadores.
2. Guarda qué contadores afecta cada botón.
3. Expone los datos necesarios para calcular el mínimo de pulsaciones.

### `MinimumButtonPressesCalculatorPart1` - `domain/part1/MinimumButtonPressesCalculatorPart1.java`

1. Recorre las máquinas del input.
2. Busca combinaciones de pulsaciones que enciendan todos los contadores.
3. Suma el mínimo de pulsaciones necesario para cada máquina.

### `MinimumJoltageButtonPressesCalculatorPart2` - `domain/part2/MinimumJoltageButtonPressesCalculatorPart2.java`

1. Traduce cada máquina a un sistema lineal de ecuaciones.
2. Reduce la matriz para separar columnas pivote y variables libres.
3. Enumera soluciones enteras no negativas y se queda con la de menor coste.

### `LinearSystem` - `domain/part2/MinimumJoltageButtonPressesCalculatorPart2.java`

1. Guarda la matriz reducida del sistema.
2. Conserva las columnas pivote y las columnas libres.
3. Permite evaluar soluciones candidatas sin recalcular la reducción.

### `Fraction` - `domain/part2/MinimumJoltageButtonPressesCalculatorPart2.java`

1. Representa números racionales exactos durante la reducción.
2. Normaliza signo y divisor común.
3. Evita errores de redondeo al comprobar si una solución es entera.

### `FactoryMachineSource` - `infrastructure/FactoryMachineSource.java`

1. Define cómo obtener las líneas del input.
2. Desacopla el solver del origen físico de los datos.

### `FileFactoryMachineSource` - `infrastructure/FileFactoryMachineSource.java`

1. Guarda la ruta del fichero de entrada.
2. Lee sus líneas desde disco.
3. Implementa `FactoryMachineSource` para integrarse con la capa de aplicación.

## Flujo del programa

1. `Main` crea `FileFactoryMachineSource`.
2. `FactorySolver` lee el input y `FactoryMachineParser` construye una lista de `FactoryMachine`.
3. La parte 1 prueba combinaciones de botones usando máscaras de bits y se queda con la combinación con menos pulsaciones.
4. La parte 2 convierte cada máquina en un sistema lineal donde cada botón aporta a ciertos contadores.
5. `MinimumJoltageButtonPressesCalculatorPart2` reduce la matriz con fracciones exactas, enumera variables libres y valida soluciones enteras no negativas.

```java
var machines = parser.parse(source.getLines());
return new MinimumJoltageButtonPressesCalculatorPart2().calculate(machines);
```

En la parte 1, cada combinación se representa como bits activados:

```java
for (int button = 0; button < buttonCount; button++) {
    if ((combination & (1 << button)) != 0) {
        currentMask ^= machine.buttonMasks().get(button);
    }
}
```

En la parte 2, `Fraction` evita redondeos al decidir si una solución es válida:

```java
if (!value.isInteger() || value.isNegative()) {
    return NO_SOLUTION;
}
```

## Fundamentos de diseño aplicados

### Alta Cohesión

`FactoryMachine` representa una máquina validada, `MinimumButtonPressesCalculatorPart1`
enumera combinaciones de botones y `MinimumJoltageButtonPressesCalculatorPart2`
resuelve el sistema de voltajes. El parser solo interpreta el formato textual.

### Bajo Acoplamiento

`FactorySolver` depende de `FactoryMachineSource`. Las calculadoras reciben
`FactoryMachine` y no conocen cómo se obtiene ni cómo se parsea el manual.

### Modularidad

La representación común de máquinas está separada de los dos algoritmos. La
eliminación gaussiana queda encapsulada dentro de la parte 2 y no afecta a la parte 1.

### Código Expresivo

Nombres como `targetMask`, `buttonMasks`, `joltageRequirements`,
`pivotColumns` y `freeColumns` hacen visible la estructura del problema y del sistema
lineal.

### Abstracción

`FactoryMachine` oculta la validación de máscaras y requisitos. `Fraction` oculta la
aritmética exacta necesaria para la eliminación gaussiana.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

`FactoryMachineParser` parsea máquinas, `FactoryMachine` representa una máquina válida, `MinimumButtonPressesCalculatorPart1` enumera combinaciones de botones, `MinimumJoltageButtonPressesCalculatorPart2` resuelve el sistema de voltajes y `FactorySolver` coordina.

### Principio Abierto/Cerrado (OCP)

La parte 2 añade una resolución algebraica nueva sin modificar la calculadora de la parte 1. `FactoryMachine` queda como modelo común estable para ambas reglas.

### Principio de Sustitución de Liskov (LSP)

`FactorySolver` depende de `FactoryMachineSource`. Una fuente alternativa compatible puede reemplazar a `FileFactoryMachineSource`.

### Principio de Segregación de la Interfaz (ISP)

`FactoryMachineSource` solo obliga a leer líneas. La interfaz no mezcla responsabilidades de parseo, resolución o salida.

### Principio de Inversión de Dependencias (DIP)

La capa de aplicación recibe una abstracción:

```java
public FactorySolver(FactoryMachineSource source) {
    this.source = source;
}
```

Así, la lógica de alto nivel no depende de la lectura concreta desde fichero.

### Principio de Composición sobre Herencia (COI)

El solver compone el modelo común con calculadoras concretas para cada parte. No se usa herencia para compartir comportamiento entre algoritmos tan distintos.

### Principio DRY

`FactoryMachine` centraliza luces, botones, máscaras y requisitos. Las dos partes reutilizan esa representación y no duplican validaciones del manual.

### Convención sobre Configuración (CoC)

La organización Maven del módulo permite compilar, probar y cargar recursos mediante convenciones, sin configuración manual por día.

### Principio YAGNI

No se implementa un solucionador matemático genérico. La eliminación gaussiana y las fracciones exactas se incluyen porque la parte 2 las necesita, pero no se generalizan más allá del problema.

## Patrones de diseño aplicados

### Creacionales

Se refleja `Factory Method` en el método `Fraction.of(...)` usado por la parte 2. En
vez de crear directamente una fracción para representar un entero con
`new Fraction(value, 1)`, el código usa un método estático que encapsula esa creación:

```java
static Fraction of(long value) {
    return new Fraction(value, 1);
}
```

No se aplica `Singleton`, porque no existe ningún recurso global que deba tener una
única instancia.

### Estructurales

Se refleja `Adapter` en `FileFactoryMachineSource`. La aplicación trabaja con
`FactoryMachineSource`, mientras que `FileFactoryMachineSource` adapta
`Files.readAllLines` a esa interfaz propia del proyecto.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`, por ejemplo
al recorrer máquinas, botones y requisitos. En Java este recorrido se apoya en
`Iterable`/`Iterator`, aunque el código no cree el iterador manualmente.

No se aplica `Command`, porque los botones se modelan como datos de la máquina y no
como objetos que encapsulen una acción ejecutable. Tampoco se aplica `Observer`,
porque no hay suscripciones ni notificación de cambios.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de una máquina válida;
- el rechazo de líneas inválidas;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `7`;
- que no hace falta pulsar un botón más de una vez para configurar solo luces.
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `33`;
- que en la parte 2 un botón pueda pulsarse varias veces.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia10 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia10 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 545
Parte 2: 22430
```
