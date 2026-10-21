# 📲 Cómo convertir este proyecto en un APK usando GitHub (gratis)

GitHub va a **compilar el APK por ti en la nube**. Tu PC no hace el trabajo
pesado: solo subes los archivos y GitHub te devuelve el `.apk` listo para
instalar. Tiempo total: ~15 minutos la primera vez.

---

## PASO 0 — Crear una cuenta en GitHub (si no tienes)
1. Entra a **https://github.com** y crea una cuenta gratis.
2. Verifica tu correo.

---

## PASO 1 — Crear un repositorio nuevo
1. Arriba a la derecha, toca el **+** → **New repository**.
2. En "Repository name" escribe:  **MiRespaldoFamiliar**
3. Déjalo en **Public** (o Private, da igual; el APK lo verás igual).
4. **NO** marques "Add a README".
5. Toca **Create repository**.

Te quedará una página que dice *"…or push an existing repository…"*.
Deja esa pestaña abierta.

---

## PASO 2 — Subir los archivos del proyecto

### Opción A — Arrastrando (la más fácil, sin comandos)
1. En la página del repo, toca el enlace **"uploading an existing file"**
   (o ve a **Add file → Upload files**).
2. Abre la carpeta `MiRespaldoFamiliar` en tu PC.
3. **Selecciona TODO lo de adentro** (Ctrl+A) y arrástralo a la ventana de GitHub.
   - Importante: arrastra el **contenido** de la carpeta, no la carpeta en sí.
   - Debe incluir: `app/`, `gradle/`, `.github/`, `gradlew`, `build.gradle`, etc.
4. Espera a que suban todos (puede tardar un par de minutos).
5. Abajo toca **Commit changes**.

> ⚠️ Si la web no te deja arrastrar carpetas, usa la Opción B.

### Opción B — Con GitHub Desktop (recomendada si la A falla)
1. Descarga **GitHub Desktop**: https://desktop.github.com
2. Instálalo e inicia sesión con tu cuenta.
3. **File → Clone repository →** elige *MiRespaldoFamiliar* → Clone.
4. Copia TODO el contenido de la carpeta del proyecto dentro de la
   carpeta que clonó GitHub Desktop.
5. En GitHub Desktop verás los archivos nuevos. Abajo escribe un mensaje
   (ej. "primera version") y toca **Commit to main**.
6. Arriba toca **Push origin**.

---

## PASO 3 — GitHub compila el APK solo
En cuanto subes los archivos, se dispara la compilación automáticamente.

1. En tu repo, ve a la pestaña **Actions** (arriba).
2. Verás un trabajo llamado **"Compilar APK"** corriendo (círculo amarillo 🟡).
3. Espera a que termine (~3-6 min). Cuando quede un ✅ verde, está listo.

> Si ves una ✗ roja, abre el trabajo, copia el error y mándamelo;
> te digo exactamente qué ajustar.

---

## PASO 4 — Descargar el APK

### Desde "Releases" (ideal para bajarlo al celular)
1. En la página principal del repo, a la derecha toca **Releases**.
2. Verás **MiRespaldo v1.0.x** → en "Assets" está el archivo **`app-release.apk`**.
3. Desde el celular, abre esa página y toca el `.apk` para descargarlo.

### O desde "Actions" (como archivo comprimido)
1. Pestaña **Actions** → entra al trabajo ✅ terminado.
2. Abajo, en **Artifacts**, descarga **MiRespaldo-APK** (es un .zip; adentro está el .apk).

---

## PASO 5 — Instalar el APK en cada teléfono
1. Pasa el `.apk` al teléfono (o ábrelo desde la página de Releases).
2. Al abrirlo, Android dirá *"Instalación de apps desconocidas"* →
   dale **Permitir / Configuración** y activa el permiso para tu navegador
   o gestor de archivos. (Es normal: pasa con toda app fuera de Play Store.)
3. Instala. Abre **MiRespaldo**.
4. Pon el nombre del teléfono, la **IP de tu PC** (la que muestra la ventana
   negra del Vigilante Vecinal, ej. `192.168.0.100`), elige **Compartir siempre**
   o **Solo cuando yo lo active**, y toca **GUARDAR Y PERMITIR ACCESO**.
5. Concede el permiso de archivos cuando lo pida.

¡Listo! Ese teléfono ya aparece en tu PC.

---

## PASO 6 — Verlo en la PC
1. Ten corriendo el **Vigilante Vecinal** en tu PC (doble clic en `INICIAR.bat`).
2. En el navegador de la PC entra a:  **http://localhost:3000/respaldos.html**
3. Verás los teléfonos de tu familia que activaron el acceso.
   Toca uno → explora sus carpetas → toca un archivo para **descargarlo/respaldarlo**.

---

## Para actualizar la app más adelante
Si cambiamos algo del código, solo vuelves a subir los archivos (Paso 2).
GitHub recompila solo y genera un nuevo APK en Releases. Fácil.

---

### Resumen de por qué así
- Tu PC (Pentium, 8GB) no aguanta bien Android Studio → **GitHub compila por ti**.
- No pagas nada: GitHub Actions es gratis para esto.
- El APK queda firmado para instalación directa (no necesita Play Store).
