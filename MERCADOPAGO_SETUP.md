# 💳 Configuración de Mercado Pago en SmartFashion

## 🎯 ¿Qué es Mercado Pago?

Mercado Pago es una plataforma de pagos **100% GRATUITA** que te permite recibir pagos en Perú mediante:
- 💜 **Yape**
- 💳 **Tarjetas de crédito/débito** (Visa, Mastercard, etc.)
- 📱 **Banca móvil** (BCP, Interbank, BBVA, etc.)
- 💵 **Efectivo** (PagoEfectivo, Tambo+, Kasnet, etc.)

---

## 🚀 Paso 1: Crear tu cuenta de Mercado Pago

1. Ve a: **https://www.mercadopago.com.pe**
2. Haz clic en **"Regístrate"** o **"Crear cuenta"**
3. Completa tus datos:
   - Correo electrónico
   - Crea una contraseña
   - Ingresa tu número de DNI
4. Verifica tu correo electrónico
5. ¡Listo! Ya tienes tu cuenta creada

---

## 🔑 Paso 2: Obtener tus credenciales (ACCESS TOKEN)

### Para TESTING (pruebas):

1. Inicia sesión en **https://www.mercadopago.com.pe**
2. Ve a: **https://www.mercadopago.com.pe/developers/panel/app**
3. Haz clic en **"Crear aplicación"** o selecciona una existente
4. En el panel de tu aplicación, busca la sección **"Credenciales"**
5. Verás dos tipos de credenciales:
   - ✅ **Credenciales de prueba** (para testing)
   - 🔒 **Credenciales de producción** (para tu app real)

6. Copia el **"Access Token de prueba"** (comienza con `TEST-`)

### Para PRODUCCIÓN (app real):

1. En el mismo panel, activa el **"Modo producción"**
2. Copia el **"Access Token de producción"** (comienza con `APP_USR-`)

---

## 🛠️ Paso 3: Configurar el ACCESS TOKEN en tu app

1. Abre el archivo:
   ```
   app/src/main/java/com/ropa/smartfashionecommerce/network/MercadoPagoService.kt
   ```

2. Busca la línea que dice:
   ```kotlin
   private const val ACCESS_TOKEN = "TEST-YOUR_ACCESS_TOKEN_HERE"
   ```

3. Reemplázala con tu token:
   ```kotlin
   private const val ACCESS_TOKEN = "TEST-1234567890-123456-abcdef123456789-123456789"
   ```

4. **IMPORTANTE**: 
   - Para pruebas, usa el token que comienza con `TEST-`
   - Para producción, usa el token que comienza con `APP_USR-`

---

## ✅ Paso 4: Probar la integración

### Modo Testing:

1. Asegúrate de usar el **Access Token de prueba** (TEST-...)
2. Compila y ejecuta la app
3. Agrega productos al carrito
4. Ve a **"Finalizar compra"**
5. Completa tus datos
6. Haz clic en **"Pagar con Mercado Pago"**
7. Se abrirá el navegador con el checkout de Mercado Pago
8. **Usa tarjetas de prueba:**

   **Tarjetas de prueba aprobadas:**
   - Número: `5031 7557 3453 0604`
   - CVV: `123`
   - Vencimiento: `11/25`
   - Nombre: `APRO`

   **Más tarjetas de prueba:**
   - Visa: `4509 9535 6623 3704`
   - Mastercard: `5031 4332 1540 6351`

9. Completa el pago
10. Serás redirigido a la app con el resultado

### Lista completa de tarjetas de prueba:
https://www.mercadopago.com.pe/developers/es/docs/checkout-api/testing

---

## 🎉 Paso 5: Pasar a producción

Cuando tu app esté lista para usuarios reales:

1. Reemplaza el `ACCESS_TOKEN` con el de **producción** (APP_USR-...)
2. Completa la **verificación de identidad** en Mercado Pago:
   - Ve a tu panel de Mercado Pago
   - Sigue el proceso de verificación (suben DNI, datos bancarios)
3. Una vez verificado, ya puedes recibir pagos reales
4. El dinero llegará a tu cuenta de Mercado Pago
5. Puedes transferirlo a tu cuenta bancaria cuando quieras

---

## 💰 ¿Cuánto cobra Mercado Pago?

**Para Perú (2025):**
- Tarjetas de crédito: ~3.99% + S/ 0.50 por transacción
- Tarjetas de débito: ~2.99% + S/ 0.50
- Yape: ~2.99% + S/ 0.50
- Efectivo: Variable según punto de pago

**IMPORTANTE**: Las tarifas pueden cambiar. Verifica las tarifas actuales en:
https://www.mercadopago.com.pe/costs-section/

---

## 🔐 Seguridad

**NUNCA compartas tu Access Token:**
- ❌ No lo subas a GitHub público
- ❌ No lo compartas en chats o emails
- ❌ No lo hardcodees en el código si planeas publicar el código

**Recomendación para producción:**
- Usa variables de entorno
- O mueve las credenciales a un archivo `local.properties` (que no se sube a Git)

Ejemplo en `local.properties`:
```properties
mercadopago.access.token=APP_USR-tu-token-aqui
```

---

## 🆘 Problemas comunes

### "Mercado Pago no está configurado"
- ✅ Verifica que hayas puesto tu ACCESS TOKEN en `MercadoPagoService.kt`
- ✅ Asegúrate de que no sea el valor por defecto `TEST-YOUR_ACCESS_TOKEN_HERE`

### "Error al procesar el pago"
- ✅ Verifica tu conexión a Internet
- ✅ Comprueba que el ACCESS TOKEN sea válido
- ✅ Revisa los logs de Android Studio para más detalles

### El pago no redirige a la app
- ✅ Verifica que el `AndroidManifest.xml` tenga el intent-filter configurado
- ✅ El esquema debe ser `smartfashion://payment/success`

---

## 📚 Recursos útiles

- **Portal de desarrolladores:** https://www.mercadopago.com.pe/developers
- **Documentación API:** https://www.mercadopago.com.pe/developers/es/docs
- **Tarjetas de prueba:** https://www.mercadopago.com.pe/developers/es/docs/checkout-api/testing
- **Soporte:** https://www.mercadopago.com.pe/ayuda

---

## 🎓 Flujo completo de pago

```
1. Usuario → Agrega productos al carrito
2. Usuario → Va a "Finalizar compra"
3. Usuario → Completa datos (nombre, email, dirección, etc.)
4. Usuario → Presiona "Pagar con Mercado Pago"
5. App → Llama a PaymentManager.createMercadoPagoPayment()
6. App → Recibe URL de pago de Mercado Pago
7. App → Abre navegador con la URL
8. Usuario → Elige método de pago (Yape, tarjeta, etc.)
9. Usuario → Completa el pago
10. Mercado Pago → Procesa el pago
11. Mercado Pago → Redirige a: smartfashion://payment/success (o pending/failure)
12. App → PaymentReturnActivity captura el resultado
13. App → Registra el pedido si fue exitoso
14. App → Muestra pantalla de confirmación
```

---

## ✨ ¡Listo!

Tu app ahora puede recibir pagos reales con Mercado Pago. 🎉

Recuerda:
1. Primero prueba con el token de TEST
2. Usa las tarjetas de prueba
3. Cuando todo funcione, cambia al token de producción
4. ¡Empieza a vender!

---

**Última actualización:** Noviembre 2025
**Versión:** 1.0
