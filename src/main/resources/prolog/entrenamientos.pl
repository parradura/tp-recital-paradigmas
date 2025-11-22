:- module(entrenamientos, [
    entrenamientos_minimos/2
]).

/*
  entrenamientos_minimos(+ListaFaltantesPorRol, -TotalEntrenamientos)

  ListaFaltantesPorRol es una lista de listas.
  Cada sub-lista corresponde a un rol.
  Cada elemento de la sub-lista es la cantidad de faltantes de ese rol en una canción.

  Ejemplo:
    [[2,0,1], [0,1,0]]

  Significa:
    - Para el rol 1: faltantes por canción = 2,0,1
    - Para el rol 2: faltantes por canción = 0,1,0

  Para cada rol:
    EntrenamientosRol = max(FaltantesPorCancion)
  TotalEntrenamientos = suma de EntrenamientosRol.
*/

entrenamientos_minimos(ListaFaltantesPorRol, TotalEntrenamientos) :-
    entrenamientos_minimos_(ListaFaltantesPorRol, 0, TotalEntrenamientos).

entrenamientos_minimos_([], Acc, Acc).
entrenamientos_minimos_([FaltantesRol|Resto], Acc, Total) :-
    entrenamientos_para_rol(FaltantesRol, EntrenamientosRol),
    Acc1 is Acc + EntrenamientosRol,
    entrenamientos_minimos_(Resto, Acc1, Total).

/*
  entrenamientos_para_rol(+FaltantesPorCancion, -EntrenamientosRol)

  EntrenamientosRol es el máximo de la lista de faltantes por canción.
  Si la lista está vacía, es 0.
*/

entrenamientos_para_rol([], 0).
entrenamientos_para_rol(Faltantes, EntrenamientosRol) :-
    max_list(Faltantes, EntrenamientosRol).

/* max_list(+Lista, -Max) - máx. genérico para una lista no vacía */
max_list([X|Xs], Max) :-
    max_list_(Xs, X, Max).

max_list_([], Max, Max).
max_list_([X|Xs], Acc, Max) :-
    (  X > Acc
    -> Acc1 = X
    ;  Acc1 = Acc
    ),
    max_list_(Xs, Acc1, Max).
