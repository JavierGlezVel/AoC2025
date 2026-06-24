# Dia 8

## Problema

El problema ocurre en un parque subterraneo. La entrada contiene posiciones de cajas
de conexion en un espacio tridimensional. Cada linea tiene coordenadas `X,Y,Z`:

```text
162,817,812
57,618,57
```

Los elfos conectan parejas de cajas empezando por las que estan mas cerca en
distancia recta. Cuando dos cajas se conectan, sus circuitos se unen. Si las dos cajas
ya estaban en el mismo circuito, esa conexion no cambia la red, pero sigue formando
parte de la lista de conexiones mas cortas procesadas.

La entrada esta en:

```text
src/main/resources/input.txt
```

## Parte 1

El objetivo es procesar las 1000 parejas de cajas mas cercanas y multiplicar los
tamanos de los tres circuitos mas grandes que queden despues.

Con el ejemplo oficial, tras procesar las 10 conexiones mas cortas, los tres mayores
circuitos tienen tamanos `5`, `4` y `2`. El resultado es:

```text
40
```

Con el input del proyecto, la respuesta de la parte 1 es:

```text
244188
```

## Parte 2

Ahora hay que seguir conectando parejas, siempre en orden de cercania, hasta que
todas las cajas queden dentro de un unico circuito. El resultado pedido es el
producto de las coordenadas `X` de las dos cajas de la conexion que consigue unirlo
todo.

Con el ejemplo oficial, la conexion final es entre `216,146,977` y `117,168,530`, y
el resultado es:

```text
25272
```

Con el input del proyecto, la respuesta de la parte 2 es:

```text
8361881885
```

## Enfoque de la solucion

`ConnectionCandidateGenerator` genera todas las parejas posibles de cajas y calcula
su distancia al cuadrado:

```java
long distanceSquared = junctionBoxes.get(first).distanceSquaredTo(junctionBoxes.get(second));
```

No hace falta calcular la raiz cuadrada porque comparar distancias al cuadrado
mantiene el mismo orden y evita trabajar con numeros decimales.

Despues ordena las parejas por distancia. Las dos partes reutilizan esa lista
ordenada, pero aplican reglas distintas:

- `CircuitSizeProductCalculatorPart1` procesa las primeras 1000 conexiones y
  multiplica los tres tamanos de circuito mas grandes.
- `FinalConnectionXProductCalculatorPart2` procesa conexiones hasta que
  `CircuitNetwork` queda reducido a un unico circuito y devuelve el producto de las
  coordenadas `X` de esa ultima conexion necesaria.

`CircuitNetwork` implementa union-busqueda. Esta estructura mantiene para cada caja
su representante de circuito, el tamano de cada componente conectada y cuantos
circuitos quedan activos.

## Diseno de clases

La solucion esta dividida en tres paquetes principales:

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

- `JunctionBox`: representa una caja de conexion y calcula distancias al cuadrado.
- `ConnectionCandidate`: representa una posible conexion entre dos cajas.
- `ConnectionCandidateGenerator`: genera las posibles conexiones ordenadas por
  distancia.
- `CircuitNetwork`: representa la red de circuitos mediante union-busqueda.

### `domain/part1`

Contiene la regla especifica de la primera parte.

- `CircuitSizeProductCalculatorPart1`: procesa las conexiones mas cortas y calcula
  el producto pedido.

### `domain/part2`

Contiene la regla especifica de la segunda parte.

- `FinalConnectionXProductCalculatorPart2`: conecta cajas hasta que queda un unico
  circuito y calcula el producto de las `X` de la conexion final.

### `application`

Coordina el caso de uso.

- `JunctionBoxParser`: transforma las lineas del fichero en cajas del dominio.
- `PlaygroundSolver`: lee la entrada, la parsea y delega el calculo.

### `infrastructure`

Contiene los detalles externos al dominio.

- `JunctionBoxSource`: interfaz para obtener las lineas de entrada.
- `FileJunctionBoxSource`: implementacion que lee las cajas desde un fichero.

## Principios aplicados

### Abstraccion

El dominio trabaja con conceptos propios del problema: caja de conexion, red de
circuitos, conexiones candidatas y calculos de cada parte. La logica no depende de
rutas de ficheros ni de consola.

### Diseno por contrato

`JunctionBoxParser` rechaza entradas vacias, lineas nulas, lineas sin tres
coordenadas y coordenadas que no sean numericas. `CircuitNetwork` exige al menos un
elemento, `CircuitSizeProductCalculatorPart1` exige al menos tres cajas y
`FinalConnectionXProductCalculatorPart2` exige al menos dos cajas.

### Alta cohesion y SRP

Cada clase tiene una responsabilidad concreta:

- `JunctionBoxParser` solo parsea coordenadas.
- `JunctionBox` solo representa una posicion 3D y calcula distancias.
- `ConnectionCandidateGenerator` solo genera conexiones candidatas ordenadas.
- `CircuitNetwork` solo gestiona componentes conectadas.
- `CircuitSizeProductCalculatorPart1` solo aplica la regla de la parte 1.
- `FinalConnectionXProductCalculatorPart2` solo aplica la regla de la parte 2.
- `FileJunctionBoxSource` solo lee lineas de un fichero.
- `PlaygroundSolver` solo coordina el caso de uso.
- `Main` solo prepara dependencias y muestra la salida.

Esto sigue la idea de cohesion y responsabilidad unica vista en teoria: cada modulo
tiene una razon principal para cambiar.

### Bajo acoplamiento

`PlaygroundSolver` depende de `JunctionBoxSource`, no de `FileJunctionBoxSource`:

```java
public PlaygroundSolver(JunctionBoxSource source) {
    this.source = source;
}
```

Esto permite cambiar el origen de datos sin modificar la logica de aplicacion.

### Inversion e inyeccion de dependencias

La logica de alto nivel depende de una abstraccion (`JunctionBoxSource`). La
implementacion concreta se crea fuera y se inyecta por constructor:

```java
JunctionBoxSource source = new FileJunctionBoxSource(inputPath);
PlaygroundSolver solver = new PlaygroundSolver(source);
```

Asi se separa la creacion del objeto concreto de su uso, reduciendo acoplamiento.

### Modularidad

La division en paquetes separa responsabilidades:

- `domain/common`: conceptos compartidos del problema.
- `domain/part1`: regla especifica de la primera parte.
- `domain/part2`: regla especifica de la segunda parte.
- `application`: coordinacion del caso de uso.
- `infrastructure`: detalles tecnicos de entrada.

### Abierto/cerrado

La parte 2 se ha anadido creando `FinalConnectionXProductCalculatorPart2` y
reutilizando `JunctionBox`, `CircuitNetwork` y `ConnectionCandidateGenerator`. Esto
mantiene las clases del dominio comun abiertas a reutilizacion por nuevas reglas y
cerradas frente a cambios innecesarios cuando aparece una nueva parte del problema.

## Patrones y tecnicas usadas

### Source / Adapter

`JunctionBoxSource` abstrae el origen de datos. `FileJunctionBoxSource` adapta
`Files.readAllLines` a una interfaz propia del proyecto.

### Value Object

`JunctionBox` se modela como `record`, por lo que representa un valor del dominio
definido por sus coordenadas.

### Service

`CircuitSizeProductCalculatorPart1` y `FinalConnectionXProductCalculatorPart2`
actuan como servicios de dominio: no representan entidades con identidad propia,
sino operaciones que calculan los resultados de cada parte.

### Union-busqueda

`CircuitNetwork` usa union-busqueda con compresion de caminos y union por tamano.
Esto permite unir circuitos y consultar componentes de forma eficiente.

## Tests

Los tests estan en:

```text
src/test/java/
```

Cubren:

- el parseo de coordenadas validas;
- el rechazo de lineas invalidas;
- el ejemplo oficial de la parte 1, cuyo resultado esperado es `40`;
- que una pareja ya conectada siga contando como conexion procesada.
- el ejemplo oficial de la parte 2, cuyo resultado esperado es `25272`;
- que la parte 2 ignore conexiones internas al mismo circuito y pare en la union
  real que deja un unico circuito.

Para ejecutar los tests desde la raiz del repositorio:

```bash
mvn -pl dia8 test
```

## Ejecucion

Desde la raiz del repositorio:

```bash
mvn -pl dia8 exec:java -Dexec.mainClass=Main
```

El programa imprime:

```text
Parte 1: 244188
Parte 2: 8361881885
```
