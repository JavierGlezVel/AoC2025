# Día 2

## Problema

El problema ocurre en la tienda de regalos del Polo Norte. La base de datos contiene
rangos de IDs de producto, pero algunos IDs son inválidos porque siguen patrones
repetitivos.

La entrada es una única línea con rangos separados por comas:

```text
11-22,95-115,998-1012
```

Cada rango está formado por:

- primer ID del rango;
- guion `-`;
- último ID del rango.

El rango incluye ambos extremos. Por ejemplo, `11-22` incluye tanto `11` como `22`.

Los IDs no tienen ceros a la izquierda. Por eso `0101` no se considera un ID válido.

## Parte 1

En la primera parte, un ID es inválido si está formado por una secuencia de dígitos
repetida exactamente dos veces.

Ejemplos:

- `55` es inválido porque es `5` repetido dos veces.
- `6464` es inválido porque es `64` repetido dos veces.
- `123123` es inválido porque es `123` repetido dos veces.

El objetivo es encontrar todos los IDs inválidos que aparecen dentro de los rangos de
entrada y sumarlos.

Con el ejemplo oficial, la suma de la parte 1 es:

```text
1227775554
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
21139440284
```

## Parte 2

En la segunda parte, la regla se amplía. Ahora un ID es inválido si está formado por
una secuencia de dígitos repetida al menos dos veces.

Ejemplos:

- `12341234` es inválido porque `1234` se repite dos veces.
- `123123123` es inválido porque `123` se repite tres veces.
- `1212121212` es inválido porque `12` se repite cinco veces.
- `1111111` es inválido porque `1` se repite siete veces.

Esta regla incluye todos los casos de la parte 1, pero añade IDs con tres o más
repeticiones.

Con el ejemplo oficial, la suma de la parte 2 es:

```text
4174379265
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
38731915928
```

## Enfoque de la solución

Una solución directa sería recorrer todos los números de todos los rangos y comprobar
si cada número cumple el patrón. Esa opción es sencilla, pero no escala bien cuando
los rangos contienen millones o miles de millones de IDs.

La solución implementada hace lo contrario: genera solo los IDs que pueden ser
inválidos y después comprueba si están dentro de alguno de los rangos.

### Generación de candidatos para la parte 1

`RepeatedTwiceProductIdGenerator` genera candidatos repitiendo un prefijo
exactamente dos veces:

```text
1   -> 11
2   -> 22
64  -> 6464
123 -> 123123
```

Después, `InvalidProductIdSumCalculatorPart1` usa esos candidatos para calcular la
suma de los que aparecen en los rangos.

### Generación de candidatos para la parte 2

`RepeatedAtLeastTwiceProductIdGenerator` generaliza la idea anterior. Toma un bloque
de dígitos y lo repite dos, tres, cuatro o más veces mientras el número generado no
supere el mayor ID de la entrada.

Por ejemplo, con el bloque `12` se generan:

```text
1212
121212
12121212
```

Los candidatos se guardan en un `Set` para evitar duplicados. Esto es necesario
porque un mismo número puede cumplir la regla de más de una forma. Por ejemplo,
`1111` puede verse como `11` repetido dos veces o como `1` repetido cuatro veces,
pero solo debe sumarse una vez.

## Diseño de clases

La solución está dividida en tres paquetes:

```text
application/
domain/
infrastructure/
```

### `domain`

Contiene las reglas del problema.

- `ProductIdRange`: representa un rango cerrado de IDs.
- `RepeatedTwiceProductIdGenerator`: genera IDs formados por un bloque repetido exactamente dos veces.
- `RepeatedAtLeastTwiceProductIdGenerator`: genera IDs formados por un bloque repetido dos o más veces.
- `InvalidProductIdSumCalculator`: contiene la lógica común para sumar candidatos dentro de los rangos.
- `InvalidProductIdSumCalculatorPart1`: calcula la suma de IDs inválidos de la parte 1.
- `InvalidProductIdSumCalculatorPart2`: calcula la suma de IDs inválidos de la parte 2.

### `application`

Coordina el caso de uso.

- `ProductIdRangeParser`: transforma la línea de entrada en objetos `ProductIdRange`.
- `GiftShopSolver`: lee la entrada, la parsea y delega el cálculo de cada parte.

### `infrastructure`

Contiene los detalles externos al dominio.

- `RangeSource`: interfaz para obtener el contenido de entrada.
- `FileRangeSource`: implementación que lee la entrada desde un fichero.

## Principios aplicados

### Abstracción

La abstracción consiste en trabajar con conceptos relevantes del problema sin exponer
detalles innecesarios. En esta solución se trabaja con rangos, fuentes de entrada,
generadores de candidatos y calculadores de suma.

`ProductIdRange` oculta la comprobación de límites detrás del método `contains`:

```java
public boolean contains(long id) {
    return firstId <= id && id <= lastId;
}
```

Quien usa un rango no necesita saber cómo se comparan internamente los límites.

### Diseño por contrato

`ProductIdRange` valida sus invariantes al construirse:

```java
if (firstId < 0 || lastId < 0) {
    throw new IllegalArgumentException("Range limits must be >= 0");
}
if (firstId > lastId) {
    throw new IllegalArgumentException("First ID must be <= last ID");
}
```

Así, el resto del dominio puede confiar en que todo rango tiene límites no negativos
y que el inicio no es mayor que el final.

### Alta cohesión y SRP

Cada clase tiene una responsabilidad concreta:

- `ProductIdRangeParser` solo parsea la entrada.
- `ProductIdRange` solo representa y valida un rango.
- `RepeatedTwiceProductIdGenerator` solo genera candidatos de la parte 1.
- `RepeatedAtLeastTwiceProductIdGenerator` solo genera candidatos de la parte 2.
- `InvalidProductIdSumCalculator` solo suma candidatos que aparecen en los rangos.
- `InvalidProductIdSumCalculatorPart1` solo resuelve la parte 1.
- `InvalidProductIdSumCalculatorPart2` solo resuelve la parte 2.
- `FileRangeSource` solo lee el fichero.
- `GiftShopSolver` solo coordina el caso de uso.

Esta separación evita mezclar parseo, lectura de ficheros, generación de candidatos y
cálculo de resultados en una misma clase.

### Bajo acoplamiento

`GiftShopSolver` depende de la interfaz `RangeSource`, no directamente de
`FileRangeSource`:

```java
public GiftShopSolver(RangeSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la lógica de aplicación. Por
ejemplo, se podría crear una fuente en memoria para tests o una fuente que obtenga
los datos desde otro sistema.

### Inversión e inyección de dependencias

La lógica de alto nivel (`GiftShopSolver`) depende de una abstracción (`RangeSource`).
La implementación concreta se crea fuera y se inyecta por constructor.

Esto separa la creación de objetos de su uso y facilita probar el caso de uso con
distintas fuentes de datos.

### Modularidad

La división en paquetes separa las reglas del problema, la coordinación de la
aplicación y los detalles técnicos de entrada.

Esta estructura permite añadir nuevas reglas, como la parte 2, sin modificar piezas
que no tienen relación con esa regla.

## Patrones y técnicas usadas

### Source

`RangeSource` actúa como abstracción del origen de datos. El dominio no depende de
si la entrada viene de un fichero, de memoria o de cualquier otra fuente.

### Value Object

`ProductIdRange` se modela como `record`, por lo que representa un valor del
dominio definido por sus datos (`firstId` y `lastId`). Además, valida sus invariantes al
construirse.

### Service

`InvalidProductIdSumCalculatorPart1`, `InvalidProductIdSumCalculatorPart2` e
`InvalidProductIdSumCalculator` actúan como servicios de dominio: reciben datos del
problema y devuelven resultados calculados, sin representar entidades con identidad
propia.

### Generador de candidatos

`RepeatedTwiceProductIdGenerator` y `RepeatedAtLeastTwiceProductIdGenerator`
encapsulan las estrategias de generación de candidatos inválidos. Esto evita que los
calculadores conozcan los detalles de construcción de candidatos.

### Fachada de caso de uso

`GiftShopSolver` ofrece métodos simples (`solvePart1` y `solvePart2`) que ocultan los
pasos internos: leer entrada, parsear rangos y calcular la suma.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de rangos separados por comas;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `1227775554`;
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `4174379265`;
- la generación de IDs repetidos al menos dos veces;
- el caso de rangos solapados, para no sumar dos veces el mismo ID inválido.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia2 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia2 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 21139440284
Parte 2: 38731915928
```

También se puede ejecutar `Main` desde IntelliJ. Si se ejecuta desde la carpeta raíz
`AOC`, el programa busca el input en:

```text
dia2/src/main/resources/input.txt
```

Si se ejecuta directamente desde `dia2`, lo busca en:

```text
src/main/resources/input.txt
```
