# Diseño del formulario de crear álbum (T16, HU07)

Define qué pide el formulario de la maqueta P07, cómo se valida y cómo se envía al backend. Es la base de T17 (formulario), T18 (envío) y T19 (pruebas).

Fuente del contrato: `src/album/album.service.ts`, `src/album/album.dto.ts` y los controladores `albummusician` y `albumband` de [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils).

## 1. Diferencias entre la maqueta P07 y el backend

| Tema | Maqueta P07 | Backend | Decisión propuesta |
|---|---|---|---|
| Portada | No aparece | `cover` obligatorio | Agregar campo "URL de la portada" |
| Descripción | No aparece | `description` obligatorio | Agregar campo de texto de varias líneas |
| Sello discográfico | No aparece | `recordLabel` obligatorio | Agregar selector |
| Artista | Selector en el formulario | No es parte de `POST /albums` | Mantener el selector y asociarlo con una segunda llamada (sección 4) |
| Género | La maqueta usa "Pop / Rock" como ejemplo | Solo `Classical`, `Salsa`, `Rock`, `Folk` | Usar solo los valores del backend |
| Fecha | `DD/MM/AAAA` | Fecha válida para Joi | Mostrar `DD/MM/AAAA` y enviar `AAAA-MM-DD` |

La maqueta debe actualizarse con los tres campos nuevos.

## 2. Campos y validaciones

Las reglas marcadas como **backend** son obligatorias: si no se cumplen, el servidor responde 400. Las marcadas como **propuesta** son decisiones de la app para mejorar la calidad de los datos; el equipo puede ajustarlas.

| Campo | Control | Reglas | Origen | Mensaje de error |
|---|---|---|---|---|
| Nombre | Texto | Obligatorio; no vacío después de quitar espacios | Backend | "Ingresa el nombre del álbum." |
| | | Máximo 100 caracteres | Propuesta | "El nombre admite hasta 100 caracteres." |
| Artista | Selector (músicos y bandas) | Obligatorio | Propuesta (maqueta) | "Selecciona un artista." |
| Género | Selector | Obligatorio; uno de `Classical`, `Salsa`, `Rock`, `Folk` | Backend | "Selecciona un género." |
| Sello discográfico | Selector | Obligatorio; uno de `Sony Music`, `EMI`, `Discos Fuentes`, `Elektra`, `Fania Records` | Backend | "Selecciona un sello discográfico." |
| Fecha de lanzamiento | Fecha `DD/MM/AAAA` | Obligatoria; debe ser una fecha real | Backend | "Ingresa una fecha válida (DD/MM/AAAA)." |
| | | No puede ser futura | Propuesta | "La fecha no puede ser futura." |
| URL de la portada | Texto (teclado URL) | Obligatoria | Backend | "Ingresa la URL de la portada." |
| | | Debe iniciar con `http://` o `https://` | Propuesta | "Ingresa una URL válida." |
| Descripción | Texto de varias líneas | Obligatoria; no vacía después de quitar espacios | Backend | "Ingresa una descripción." |
| | | Máximo 500 caracteres | Propuesta | "La descripción admite hasta 500 caracteres." |

Comportamiento:

- Cada campo se valida al salir de él y todos se validan al pulsar **Guardar álbum**.
- El error se muestra debajo del campo. Al corregirlo, el mensaje desaparece.
- Al pulsar Guardar con errores no se envía nada y el foco va al primer campo inválido.
- Los valores de género y sello se muestran y se envían tal como los define el backend.

## 3. Envío al backend

`POST /albums` responde 200 con el álbum creado, incluido su `id`.

```json
{
  "name": "Buscando América",
  "cover": "https://ejemplo.com/portada.jpg",
  "releaseDate": "1984-08-01",
  "description": "Primer álbum de la banda Seis del Solar.",
  "genre": "Salsa",
  "recordLabel": "Elektra"
}
```

El esquema de Joi no admite campos adicionales, así que el cuerpo debe tener exactamente estos seis. El artista no se incluye aquí.

## 4. Asociar el artista

Después de crear el álbum, según el tipo de artista:

- Músico: `POST /albums/{albumId}/musicians/{musicianId}`
- Banda: `POST /albums/{albumId}/bands/{bandId}`

El tipo se conoce porque el selector se llena con `getArtists`, disponible en la rama de T13/T14.

## 5. Estados y errores

| Situación | Comportamiento |
|---|---|
| Enviando | Botón deshabilitado con indicador de carga, para evitar álbumes duplicados |
| Todo correcto | Regresa al catálogo, que se recarga y muestra el álbum nuevo |
| Falla la red en `POST /albums` | Mensaje con opción de reintentar; se conservan los datos del formulario |
| Responde 400 | "Revisa los datos del álbum." Se conservan los datos. No debería ocurrir si las validaciones están bien |
| Falla la asociación del artista | El álbum **ya existe**: no se debe repetir `POST /albums`. Se avisa y se reintenta solo la asociación. Si el usuario desiste, regresa al catálogo |
| No cargan los artistas | Error con reintento en el selector; no se puede guardar sin artista |

## 6. Casos de prueba sugeridos (T19)

| Caso | Acción | Resultado esperado |
|---|---|---|
| CP-21 | Guardar con el formulario vacío | Un error por campo; no se envía nada |
| CP-22 | Fecha `31/02/2020` | Error de fecha válida |
| CP-23 | Fecha futura | Error de fecha futura |
| CP-24 | URL sin `http` | Error de URL |
| CP-25 | Datos válidos de un músico | `POST /albums` y luego `/musicians/{id}`; el álbum aparece en el catálogo |
| CP-26 | Datos válidos de una banda | `POST /albums` y luego `/bands/{id}` |
| CP-27 | Falla la red al guardar | Se muestra error y se conservan los datos |
| CP-28 | Falla la asociación | Reintentar no crea un álbum duplicado |
| CP-29 | Doble toque en Guardar | Solo se envía una solicitud |

Las pruebas no deben crear álbumes en el backend de Firebase, que es compartido: usar un repositorio falso o MockWebServer.

## 7. Decisiones que el equipo debe confirmar

1. Aceptar los tres campos nuevos en la maqueta P07.
2. Que el artista sea obligatorio. El backend permite álbumes sin artista, pero la maqueta lo pide.
3. Los límites propuestos: 100 y 500 caracteres, y no aceptar fechas futuras.
