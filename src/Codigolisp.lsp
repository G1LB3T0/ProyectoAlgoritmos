(defun fahrenheit-a-celsius (fahrenheit);;definimos la funcion y ponemos entre () el parametro
  "Convierte una temperatura de Fahrenheit a Celsius.";;comentario para la interacción con el usuario
  (/ (* (- fahrenheit 32) 5) 9))';;conversion de fahrenheit a celcuis que es de afuera hacia adentro que primero resta el input con 32 luego le multiplica el 5 y de ultimo divide entro de 9

(defun main ();;definimos la funcion main
  "Función principal para la interacción con el usuario.";;comentario para interacción con el usuario
  (format t "Ingrese la temperatura en grados Fahrenheit: ");; pide al usuario ingresar una temperatura
  (finish-output);;asegura que este mensaje se muestre completamente antes de que el programa continue
  (let ((fahrenheit (read)));; lee lo que ingreso el usuario y lo guarda en la variable "fahrenheit"
    (format t "La temperatura en grados Celsius es: ~a~%" (fahrenheit-a-celsius fahrenheit))));; imprime un comentario y llamamos el resultado de nuestra conversion definida anteriormente con la variable fahrenheit

(main);;llamamos al main
