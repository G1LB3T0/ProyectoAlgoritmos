(defun fahrenheit-a-celsius (fahrenheit)
  (/ (* (- fahrenheit 32) 5) 9))

(defun limpiar-consola ()
  (dotimes (i 50) (format t "~&")))

(defun main ()
  (limpiar-consola)
  (format t "Ingrese la temperatura en grados Fahrenheit: ")
  (finish-output)
  (let ((fahrenheit (read-line)))
    (format t "La temperatura en grados Celsius es: ~a~%" (fahrenheit-a-celsius (parse-integer fahrenheit)))))

(main)
