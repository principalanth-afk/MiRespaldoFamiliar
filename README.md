# 🛡️ MiRespaldo Familiar

App Android para que la PC de la casa pueda **explorar y respaldar** los
archivos de los teléfonos de la familia, **con su consentimiento** y solo
dentro de la red WiFi del hogar. Complemento del proyecto *Vigilante Vecinal*.

## ¿Cómo funciona?
- Cada teléfono instala esta app una vez.
- Al abrirla, la persona elige **Compartir siempre** o **Solo cuando yo lo active**,
  pone un nombre y la IP de la PC.
- La app corre un mini-servidor (puerto 8787) que expone **solo lectura** de
  las carpetas del teléfono y avisa a la PC que está disponible.
- Desde la PC entras a `http://localhost:3000/respaldos.html` y navegas/descargas.

## Compilar el APK
No necesitas Android Studio. **GitHub lo compila por ti.**
👉 Lee **COMO-SUBIR-A-GITHUB.md** — paso a paso.

## Privacidad y consentimiento
- El acceso es de **solo lectura** (no borra ni modifica nada en el teléfono).
- La persona puede **apagar** el acceso cuando quiera abriendo la app.
- Funciona únicamente dentro de la misma red WiFi (no por internet).
- Pensado para uso familiar con permiso explícito de cada quien.

## Estructura
```
app/                      -> código de la app (Kotlin)
  src/main/java/...       -> MainActivity, servidor y servicio
  src/main/res/...        -> pantalla e iconos
.github/workflows/        -> compilación automática del APK
gradle*, build.gradle     -> configuración de compilación
```
