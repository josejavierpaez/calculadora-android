package com.example.josej.cursos.calculadora;

import android.widget.EditText;

import java.util.Locale;



public class Metodos {
    // metodo capaz de extraer el operador existente de una operacion proporcionada
    public static String getOperator(String operation) {
        /*Secuencia de operadores ternarios para saber si la operacion contiene '÷ x +', de lo
          contrario, se asigna el operador = 'null' (sin importar si es '-' o null */
        String operator = operation.contains(Constantes.OPERATOR_MULTI) ? Constantes.OPERATOR_MULTI :
                operation.contains(Constantes.OPERATOR_BETWEN) ? Constantes.OPERATOR_BETWEN :
                        operation.contains(Constantes.OPERATOR_SUM) ? "\\" + Constantes.OPERATOR_SUM : Constantes.OPERATOR_NULL;

        /* en un segundo paso, se verifica si la operación contiene operador '-' valido,
           si y solo si el operador actual es null, y se encuentra un '-' y su index es mayor a 0
           es decir, no se encuentra al inicio de la operacion(haciendo negativo el primer numero)*/
        if (operator.equals(Constantes.OPERATOR_NULL) && operation.lastIndexOf(Constantes.OPERATOR_SUB) > 0) {
            // reasignar el valor de operator con OPERATOR_SUB
            operator = Constantes.OPERATOR_SUB;
        }
        // regresar nuestra variable string operador con el nuevo valor.
        return operator;
		
		
		//Lenguaje de expresión, más info aquí: https://developer.android.com/topic/libraries/data-binding/?hl=es  
    }

    public static void tryResolve(boolean fromResult, EditText etInput, OnResolveCallback callback) {
        // extraemos la posible operación contenida en etInput y se asigna a la variable operation
        String operation = etInput.getText().toString();

        //Si la operacion esta vacia, no hay nada que procesar y se retorna la función
        if (operation.isEmpty()) {
            return;
        }

        // si la operación contiene un punto '.'
        // y si ese punto tiene como indice la última posición
        if (operation.contains(Constantes.POINT) &&
                operation.lastIndexOf(Constantes.POINT) == operation.length() - 1) {
            /* se recorta la operación para que ese punto al final desaparezca(n. = n.0),
               y de esa forma se proceda a ejecutar una operación correcta */
            operation = operation.substring(0, operation.length() - 1);
        }

        // conseguimos el operador
        String operator = Metodos.getOperator(operation);
        // creamos un arreglo 'values' con longitud 0
        String[] values = new String[0];
        // si el operador es diferente de 'null'
        if (!operator.equals(Constantes.OPERATOR_NULL)) {
            // si el operador es igual a '-' menos
            if (operator.equals(Constantes.OPERATOR_SUB)) {
                /*  creamos una constante 'index' ayudara a dividir la operación a partir del último
                    operador '-' encontrado. */
                final int index = operation.lastIndexOf(Constantes.OPERATOR_SUB);
                /*  values se iguala a una nueva instancia de un arreglo con longitud = 2 para
                    almacenar los 2 numeros a procesar. */
                values = new String[2];
                // se extrae el primer numero, desde el inicio de la operación, hasta el 'index'
                values[0] = operation.substring(0, index);
                // se extrae el segundo numero, desde el 'index + 1' hasta el final de la operación
                values[1] = operation.substring(index + 1);
            } else {
                // de lo contrario, si el operador es diferente de 'null' o '-' (osea ÷ x +)
                // dividimos 'operation' con el metodo 'split' de la clase String, con esto
                // se asigna a values un arreglo de 2 elementos, a partir de 'operator'
                values = operation.split(operator);
            }
        }

        /*  si 'values' tiene un elemento o más, significa que muy probablemente tenemos 2 numeros
            validos, cabe la posibilidad de que una parte tenga un mal formato, alguna combinación
            que no hayamos contemplado dentro de nuestras validaciones */
        if (values.length > 1) {
            try {
                // creamos 2 constantes que recibiran un double,
                // si alguna conversion falla, recaera en el catch
                final double numberOne = Double.valueOf(values[0]);
                final double numberTwo = Double.valueOf(values[1]);
                // limpiamos el contenido del 'etInput'
                etInput.getText().clear();
                // notificamos via callback, que la variable booleana deberia ser true.
                callback.onIsEditing();
                /* utilizamos el metodo append, porque nos permite establecer el texto si este no
                   estuviera editable(evitamos esto desde el xml para evitar el teclado.
                   Ponemos el resultado con un formato neutro(ROOT), y a 2 decimales */
                etInput.append(String.format(Locale.ROOT, "%.2f",
                        Metodos.getResult(numberOne, operator, numberTwo)));
            } catch (NumberFormatException e) { // si algun Double.value falla
                // verifica si viene del boton '=' y muestra el mensaje de error
                if (fromResult) {
                    //showMessage(R.string.message_exp_incorrect);
                    callback.onShowMessage(R.string.message_exp_incorrect);
                }
            }
        } else { // si 'values.length' no es mayor a 1(osea = 0) no se pudo dividir con ningún operador
            // verifica si viene del boton '='
            if (fromResult) {
                // si el operador es valido. (si es igual a 'null', es una operación incompleta
                if (!operator.equals(Constantes.OPERATOR_NULL)){
                    // muestra el mensaje de error
                    callback.onShowMessage(R.string.message_exp_incorrect);
                }
            }
        }
		
		// Más sobre EditText y sus métodos en: https://developer.android.com/reference/android/widget/EditText
		// Más info. Sobre métodos de la clase String: https://developer.android.com/reference/java/lang/String
    }

    // este metodo es llamado si y solo si existe una operación valida, en cuyo caso se procede
    public static double getResult(double numberOne, String operator, double numberTwo) {
        // se crea una nueva variable que contendrá el resultado de la operación
        double result = 0;

        // se eliminan las diagonales inversas en caso de existir y asi dejar el operador solo.
        operator = operator.replace("\\", "");

		// segun el caso del operador, se procede a realizar la operación matematica
        switch (operator) {
            case Constantes.OPERATOR_MULTI:
                result = numberOne * numberTwo;
                break;
            case Constantes.OPERATOR_BETWEN:
                result = numberOne / numberTwo;
                break;
            case Constantes.OPERATOR_SUM:
                result = numberOne + numberTwo;
                break;
            case Constantes.OPERATOR_SUB:
                result = numberOne - numberTwo;
                break;
        }

		// devuelve el resultado
        return result;
    }

    public static boolean canReplaceOperator(CharSequence s) {
		// si existen al menos 2 caracteres sigue, si no, retorna falso
        if (s.length() < 2) {
            return false;
        }
		// se extraen los 2 ultimos caracteres en constantes string para hacer las comprobaciones
        final String ultimoCaracter = String.valueOf(s.charAt(s.length()-1));
        final String penultimoCaracter = String.valueOf(s.charAt(s.length()-2));

		// y si el ultimo es un operador diferente de '-'
        return (ultimoCaracter.equals(Constantes.OPERATOR_MULTI) ||
                ultimoCaracter.equals(Constantes.OPERATOR_BETWEN) ||
                ultimoCaracter.equals(Constantes.OPERATOR_SUM)) &&
				// y verificar que el penultimo caracter sea un operador '÷ x + -'
                (penultimoCaracter.equals(Constantes.OPERATOR_MULTI) ||
                penultimoCaracter.equals(Constantes.OPERATOR_BETWEN) ||
                penultimoCaracter.equals(Constantes.OPERATOR_SUM) ||
                penultimoCaracter.equals(Constantes.OPERATOR_SUB));
    }
}
