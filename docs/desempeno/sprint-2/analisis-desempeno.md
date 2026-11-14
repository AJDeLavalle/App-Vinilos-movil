# Análisis de desempeño: primera medición (T20)

Línea base del desempeño de la app para comparar en la medición final (T30).

**Conclusión:** arranque, memoria y CPU están en un rango sano. El mayor retraso que percibe el usuario viene de la red, sobre todo cuando el backend de Firebase está inactivo. La fluidez no se puede concluir en emulador y debe confirmarse en un dispositivo físico.

## 1. Entorno

| Elemento | Valor |
|---|---|
| Fecha | 2026-11-04 |
| Código medido | Commit `dae81c4` (`feature/T24-coleccionistas`): incluye artistas y coleccionistas, aún sin fusionar en `develop` |
| APK | `app-release.apk`, sin R8, firmado con la llave de debug |
| Dispositivo | Emulador `Medium_Phone`, Android 17 (API 37), x86_64 |
| Backend | Firebase (Cloud Functions) |
| Datos | 4 álbumes, 2 artistas y 2 coleccionistas |

Los datos crudos están en [`medicion.txt`](medicion.txt).

## 2. Resultados

### Tamaño

| APK | Tamaño |
|---|---|
| Release | 7,72 MB |
| Debug | 11,75 MB |

### Arranque

`TotalTime` de `am start -W`: tiempo hasta el primer dibujo de la pantalla, sin contar la carga de datos.

| Tipo | Mediana | Mín. | Máx. | Referencia Android vitals |
|---|---|---|---|---|
| Primer arranque tras instalar (1 medición) | 3.013 ms | | | |
| Frío (10 mediciones) | 819 ms | 697 ms | 1.112 ms | Excesivo desde 5 s |
| Caliente (10 mediciones) | 96 ms | 73 ms | 131 ms | Excesivo desde 1,5 s |

### Memoria (PSS)

| Pantalla | PSS |
|---|---|
| Catálogo | 61,5 MB |
| Detalle de álbum | 60,2 MB |
| Artistas | 62,3 MB |
| Detalle de artista | 60,5 MB |
| Coleccionistas | 63,5 MB |
| Detalle de coleccionista | 61,4 MB |

En 5 vueltas por todas las pantallas el PSS fue 64,8, 65,2, 63,3, 63,8 y 64,1 MB. No hay crecimiento sostenido, así que no hay señales de fuga en este recorrido.

### Fluidez

Una vuelta por las pantallas con desplazamiento en los detalles.

| Métrica | Valor |
|---|---|
| Fotogramas renderizados | 244 |
| Fotogramas con tirones | 57 (23,36 %) |
| Percentil 50 / 90 / 95 / 99 | 27 / 48 / 53 / 85 ms |

Es la cifra menos confiable del informe: el emulador renderiza sin la GPU de un teléfono y una sola vuelta es una muestra pequeña. No demuestra que la app vaya lenta ni que vaya bien.

### CPU en reposo

5 muestras: mediana 0 %, máximo 28 %. La app no consume CPU de forma continua; el pico aislado no se investigó.

### API

5 solicitudes por recurso desde el computador con `curl`. Cada una abre una conexión nueva, así que son más lentas que en la app, que reutiliza la conexión.

| Recurso | Mediana | Tamaño |
|---|---|---|
| `/albums` | 612 ms | 4.712 B |
| `/albums/100` | 640 ms | 1.162 B |
| `/musicians` | 563 ms | 1.668 B |
| `/bands` | 586 ms | 1.816 B |
| `/prizes` | 551 ms | 731 B |
| `/collectors` | 590 ms | 1.530 B |
| `/collectors/100/albums` | 605 ms | 684 B |

Observado en los registros de la app (build debug): la primera carga de `/albums` tardó 7,7 s, 6,6 s y 1,5 s en tres aperturas distintas, y las siguientes solicitudes entre 0,2 y 1 s. Son observaciones sueltas, no una medición controlada.

## 3. Hallazgos y mejoras propuestas

| # | Hallazgo | Impacto | Propuesta |
|---|---|---|---|
| 1 | La primera carga puede tardar unos 7 s, probablemente por el arranque en frío de Cloud Functions | Alto, pero fuera de la app | Mantener el indicador de carga. Si importa para la demo, abrir la app antes de presentarla |
| 2 | Listado de artistas y detalles de artista y coleccionista hacen dos solicitudes en serie | Medio: suma una ida y vuelta | Hacer en paralelo `/musicians` y `/bands`. Medir antes y después |
| 3 | No hay caché: cada apertura de un detalle vuelve a consultar la red | Medio | Caché en memoria en los repositorios, o caché HTTP de OkHttp |
| 4 | Release sin R8, desactivado porque la app se cerraba al abrir | Bajo | Reactivarlo con reglas de conservación podría reducir el APK; hay que probarlo con cuidado |
| 5 | 23 % de fotogramas con tirones en emulador | Por confirmar | Repetir en dispositivo físico con Android Studio Profiler |

## 4. Límites de esta medición

- Todo se midió en emulador, en una sola sesión. Los valores absolutos cambiarán en un teléfono real.
- Hay muy pocos datos: el backend tiene 4 álbumes, así que no se probó el desplazamiento de listas largas.
- `TotalTime` no incluye la llegada de los datos; no se midió el tiempo hasta ver el catálogo completo.
- No se usó Android Studio Profiler, por lo que no hay trazas de CPU ni análisis del heap.
- La carga de imágenes no quedó bien representada: varias URL del backend están rotas.

## 5. Cómo repetir la medición (T30)

Con un emulador o teléfono conectado, desde la raíz del repositorio:

```bash
./gradlew assembleRelease
```

```bash
./docs/desempeno/medir-desempeno.sh app/build/outputs/apk/release/app-release.apk 10
```

Para que la comparación sea válida, usar el mismo emulador, el APK release y las mismas repeticiones. Cerrar otras aplicaciones del computador antes de medir.
