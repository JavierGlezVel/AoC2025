# Día 11

## Problema

La entrada describe una red de dispositivos conectados:

```text
you: bbb ccc
bbb: ddd eee
```

Cada línea indica un dispositivo y a qué otros dispositivos puede enviar la señal.
El objetivo es contar caminos dentro de esa red.

Se puede imaginar como un grafo: los dispositivos son nodos y las conexiones son
flechas hacia otros nodos.

La entrada está en:

```text
src/main/resources/input.txt
```

## Parte 1

Hay que contar cuántos caminos distintos van desde `you` hasta `out`.

Cada camino válido empieza en `you`, sigue las conexiones disponibles y termina en
`out`.

Con el ejemplo oficial:

```text
aaa: you hhh
you: bbb ccc
bbb: ddd eee
ccc: ddd eee fff
ddd: ggg
eee: out
fff: out
ggg: out
hhh: ccc fff iii
iii: out
```

El resultado es:

```text
5
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
590
```

## Parte 2

Ahora se cuentan caminos desde `svr` hasta `out`, pero solo valen si pasan por `dac`
y por `fft`, en cualquier orden.

Por eso no basta con saber en qué dispositivo estamos: también hay que recordar si ya
se han visitado esos dos dispositivos obligatorios.

Con el ejemplo oficial de la parte 2, el resultado es:

```text
2
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
319473830844560
```

## Enfoque de la solución

Primero, `DeviceNetworkParser` convierte cada línea del input en conexiones entre
dispositivos. El resultado es una red donde cada dispositivo sabe a qué dispositivos
puede enviar la señal.

Después del parseo, resolver el problema consiste en recorrer ese grafo y sumar los
caminos que llegan al nodo final.

Para la parte 1, `ReactorPathCounterPart1` empieza en `you` y sigue todas las salidas
posibles. Cada vez que llega a `out`, ha encontrado un camino válido.

Para no repetir trabajo, guarda cuántos caminos salen desde cada dispositivo. Así, si
vuelve a llegar al mismo dispositivo por otro camino, reutiliza el resultado:

```java
memoizedPaths.put(device, totalPaths);
```

Cuando la búsqueda llega a `out`, devuelve `1`. Si llega a un dispositivo sin salida
que no es `out`, devuelve `0`.

Para la parte 2 se usa la misma forma de recorrer la red, pero hay que recordar más
información. No basta con saber el dispositivo actual: también hay que saber si el
camino ya pasó por `dac` y por `fft`.

Al llegar a `out`, el camino solo cuenta si esos dos dispositivos ya se visitaron. Se
usa `BigInteger` porque el número de caminos puede ser muy grande.


## Uso de Streams

En este día el stream aparece al construir `DeviceNetwork`. La entrada parseada se
recibe como un `Map<String, List<String>>`, pero el record guarda una copia
inmutable para proteger el estado interno.

```java
outputsByDevice = outputsByDevice.entrySet().stream()
        .collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                entry -> List.copyOf(entry.getValue())
        ));
```

El stream recorre las entradas del mapa original. `collect(toUnmodifiableMap(...))`
construye un nuevo mapa no modificable. La clave se mantiene con `Map.Entry::getKey`
y el valor se copia con `List.copyOf(...)` para que tampoco pueda modificarse la
lista de salidas desde fuera.

Este stream no calcula la respuesta del puzzle directamente; refuerza la seguridad
del modelo de dominio para que los contadores de caminos trabajen con una red estable.

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

- `DeviceNetwork`: representa la red dirigida de dispositivos.

### `domain/part1`

Contiene la regla específica de la primera parte.

- `ReactorPathCounterPart1`: cuenta los caminos desde `you` hasta `out`.

### `domain/part2`

Contiene la regla específica de la segunda parte.

- `ReactorRequiredDevicePathCounterPart2`: cuenta los caminos desde `svr` hasta
  `out` que pasan por `dac` y `fft`.

### `application`

Coordina el caso de uso.

- `DeviceNetworkParser`: transforma las líneas del fichero en un `DeviceNetwork`.
- `ReactorSolver`: lee la entrada, la parsea y delega el cálculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `DeviceNetworkSource`: interfaz para obtener las líneas de entrada.
- `FileDeviceNetworkSource`: implementación que lee la red desde un fichero.

## Clases principales

### `Main` - `Main.java`

1. Calcula la ruta del input del día 11.
2. Crea `FileDeviceNetworkSource` y `ReactorSolver`.
3. Ejecuta las dos partes y escribe los resultados por consola.

### `ReactorSolver` - `application/ReactorSolver.java`

1. Lee las líneas mediante `DeviceNetworkSource`.
2. Convierte el texto en `DeviceNetwork` con `DeviceNetworkParser`.
3. Delega el conteo de caminos en la clase de dominio de cada parte.

### `DeviceNetworkParser` - `application/DeviceNetworkParser.java`

1. Recorre las conexiones descritas en el input.
2. Separa cada dispositivo de sus salidas.
3. Construye el mapa de adyacencias usado por `DeviceNetwork`.

### `DeviceNetwork` - `domain/common/DeviceNetwork.java`

1. Representa la red dirigida de dispositivos.
2. Devuelve las salidas disponibles desde un dispositivo.
3. Centraliza el nombre del dispositivo final de salida.

### `ReactorPathCounterPart1` - `domain/part1/ReactorPathCounterPart1.java`

1. Empieza el recorrido desde el dispositivo inicial.
2. Explora recursivamente todas las salidas.
3. Usa memoria de resultados para no recalcular caminos repetidos.

### `ReactorRequiredDevicePathCounterPart2` - `domain/part2/ReactorRequiredDevicePathCounterPart2.java`

1. Recorre la red manteniendo si ya se han visitado los dispositivos obligatorios.
2. Cuenta solo los caminos que llegan a salida después de pasar por ambos.
3. Memoriza estados para evitar repetir subproblemas.

### `PathState` - `domain/part2/ReactorRequiredDevicePathCounterPart2.java`

1. Guarda el dispositivo actual.
2. Indica si ya se visitaron `dac` y `fft`.
3. Genera el siguiente estado al avanzar por la red.

### `DeviceNetworkSource` - `infrastructure/DeviceNetworkSource.java`

1. Define la lectura de líneas de la red.
2. Permite sustituir el origen de datos sin cambiar el solver.

### `FileDeviceNetworkSource` - `infrastructure/FileDeviceNetworkSource.java`

1. Guarda la ruta del fichero de entrada.
2. Lee todas sus líneas.
3. Implementa `DeviceNetworkSource` usando el sistema de archivos.

## Flujo del programa

1. `Main` crea `FileDeviceNetworkSource`.
2. `ReactorSolver` lee las líneas y `DeviceNetworkParser` construye la red dirigida.
3. `DeviceNetwork` permite consultar las salidas de cada dispositivo.
4. La parte 1 cuenta todos los caminos desde `svr` hasta `out`.
5. La parte 2 cuenta solo los caminos que llegan a `out` después de haber pasado por `dac` y `fft`.
6. Ambas partes usan recursión con memoria para no recalcular los caminos desde un mismo estado.

```java
var network = parser.parse(source.getLines());
return new ReactorRequiredDevicePathCounterPart2().countPaths(network);
```

La parte 2 guarda en el estado tanto el dispositivo actual como si se han visitado los obligatorios:

```java
PathState updatedState = state.withVisitedDevice();
if (DeviceNetwork.OUTPUT_DEVICE.equals(updatedState.device())) {
    return updatedState.hasVisitedBothRequiredDevices() ? BigInteger.ONE : BigInteger.ZERO;
}
```

## Fundamentos de diseño aplicados

### Alta Cohesión

`DeviceNetwork` representa la red dirigida, `ReactorPathCounterPart1` cuenta caminos
desde `you` hasta `out` y `ReactorRequiredDevicePathCounterPart2` añade el estado de
dispositivos obligatorios. Cada contador mantiene una regla clara.

### Bajo Acoplamiento

`ReactorSolver` depende de `DeviceNetworkSource`. Los contadores trabajan con
`DeviceNetwork`, no con líneas de texto ni con detalles del fichero.

### Modularidad

La red está en `domain/common`, mientras que cada regla de conteo está en su paquete
de parte correspondiente. La lectura y el parseo quedan fuera de los contadores.

### Código Expresivo

`outputsFrom`, `countPaths`, `PathState`, `visitedDac` y `visitedFft` describen la
estructura de la búsqueda y el estado requerido por la parte 2.

### Abstracción

`DeviceNetwork` oculta el mapa interno de salidas. Los contadores solo piden las
salidas de un dispositivo mediante `outputsFrom`.

## Principios aplicados

### Principio de Responsabilidad Única (SRP)

`DeviceNetworkParser` parsea la red, `DeviceNetwork` representa conexiones, `ReactorPathCounterPart1` cuenta caminos simples hacia `out`, `ReactorRequiredDevicePathCounterPart2` cuenta caminos con dispositivos obligatorios y `ReactorSolver` coordina.

### Principio Abierto/Cerrado (OCP)

La parte 2 añade una regla de conteo con estado (`PathState`) sin modificar `ReactorPathCounterPart1` ni `DeviceNetwork`. El modelo común de red queda cerrado y reutilizable.

### Principio de Sustitución de Liskov (LSP)

`ReactorSolver` usa `DeviceNetworkSource`. Cualquier implementación que entregue líneas de red válidas puede sustituir a `FileDeviceNetworkSource`.

### Principio de Segregación de la Interfaz (ISP)

`DeviceNetworkSource` es específica y pequeña: solo lectura de líneas. No fuerza a implementar operaciones de grafo que corresponden al dominio.

### Principio de Inversión de Dependencias (DIP)

El solver depende de la abstracción `DeviceNetworkSource`:

```java
public ReactorSolver(DeviceNetworkSource source) {
    this.source = source;
}
```

### Principio de Composición sobre Herencia (COI)

Las dos reglas de conteo son servicios concretos que componen `DeviceNetwork`. No hay herencia entre contadores de caminos.

### Principio DRY

`DeviceNetwork` concentra la representación de salidas por dispositivo. Ambos contadores reutilizan `outputsFrom` y no duplican la estructura del grafo.

### Convención sobre Configuración (CoC)

El día sigue el layout Maven estándar del repositorio, lo que reduce configuración explícita.

### Principio YAGNI

No se crea un framework de grafos general. La solución implementa solo conteo de caminos con memoización y el estado adicional requerido por la parte 2.

## Patrones de diseño aplicados

### Creacionales

No se aplica ningún patrón creacional de forma explícita. No hace falta `Singleton`
porque no existe ningún recurso global que deba tener una única instancia, y tampoco
se usa `Factory Method` porque la creación de objetos es simple y directa.

### Estructurales

Se refleja `Adapter` en `FileDeviceNetworkSource`. La aplicación trabaja con
`DeviceNetworkSource`, mientras que `FileDeviceNetworkSource` adapta
`Files.readAllLines` a esa interfaz propia del proyecto.

No se aplica `Decorator`, porque no se añaden responsabilidades dinámicamente a un
objeto envolviéndolo con otros objetos.

### De comportamiento

Se refleja `Iterator` mediante el uso de colecciones y bucles `for-each`, por ejemplo
al recorrer salidas de dispositivos. En Java este recorrido se apoya en
`Iterable`/`Iterator`, aunque el código no cree el iterador manualmente.

No se aplica `Command`, porque no hay objetos que encapsulen acciones ejecutables.
Tampoco se aplica `Observer`, porque no hay suscripciones ni notificación de cambios.

## Tests

Los tests están en:

```text
src/test/java/
```

Cubren:

- el parseo de conexiones;
- el rechazo de descripciones inválidas;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `5`;
- la detección de ciclos alcanzables desde `you`.
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `2`;
- que la parte 2 ignore caminos que no pasan por los dos dispositivos requeridos.

Para ejecutar los tests desde la raíz del repositorio:

```bash
mvn -pl dia11 test
```

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -pl dia11 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 590
Parte 2: 319473830844560
```
