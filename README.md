# LISP Interpreter in Java

Este repositorio contiene un intérprete LISP implementado en Java. El intérprete puede evaluar expresiones LISP, manejar definiciones de funciones y ejecutar código LISP dentro de un contexto de ejecución en Java.

## Características

- Evaluación de expresiones aritméticas y lógicas básicas.
- Definición y llamadas a funciones personalizadas.
- Soporte para operaciones condicionales `IF` y comparaciones.
- Manejo de variables mediante `SETQ`.
- Evaluación de átomos y listas.

## Sintaxis Soportada

El intérprete maneja las siguientes expresiones y funciones LISP:

- `(ATOM <expresión>)`: Evalúa si la expresión es un átomo.
- `(LIST (<elementos>))`: Crea una lista con los elementos dados.
- `(EQUAL <exp1> <exp2>)`: Compara si dos expresiones son iguales.
- `(SETQ <variable> <valor>)`: Asigna un valor a una variable.
- `(IF (<condición>) (<expresión-verdadera>) (<expresión-falsa>))`: Ejecuta una expresión basada en una condición.
- `(DEFUN <nombre-función> (<parámetros>) (<cuerpo>))`: Define una nueva función.

## Cómo usar

Para utilizar el intérprete, debes compilar y ejecutar el archivo `LispInterpreter.java`, que es la clase principal del intérprete.

### Ejemplo de Uso

Primero, compila la clase `LispInterpreter` junto con las clases `Evaluator` y `ExecutionContext`. Luego puedes ejecutar `LispInterpreter` para interactuar con el intérprete LISP a través de la consola. 

La clase `LispInterpreter` buscará y evaluará expresiones LISP contenidas en un archivo de texto y luego permitirá la entrada interactiva del usuario. 

Por defecto, busca un archivo llamado `data/expressions.txt`. Asegúrate de que este archivo existe y contiene expresiones LISP válidas, una por línea. 

Aquí tienes un ejemplo de cómo el archivo `data/expressions.txt` podría verse:

```lisp
(SETQ X 10)
(SETQ Y 20)
(DEFUN FACT (N) (IF (= N 0) 1 (* N (FACT (- N 1)))))
(FACT 5)
```

Cuando ejecutes `LispInterpreter`, se evaluarán las expresiones del archivo y se mostrarán los resultados. Después de procesar el archivo, podrás introducir más expresiones directamente en la consola:

```shell
$ java LispInterpreter
LISP Interpreter. Enter expressions or 'exit' to quit.
Result: 10
Result: 20
Function defined.
Result: 120
> (FIB 5)
Result: 5
> exit
Interpreter terminated.
```

Este enfoque te permite probar rápidamente muchas expresiones almacenadas en un archivo, así como interactuar de manera ad hoc con el intérprete a través de la línea de comandos.
