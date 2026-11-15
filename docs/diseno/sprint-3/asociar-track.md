# Diseño del formulario de asociar tracks (T25, HU08)

Define el formulario de la maqueta P08 y su envío al backend. Se abre con el botón "+ Asociar track" del detalle del álbum (P02).

Fuente del contrato: `src/track/track.service.ts` y `src/track/track.controller.ts` de [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils).

## Campos y validaciones

| Campo | Reglas | Origen | Mensaje de error |
|---|---|---|---|
| Nombre del track | Obligatorio; no vacío después de quitar espacios | Backend | "Ingresa el nombre del track." |
| | Máximo 100 caracteres | Propuesta | "El nombre admite hasta 100 caracteres." |
| Duración | Obligatoria | Backend | "Ingresa la duración." |
| | Formato `m:ss` o `mm:ss`, con segundos de 00 a 59 y mayor que cero | Propuesta | "Ingresa una duración válida (mm:ss)." |

El backend acepta cualquier texto como duración, así que el formato lo controla la app. Se admiten minutos sin cero inicial porque los datos existentes usan ese formato, como `5:05`.

Se valida al salir de cada campo y al pulsar **Asociar track**. Con errores no se envía nada y el foco va al primer campo inválido.

## Envío

`POST /albums/{albumId}/tracks` responde 200 con el track creado.

```json
{ "name": "Decisiones", "duration": "5:05" }
```

| Situación | Comportamiento |
|---|---|
| Enviando | Botón deshabilitado con indicador de carga |
| Todo correcto | Regresa al detalle del álbum, que se recarga y muestra el track nuevo |
| Falla la red | Mensaje de error; se conservan los datos para reintentar |
| Responde 400 | "Revisa los datos del track." |
| Responde 404 | "El álbum que buscas no existe." |
| No carga el álbum | Error con reintento antes de mostrar el formulario |

## Decisiones que el equipo debe confirmar

1. El formato de duración y el límite de 100 caracteres.
2. No se impiden tracks con nombre repetido, porque el backend los permite.
