# Advent of Code

Este repositorio recoge la resolución de varios ejercicios de Advent of Code en
Java, aplicando conceptos trabajados en la asignatura de Ingeniería del Software 2.
El objetivo no ha sido únicamente obtener el resultado correcto de cada problema,
sino resolverlos con una organización mantenible, separando responsabilidades y
dejando constancia de las decisiones de diseño en los README de cada día.

Cada ejercicio se ha planteado como un pequeño caso de estudio. A partir del
enunciado se identifican las entidades relevantes del dominio, las reglas del
problema y los puntos donde conviene separar lectura de datos, transformación de
entrada, lógica de negocio y ejecución. De esta forma, la teoría vista en clase se
aplica de manera práctica sobre problemas diferentes.

## Organización general

El proyecto está organizado como un Maven multimódulo. Cada carpeta `diaN`
corresponde a un día de Advent of Code y contiene su propia implementación,
entrada, pruebas y documentación.

Aunque cada problema tiene particularidades, se ha seguido una organización común:

- `domain`: contiene los conceptos del problema y las reglas principales.
- `application`: coordina el caso de uso y conecta el dominio con la entrada.
- `infrastructure`: contiene los detalles externos, principalmente la lectura de
  ficheros.
- `src/main/resources/input.txt`: guarda la entrada del ejercicio.
- `src/test`: contiene pruebas sobre el parser, los ejemplos del enunciado y los
  casos relevantes de la solución.

Esta división permite que las clases tengan una responsabilidad clara, facilita la
reutilización de código entre la parte 1 y la parte 2 de un mismo día y evita que
la solución dependa directamente del formato del fichero de entrada. También ayuda
a aplicar principios como responsabilidad única, bajo acoplamiento, alta cohesión
y apertura a la extensión cuando aparece una segunda parte con nuevas reglas.

## Documentación

Cada día incluye un `README.md` propio. En ellos se explica:

- el problema que se resuelve;
- el enfoque algorítmico usado;
- las clases principales y su responsabilidad;
- la relación con conceptos de Ingeniería del Software 2;
- las pruebas realizadas;
- los comandos necesarios para compilar, probar y ejecutar el día.

El README raíz actúa como introducción general del proyecto, mientras que los
README de cada módulo contienen el detalle concreto de cada ejercicio.

## Comandos

Desde la raíz del repositorio se pueden compilar todos los módulos:

```bash
mvn compile
```

Para ejecutar las pruebas de todos los días:

```bash
mvn test
```

Para trabajar con un día concreto:

```bash
mvn -pl dia12 test
mvn -pl dia12 exec:java -Dexec.mainClass=Main
```

Sustituyendo `dia12` por el módulo correspondiente se puede ejecutar cualquier
otro ejercicio. En IntelliJ basta con abrir la carpeta `AOC` e importar el proyecto
Maven desde el `pom.xml` de la raíz.
