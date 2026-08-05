# Checklist manual del MVP UniVote

No anote credenciales ni códigos reales en este documento. Ejecute cada caso en un entorno local autorizado y registre solo aprobado/fallido en un sistema seguro.

## Preparación

- [ ] Sin configuración local, la aplicación falla de forma segura y no muestra secretos.
- [ ] Con configuración válida, abre directamente el login sin conectar hasta una operación JDBC.
- [ ] La ventana carga FXML/CSS sin excepciones y puede cerrarse normalmente.

## Autenticación

- [ ] Credenciales incorrectas muestran el mensaje genérico y limpian la contraseña.
- [ ] Credenciales correctas abren el dashboard con nombre y rol reales.
- [ ] Un rol sin permiso no ve acciones restringidas y el servicio también las rechaza.
- [ ] Cerrar sesión vuelve al login sin modificar la base ni conservar la sesión.

## Elecciones y voto

- [ ] Solo aparecen elecciones activas dentro del periodo.
- [ ] Seleccionar una elección carga únicamente candidaturas habilitadas.
- [ ] Seleccionar una candidatura no emite el voto.
- [ ] Cancelar la confirmación no ejecuta el procedimiento.
- [ ] Confirmar ejecuta una sola emisión y bloquea clics repetidos.
- [ ] El comprobante aparece solo tras éxito y no muestra identidad ni candidatura.
- [ ] Copiar código coloca el valor completo en el portapapeles.
- [ ] Un segundo intento para la misma elección falla de forma segura.

## Verificación

- [ ] Código con longitud/formato inválido se rechaza sin consultar MySQL.
- [ ] Código sintético inexistente muestra un mensaje seguro.
- [ ] Código real conservado confirma registro y fecha sin revelar candidatura.
- [ ] Salir de la vista no conserva el código en estado de aplicación.

## Resultados

- [ ] Elección activa o programada no publica conteos parciales.
- [ ] Sin elección finalizada aparece un estado vacío comprensible.
- [ ] Una elección finalizada y cerrada muestra listas, votos y porcentajes coherentes.
- [ ] Usuario sin `CONSULTAR_RESULTADOS` no accede a la función.

## Cierre

- [ ] Todos los controles visibles del dashboard responden o están intencionalmente deshabilitados.
- [ ] “Elecciones y candidaturas” abre el flujo electoral.
- [ ] “Ver detalle” abre el mismo flujo electoral.
- [ ] No se muestran módulos fuera del MVP como si estuvieran disponibles.
- [ ] Volver al dashboard funciona desde todas las vistas.
- [ ] Cerrar la ventana termina el executor sin dejar el proceso activo.
- [ ] La consola no imprime credenciales, códigos, intención, SQL ni stack traces.
