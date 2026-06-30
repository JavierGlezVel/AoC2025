# Día 3

## Problema

La entrada contiene varios bancos de baterías. Cada línea es un banco, y cada dígito
representa el joltage de una batería.

En cada banco hay que escoger algunas baterías. El número final se forma con los
dígitos escogidos, manteniendo el orden original.

La clave es que no se pueden mover las baterías de sitio. Se eligen algunas, pero el
número resultante conserva el orden en el que aparecen en la línea.

Por ejemplo, en el banco `12345`, si se encienden las baterías `2` y `4`, el banco
produce `24` jolts.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

Hay que escoger dos baterías por banco para formar el mayor número posible. Después
se suman los mejores valores de todos los bancos.

En esta parte cada banco produce un número de dos cifras. La solución busca la mejor
primera cifra posible y luego la mejor segunda cifra que pueda aparecer después.

Con el ejemplo oficial:

```text
987654321111111
811111111111119
234234234234278
818181911112111
```

Los mayores joltages son:

- `98`, a partir de `987654321111111`.
- `89`, a partir de `811111111111119`.
- `78`, a partir de `234234234234278`.
- `92`, a partir de `818181911112111`.

La suma total del ejemplo es:

```text
357
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
17034
```

## Parte 2

Ahora hay que escoger doce baterías por banco. La idea es la misma que en la parte 1,
pero el número final tiene doce dígitos.

Aunque el número de cifras cambia, el razonamiento sigue siendo el mismo: construir
el número más grande posible sin romper el orden original.

Con el mismo ejemplo oficial, los mayores joltages son:

- `987654321111`, a partir de `987654321111111`.
- `811111111119`, a partir de `811111111111119`.
- `434234234278`, a partir de `234234234234278`.
- `888911112111`, a partir de `818181911112111`.

La suma total del ejemplo es:

```text
3121910778619
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
168798209663590
```

## Enfoque de la solución

La solución usa la misma idea para las dos partes: escoger varios dígitos de una
línea para formar el número más grande posible.

La diferencia está solo en cuántos dígitos hay que escoger. En la parte 1 se escogen
`2`; en la parte 2 se escogen `12`. Por eso `MaximumJoltageCalculator` recibe ese
número como parámetro:

```java
new MaximumJoltageCalculator(2)
new MaximumJoltageCalculator(12)
```

El algoritmo va construyendo el número de izquierda a derecha. En cada paso busca el
mejor dígito que puede escoger sin quedarse sin dígitos para completar el resultado.

Por ejemplo, si todavía faltan varias cifras por elegir, no puede escoger un dígito
que esté demasiado al final de la línea. Tiene que dejar sitio para las cifras que
faltan.

```java
int searchEnd = ratings.length() - (batteriesToTurnOn - selected);
int bestIndex = searchStart;

for (int currentIndex = searchStart; currentIndex <= searchEnd; currentIndex++) {
    if (ratings.charAt(currentIndex) > ratings.charAt(bestIndex)) {
        bestIndex = currentIndex;
    }
}
```

Cuando encuentra el mejor dígito para esa posición, lo añade al resultado y sigue
buscando desde la posición siguiente. Así nunca cambia el orden original de las
baterías.

`TotalOutputJoltageCalculator` suma el resultado de aplicar un `JoltageCalculator` a
cada banco. Es decir, calcula el mejor valor de cada línea y luego suma todos esos
valores:

```java
return banks.stream()
        .mapToLong(joltageCalculator::calculate)
        .sum();
```


## Uso de Streams

En este día el stream principal está en `TotalOutputJoltageCalculator`. Su trabajo
es aplicar el calculador de joltage a cada banco y sumar los resultados.

```java
return banks.stream()
        .mapToLong(joltageCalculator::calculate)
        .sum();
```

El stream parte de `List<BatteryBank>`. `mapToLong(joltageCalculator::calculate)`
transforma cada banco en el valor numérico que produce la regla configurada. En la
parte 1 esa regla selecciona 2 baterías; en la parte 2 selecciona 12. Finalmente,
`sum()` acumula todos los joltages en un único total.

Este stream es importante para el diseño porque permite que la suma sea común a las
dos partes. El totalizador no sabe qué regla concreta se está usando; solo aplica el
contrato `JoltageCalculator` a cada elemento.

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

Contiene conceptos y servicios compartidos por ambas partes.

- `BatteryBank`: representa un banco de baterías y valida sus invariantes.
- `JoltageCalculator`: contrato para calcular el joltage de un banco.
- `MaximumJoltageCalculator`: calcula el mayor joltage posible escogiendo un número configurable de baterías.
- `TotalOutputJoltageCalculator`: suma los joltages de todos los bancos.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `TotalOutputJoltageCalculatorPart1`: configura el cálculo para encender 2 baterías.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `TotalOutputJoltageCalculatorPart2`: configura el cálculo para encender 12 baterías.

### `application`

Coordina el caso de uso.

- `BatteryBankParser`: transforma las líneas del fichero en objetos `BatteryBank`.
- `LobbySolver`: lee la entrada, la parsea y delega el cálculo de cada parte.

### `infrastructure`

Contiene los detalles externos al dominio.

- `BatteryBankSource`: interfaz para obtener las líneas de entrada.
- `FileBatteryBankSource`: implementación que lee los bancos desde un fichero.

## Clases principales

### `Main` - `Main.java`

1. Determina la ruta del input.
2. Crea `FileBatteryBankSource` y `LobbySolver`.
3. Imprime las respuestas de ambas partes.

### `LobbySolver` - `application/LobbySolver.java`

1. Lee las líneas de bancos de baterías.
2. Usa `BatteryBankParser` para construir el modelo.
3. Ejecuta `TotalOutputJoltageCalculatorPart1` o `TotalOutputJoltageCalculatorPart2`.

### `BatteryBankParser` - `application/BatteryBankParser.java`

1. Recorre las líneas del input.
2. Limpia cada línea.
3. Construye un `BatteryBank` por línea.

### `BatteryBank` - `domain/common/BatteryBank.java`

1. Representa la secuencia de valores de un banco.
2. Valida que haya suficientes baterías.
3. Garantiza que los valores sean dígitos válidos.

### `JoltageCalculator` - `domain/common/JoltageCalculator.java`

1. Define el contrato para calcular el joltage de un banco.
2. Permite intercambiar la estrategia de cálculo usada por el totalizador.

### `MaximumJoltageCalculator` - `domain/common/MaximumJoltageCalculator.java`

1. Recibe cuántas baterías deben seleccionarse.
2. Elige vorazmente los dígitos que forman el mayor número posible.
3. Devuelve el joltage máximo de un banco.

### `TotalOutputJoltageCalculator` - `domain/common/TotalOutputJoltageCalculator.java`

1. Recibe un `JoltageCalculator`.
2. Aplica esa estrategia a cada banco.
3. Suma todos los joltages obtenidos.

### `TotalOutputJoltageCalculatorPart1` - `domain/part1/TotalOutputJoltageCalculatorPart1.java`

1. Configura el cálculo con 2 baterías.
2. Delega la suma en `TotalOutputJoltageCalculator`.

### `TotalOutputJoltageCalculatorPart2` - `domain/part2/TotalOutputJoltageCalculatorPart2.java`

1. Configura el cálculo con 12 baterías.
2. Reutiliza el mismo totalizador común.

### `BatteryBankSource` - `infrastructure/BatteryBankSource.java`

1. Define cómo obtener las líneas de entrada.
2. Evita que el solver dependa de una fuente concreta.

### `FileBatteryBankSource` - `infrastructure/FileBatteryBankSource.java`

1. Guarda la ruta del input.
2. Lee las líneas del fichero.
3. Implementa `BatteryBankSource`.

## Flujo del programa

1. `Main` prepara `FileBatteryBankSource`.
2. `LobbySolver` lee las líneas y llama a `BatteryBankParser`.
3. Cada línea se convierte en un `BatteryBank` con la cadena de valores de batería.
4. La parte 1 usa `MaximumJoltageCalculator(2)` para formar el mayor voltaje con dos dígitos.
5. La parte 2 usa `MaximumJoltageCalculator(12)` para aplicar la misma estrategia con doce dígitos.
6. `TotalOutputJoltageCalculator` suma el resultado de todos los bancos.

```java
public TotalOutputJoltageCalculatorPart2() {
    this(new MaximumJoltageCalculator(12));
}
```

La elección de dígitos se hace manteniendo el orden original y buscando el mejor candidato posible en cada paso:

```java
for (int selected = 0; selected < batteriesToTurnOn; selected++) {
    int searchEnd = ratings.length() - (batteriesToTurnOn - selected);
    int bestIndex = searchStart;

    for (int currentIndex = searchStart; currentIndex <= searchEnd; currentIndex++) {
        if (ratings.charAt(currentIndex) > ratings.charAt(bestIndex)) {
            bestIndex = currentIndex;
        }
    }
}
```

## Fundamentos de diseño aplicados

### Alta Cohesión

`MaximumJoltageCalculator` se centra en elegir los mejores dígitos de un banco,
`TotalOutputJoltageCalculator` solo suma resultados y las clases de parte 1 y parte
2 solo configuran cuántas baterías se encienden. Cada clase tiene una tarea
relacionada con su nombre.

### Bajo Acoplamiento

El totalizador depende de `JoltageCalculator`, no de una implementación concreta.
`LobbySolver` depende de `BatteryBankSource`, por lo que no queda acoplado a
`FileBatteryBankSource`.

### Modularidad

La regla común de cálculo está en `domain/common`, mientras que cada parte queda en
su paquete específico. La aplicación y la infraestructura quedan fuera del dominio.

### Código Expresivo

`BatteryBank`, `MaximumJoltageCalculator` y `TotalOutputJoltageCalculator` describen
la intención del código. El método `calculate` se usa de forma consistente para los
servicios de dominio.

### Abstracción

`JoltageCalculator` abstrae la forma concreta de calcular el joltage de un banco.
Gracias a eso, el totalizador puede sumar resultados sin saber si se seleccionan 2,
12 u otra cantidad de baterías.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

Cada clase tiene una única razón principal para cambiar:

- `BatteryBankParser` parsea líneas.
- `BatteryBank` representa un banco de baterías.
- `MaximumJoltageCalculator` calcula el mayor joltage para un banco.
- `TotalOutputJoltageCalculator` suma resultados de varios bancos.
- `TotalOutputJoltageCalculatorPart1` configura la parte 1.
- `TotalOutputJoltageCalculatorPart2` configura la parte 2.
- `LobbySolver` coordina el caso de uso.

### Principio Abierto/Cerrado (OCP)

La parte 2 no modifica el totalizador común: crea otra configuración con `MaximumJoltageCalculator(12)`. Si aparece otra regla, puede añadirse otra implementación de `JoltageCalculator` o una nueva configuración sin tocar `TotalOutputJoltageCalculator`.

### Principio de Sustitución de Liskov (LSP)

`TotalOutputJoltageCalculator` trabaja con el contrato `JoltageCalculator`. Cualquier implementación que calcule un `long` a partir de un `BatteryBank` puede sustituir a `MaximumJoltageCalculator` sin romper la suma.

### Principio de Segregación de la Interfaz (ISP)

`JoltageCalculator` solo exige `calculate(BatteryBank bank)` y `BatteryBankSource` solo exige leer líneas. Las clases cliente no dependen de métodos que no utilizan.

### Principio de Inversión de Dependencias (DIP)

El totalizador depende de `JoltageCalculator`, no de una implementación concreta:

```java
public TotalOutputJoltageCalculator(JoltageCalculator joltageCalculator) {
    this.joltageCalculator = Objects.requireNonNull(joltageCalculator);
}
```

`LobbySolver` también depende de `BatteryBankSource`, no de `FileBatteryBankSource`.

### Principio de Composición sobre Herencia (COI)

La variación se resuelve componiendo `TotalOutputJoltageCalculator` con un `JoltageCalculator`. No hace falta heredar de una clase base para cambiar de 2 a 12 baterías.

### Principio DRY

La suma de los resultados de todos los bancos está en `TotalOutputJoltageCalculator`. Las partes 1 y 2 reutilizan ese recorrido y solo cambian la configuración del cálculo.

### Convención sobre Configuración (CoC)

Se mantiene la convención Maven de fuentes, recursos y tests. El módulo no necesita configuración especial para integrarse en el reactor del proyecto.

### Principio YAGNI

No se añade un framework de estrategias más complejo. La interfaz `JoltageCalculator` basta para expresar la variación real del problema.

## Patrones de diseño aplicados

### Creacionales

No se aplica ningún patrón creacional de forma explícita. No hace falta `Singleton`
porque no existe ningún recurso global que deba tener una única instancia, y tampoco
se usa `Factory Method` porque la creación de objetos es simple y directa.

### Estructurales

Se refleja `Adapter` en `FileBatteryBankSource`. La aplicación trabaja con
`BatteryBankSource`, mientras que `FileBatteryBankSource` adapta `Files.readAllLines`
a esa interfaz propia del proyecto.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`, por ejemplo
al recorrer las líneas de entrada y los bancos de baterías. En Java este recorrido se
apoya en `Iterable`/`Iterator`, aunque el código no cree el iterador manualmente.

No se aplica `Command`, porque no hay objetos que encapsulen acciones ejecutables.
Tampoco se aplica `Observer`, porque no hay suscripciones ni notificación de cambios.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de un banco por línea;
- el rechazo de ratings fuera del rango `1` a `9`;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `357`;
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `3121910778619`;
- el respeto del orden original de las baterías;
- el rechazo de bancos con menos baterías de las que exige la regla configurada;
- el uso de cualquier implementación que cumpla el contrato `JoltageCalculator`.
