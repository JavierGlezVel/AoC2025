# Advent of Code

Repositorio para resolver Advent of Code en Java, organizado como un proyecto Maven multimodulo.

## Estructura

```text
AOC/
  pom.xml
  dia1/
    pom.xml
    src/main/java/
      application/
      domain/
        common/
        part1/
        part2/
      infrastructure/
    src/main/resources/input.txt
  dia2/
    pom.xml
    src/main/java/
      application/
      domain/
        common/
        part1/
        part2/
      infrastructure/
    src/main/resources/input.txt
  dia3/
    pom.xml
    src/main/java/
      application/
      domain/
        common/
        part1/
        part2/
      infrastructure/
    src/main/resources/input.txt
  dia4/
    pom.xml
    src/main/java/
      application/
      domain/
        common/
        part1/
      infrastructure/
    src/main/resources/input.txt
```

Cada dia es un modulo Maven independiente. Dentro de cada dia, `application` coordina
el caso de uso, `infrastructure` contiene la entrada/salida, y `domain` se divide en
`common`, `part1` y `part2` para distinguir lo compartido de lo especifico de cada
parte.

El `pom.xml` de la raiz permite compilar todos los dias juntos.

## Comandos

Desde la raiz del repositorio:

```bash
mvn compile
```

Para ejecutar un dia desde IntelliJ, abre el repositorio desde la carpeta `AOC` e importa el proyecto Maven.
