# 🚀 INICIO RÁPIDO - Mercado Pago

## ⚡ 3 pasos para activar pagos reales

### 1️⃣ Crea tu cuenta
Ve a: **https://www.mercadopago.com.pe** y regístrate (es gratis)

### 2️⃣ Obtén tu token
1. Entra a: https://www.mercadopago.com.pe/developers/panel/app
2. Crea una aplicación
3. Copia tu **"Access Token de prueba"** (comienza con TEST-)

### 3️⃣ Configúralo en la app
Abre el archivo:
```
app/src/main/java/com/ropa/smartfashionecommerce/network/MercadoPagoService.kt
```

Busca esta línea (línea 156):
```kotlin
private const val ACCESS_TOKEN = "TEST-YOUR_ACCESS_TOKEN_HERE"
```

Reemplázala con tu token:
```kotlin
private const val ACCESS_TOKEN = "TEST-1234567890-123456-abcdef..."
```

**¡Y listo!** 🎉 Ya puedes recibir pagos.

---

## 🧪 Probar con tarjetas de prueba

Usa estos datos en el checkout:

**Tarjeta aprobada:**
- Número: `5031 7557 3453 0604`
- CVV: `123`
- Vencimiento: `11/25`
- Nombre: `APRO`

---

## 📖 Documentación completa

Lee el archivo **MERCADOPAGO_SETUP.md** para instrucciones detalladas.

---

## ⚠️ IMPORTANTE

- Para testing: Usa el token `TEST-...`
- Para producción: Usa el token `APP_USR-...`
- NUNCA subas tu token a GitHub público

---

## 💰 Métodos de pago incluidos

Con Mercado Pago tus clientes pueden pagar con:
- 💜 Yape
- 💳 Tarjetas (Visa, Mastercard)
- 📱 Banca móvil
- 💵 Efectivo (PagoEfectivo, Tambo+)

Todo en una sola integración. 🚀
