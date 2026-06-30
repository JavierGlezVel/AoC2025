# Día 5

## Problema

El problema ocurre en la cafetería. Los Elfos tienen una base de datos del sistema de
inventario con dos secciones:

- una lista de rangos de IDs de ingredientes frescos;
- una línea en blanco;
- una lista de IDs de ingredientes disponibles.

Cada rango es inclusivo. Por ejemplo, `3-5` indica que los IDs `3`, `4` y `5` son
frescos. Los rangos pueden solaparse; un ingrediente es fresco si pertenece a
cualquiera de los rangos.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

El objetivo es contar cuántos IDs disponibles son frescos.

Con el ejemplo oficial:

```text
3-5
10-14
16-20
12-18

1
5
8
11
17
32
```

Los IDs frescos disponibles son `5`, `11` y `17`, así que el resultado del ejemplo es:

```text
3
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
640
```

## Parte 2

En la segunda parte, la lista de IDs disponibles deja de importar. Ahora hay que
contar cuántos IDs distintos quedan cubiertos por los rangos frescos.

Con el ejemplo oficial, los rangos:

```text
3-5
10-14
16-20
12-18
```

cubren los IDs `3`, `4`, `5`, `10`, `11`, `12`, `13`, `14`, `15`, `16`, `17`,
`18`, `19` y `20`. Por tanto, el resultado del ejemplo es:

```text
14
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
365804144481581
```

## Enfoque de la solución

`InventoryDatabaseParser` separa la entrada en dos secciones usando la línea en
blanco. Antes de esa línea parsea rangos; después de esa línea parsea IDs
disponibles.

`FreshIngredientCounterPart1` recorre los IDs disponibles y comprueba si cada uno
pertenece a algún rango fresco:

```java
for (FreshIngredientIdRange range : database.freshRanges()) {
    if (range.contains(ingredientId)) {
        return true;
    }
}
```

Como la parte 1 solo pide clasificar los IDs disponibles, no hace falta expandir los
rangos ni generar todos los IDs frescos. Esto evita trabajar con intervalos enormes.

Para la parte 2 tampoco se expanden los rangos. `FreshIngredientIdCoverageCounterPart2`
ordena los rangos por inicio, fusiona los que se solapan o se tocan, y suma la
longitud inclusiva de los intervalos resultantes:

```java
if (currentRange.overlapsOrTouches(range)) {
    currentRange = currentRange.merge(range);
} else {
    freshIngredientIds += currentRange.size();
    currentRange = range;
}
```

Así, un ID cubierto por varios rangos se cuenta una sola vez.

## Resolución detallada

### Parte 1

La primera parte recibe una lista de IDs disponibles y una lista de rangos frescos.
La solución comprueba cada ID disponible contra todos los rangos y cuenta cuántos
pertenecen al menos a uno. La lógica de pertenencia está encapsulada en
`FreshIngredientIdRange`, que representa un intervalo cerrado.

```java
public boolean contains(long ingredientId) {
    return firstId <= ingredientId && ingredientId <= lastId;
}
```

La calculadora recorre los IDs disponibles y delega la comprobación en `isFresh`:

```java
public int count(InventoryDatabase database) {
    int freshIngredientIds = 0;

    for (long ingredientId : database.availableIngredientIds()) {
        if (isFresh(ingredientId, database)) {
            freshIngredientIds++;
        }
    }

    return freshIngredientIds;
}
```

El método auxiliar termina en cuanto encuentra un rango que contiene el ID. No hace
falta seguir buscando porque el resultado ya es verdadero:

```java
private boolean isFresh(long ingredientId, InventoryDatabase database) {
    for (FreshIngredientIdRange range : database.freshRanges()) {
        if (range.contains(ingredientId)) {
            return true;
        }
    }
    return false;
}
```

### Parte 2

La segunda parte ya no pregunta por los IDs disponibles, sino por cuántos IDs
distintos quedan cubiertos por todos los rangos frescos. Para no contar dos veces
los solapamientos, se ordenan los rangos por inicio y se fusionan los que se solapan
o se tocan.

El propio intervalo sabe si puede fusionarse con otro:

```java
public boolean overlapsOrTouches(FreshIngredientIdRange other) {
    return firstId <= other.lastId + 1 && other.firstId <= lastId + 1;
}

public FreshIngredientIdRange merge(FreshIngredientIdRange other) {
    return new FreshIngredientIdRange(
            Math.min(firstId, other.firstId),
            Math.max(lastId, other.lastId)
    );
}
```

La calculadora mantiene un intervalo acumulado. Si el siguiente rango se une con él,
se fusiona; si queda separado, se suma el tamaño del acumulado y se empieza uno
nuevo:

```java
for (FreshIngredientIdRange range : sortedRanges) {
    if (currentRange == null) {
        currentRange = range;
        continue;
    }

    if (currentRange.overlapsOrTouches(range)) {
        currentRange = currentRange.merge(range);
    } else {
        freshIngredientIds += currentRange.size();
        currentRange = range;
    }
}

if (currentRange != null) {
    freshIngredientIds += currentRange.size();
}
```

Con esto, rangos como `10-20` y `15-25` se cuentan como `10-25`, no como dos
tramos independientes.

## Uso de Streams

En este día se usa un stream en la parte 2 para ordenar los rangos frescos antes de
fusionarlos:

```java
List<FreshIngredientIdRange> sortedRanges = database.freshRanges().stream()
        .sorted(Comparator.comparingLong(FreshIngredientIdRange::firstId))
        .toList();
```

El stream parte de la lista de rangos frescos. `sorted(...)` crea una vista ordenada
por el inicio de cada intervalo (`firstId`). Esa ordenación es necesaria porque la
fusión funciona recorriendo de izquierda a derecha: si el rango actual se solapa o
toca con el acumulado, se fusiona; si no, se cierra el acumulado y se empieza otro.

`toList()` materializa el resultado ordenado en una lista inmutable. A partir de ahí,
la lógica de fusión usa un bucle normal porque necesita mantener estado (`currentRange`
y el total acumulado).

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

- `FreshIngredientIdRange`: representa un rango inclusivo de IDs frescos.
- `InventoryDatabase`: agrupa los rangos frescos y los IDs disponibles.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `FreshIngredientCounterPart1`: cuenta los IDs disponibles que son frescos.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `FreshIngredientIdCoverageCounterPart2`: cuenta cuántos IDs distintos quedan cubiertos por los rangos frescos.

### `application`

Coordina el caso de uso.

- `InventoryDatabaseParser`: transforma las líneas del fichero en un `InventoryDatabase`.
- `CafeteriaSolver`: lee la entrada, la parsea y delega el cálculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `DatabaseSource`: interfaz para obtener las líneas de entrada.
- `FileDatabaseSource`: implementación que lee la base de datos desde un fichero.

## Clases principales

### `Main` - `dia5/src/main/java/Main.java`

1. Localiza el input del día.
2. Crea `FileDatabaseSource` y `CafeteriaSolver`.
3. Ejecuta las dos partes y muestra sus resultados.

### `CafeteriaSolver` - `dia5/src/main/java/application/CafeteriaSolver.java`

1. Lee la base de datos mediante `DatabaseSource`.
2. La convierte en `InventoryDatabase` con `InventoryDatabaseParser`.
3. Delega la parte 1 y la parte 2 en sus contadores.

### `InventoryDatabaseParser` - `dia5/src/main/java/application/InventoryDatabaseParser.java`

1. Separa los rangos frescos de los IDs disponibles.
2. Convierte cada rango textual en `FreshIngredientIdRange`.
3. Construye un `InventoryDatabase`.

### `FreshIngredientIdRange` - `dia5/src/main/java/domain/common/FreshIngredientIdRange.java`

1. Representa un intervalo cerrado de IDs.
2. Comprueba pertenencia con `contains`.
3. Permite fusionar rangos que se solapan o se tocan.

### `InventoryDatabase` - `dia5/src/main/java/domain/common/InventoryDatabase.java`

1. Agrupa los rangos frescos y los IDs disponibles.
2. Copia las listas para evitar modificaciones externas.
3. Sirve como entrada común para ambas partes.

### `FreshIngredientCounterPart1` - `dia5/src/main/java/domain/part1/FreshIngredientCounterPart1.java`

1. Recorre los IDs disponibles.
2. Comprueba si cada ID cae dentro de algún rango fresco.
3. Cuenta cuántos IDs disponibles son frescos.

### `FreshIngredientIdCoverageCounterPart2` - `dia5/src/main/java/domain/part2/FreshIngredientIdCoverageCounterPart2.java`

1. Ordena los rangos frescos.
2. Fusiona rangos solapados o contiguos.
3. Suma la cobertura total sin contar duplicados.

### `DatabaseSource` - `dia5/src/main/java/infrastructure/DatabaseSource.java`

1. Define cómo obtener las líneas de la base de datos.
2. Desacopla el solver del mecanismo concreto de lectura.

### `FileDatabaseSource` - `dia5/src/main/java/infrastructure/FileDatabaseSource.java`

1. Guarda la ruta del fichero.
2. Lee todas sus líneas.
3. Implementa la interfaz `DatabaseSource`.

## Flujo del programa

1. `Main` crea `FileDatabaseSource`.
2. `CafeteriaSolver` lee las líneas del input y usa `InventoryDatabaseParser`.
3. El parser construye un `InventoryDatabase` con rangos de IDs frescos y los IDs disponibles.
4. La parte 1 recorre los IDs disponibles y cuenta cuántos pertenecen a algún rango fresco.
5. La parte 2 ordena los rangos frescos, fusiona los que se solapan o se tocan y suma su cobertura total.

```java
var database = parser.parse(source.getLines());
return new FreshIngredientIdCoverageCounterPart2().count(database);
```

La parte 2 evita contar dos veces IDs cubiertos por rangos solapados:

```java
if (currentRange.overlapsOrTouches(range)) {
    currentRange = currentRange.merge(range);
} else {
    freshIngredientIds += currentRange.size();
    currentRange = range;
}
```

## Fundamentos de diseño aplicados

### Alta Cohesión

`FreshIngredientIdRange` reúne las operaciones propias de un intervalo, como
`contains`, `merge` y `size`. La parte 1 se centra en contar IDs disponibles y la
parte 2 en calcular cobertura total de rangos.

### Bajo Acoplamiento

`CafeteriaSolver` depende de `DatabaseSource`, y los contadores dependen de
`InventoryDatabase`. Ninguna regla de dominio conoce cómo se lee el fichero.

### Modularidad

El modelo de inventario se comparte en `domain/common`. Las dos interpretaciones del
enunciado se mantienen en clases separadas, una para cada parte.

### Código Expresivo

Nombres como `FreshIngredientCounterPart1` y
`FreshIngredientIdCoverageCounterPart2` dejan claro si se cuentan IDs disponibles o
cobertura total de rangos. `overlapsOrTouches` expresa directamente la regla de
fusión.

### Abstracción

La clase `FreshIngredientIdRange` oculta las comparaciones de límites. La parte 2 no
manipula pares de números sueltos, sino rangos con operaciones propias del dominio.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

`InventoryDatabaseParser` parsea, `FreshIngredientIdRange` representa intervalos, `InventoryDatabase` agrupa datos, `FreshIngredientCounterPart1` cuenta IDs disponibles, `FreshIngredientIdCoverageCounterPart2` fusiona cobertura de rangos y `CafeteriaSolver` coordina el flujo.

### Principio Abierto/Cerrado (OCP)

La parte 2 se incorpora como una calculadora nueva que reutiliza `FreshIngredientIdRange` e `InventoryDatabase`. La parte 1 no se modifica para añadir la regla de cobertura total.

### Principio de Sustitución de Liskov (LSP)

`CafeteriaSolver` depende de `DatabaseSource`. Otra fuente de datos compatible puede sustituir a `FileDatabaseSource` sin afectar al caso de uso.

### Principio de Segregación de la Interfaz (ISP)

`DatabaseSource` solo expone la lectura de líneas. La implementación no tiene que soportar operaciones que el solver no usa.

### Principio de Inversión de Dependencias (DIP)

La aplicación depende de `DatabaseSource`, no de la clase concreta de fichero:

```java
public CafeteriaSolver(DatabaseSource source) {
    this.source = source;
}
```

### Principio de Composición sobre Herencia (COI)

La solución compone records de dominio y calculadores concretos. No hay una clase base de contadores ni una jerarquía de intervalos.

### Principio DRY

La lógica de pertenencia, solapamiento, fusión y tamaño de intervalos vive en `FreshIngredientIdRange`. Las partes reutilizan esas operaciones en lugar de repetir comparaciones de límites.

### Convención sobre Configuración (CoC)

La estructura del módulo sigue Maven: código, tests y recursos están en las carpetas esperadas por defecto.

### Principio YAGNI

No se añade una estructura de interval tree ni un motor de consultas más general. Para el enunciado basta con comprobar pertenencia en la parte 1 y ordenar/fusionar rangos en la parte 2.

## Patrones de diseño aplicados

### Creacionales

No se aplica ningún patrón creacional de forma explícita. No hace falta `Singleton`
porque no existe ningún recurso global que deba tener una única instancia, y tampoco
se usa `Factory Method` porque la creación de objetos es simple y directa.

### Estructurales

Se refleja `Adapter` en `FileDatabaseSource`. La aplicación trabaja con
`DatabaseSource`, mientras que `FileDatabaseSource` adapta `Files.readAllLines` a
esa interfaz propia del proyecto.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`, por ejemplo
al recorrer rangos de ingredientes. En Java este recorrido se apoya en
`Iterable`/`Iterator`, aunque el código no cree el iterador manualmente.

No se aplica `Command`, porque no hay objetos que encapsulen acciones ejecutables.
Tampoco se aplica `Observer`, porque no hay suscripciones ni notificación de cambios.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de rangos y IDs disponibles;
- el rechazo de rangos inválidos;
- la comprobación inclusiva de límites;
- la fusión de rangos solapados o contiguos;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `3`;
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `14`.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia5 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia5 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 640
Parte 2: 365804144481581
```
