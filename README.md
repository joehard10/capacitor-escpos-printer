# capacitor-escpos-printer

Capacitor plugin for printing receipts to Bluetooth, Wifi and USB printers.Enables Ionic/Capacitor apps to send ESC/POS commands or plain text to thermal or dot-matrix printers, allowing seamless printing of tickets, invoices and reports directly from Android and iOS.

## Install

```bash
npm install capacitor-escpos-printer
npx cap sync
```

## API

<docgen-index>

* [`listPairedDevices()`](#listpaireddevices)
* [`connect(...)`](#connect)
* [`disconnect()`](#disconnect)
* [`isConnected()`](#isconnected)
* [`printText(...)`](#printtext)
* [`setBold(...)`](#setbold)
* [`setAlignment(...)`](#setalignment)
* [`setTextSize(...)`](#settextsize)
* [`cutPaper()`](#cutpaper)
* [`connectWifi(...)`](#connectwifi)
* [`disconnectWifi()`](#disconnectwifi)
* [`isWifiConnected()`](#iswificonnected)
* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`listUsbDevices()`](#listusbdevices)
* [`connectUsb(...)`](#connectusb)
* [`disconnectUsb()`](#disconnectusb)
* [`isUsbConnected()`](#isusbconnected)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### listPairedDevices()

```typescript
listPairedDevices() => Promise<{ devices: { name: string; address: string; }[]; }>
```

**Returns:** <code>Promise&lt;{ devices: { name: string; address: string; }[]; }&gt;</code>

--------------------


### connect(...)

```typescript
connect(options: { deviceName: string; }) => Promise<{ connected: boolean; }>
```

| Param         | Type                                 |
| ------------- | ------------------------------------ |
| **`options`** | <code>{ deviceName: string; }</code> |

**Returns:** <code>Promise&lt;{ connected: boolean; }&gt;</code>

--------------------


### disconnect()

```typescript
disconnect() => Promise<{ disconnected: boolean; }>
```

**Returns:** <code>Promise&lt;{ disconnected: boolean; }&gt;</code>

--------------------


### isConnected()

```typescript
isConnected() => Promise<{ connected: boolean; }>
```

**Returns:** <code>Promise&lt;{ connected: boolean; }&gt;</code>

--------------------


### printText(...)

```typescript
printText(options: { text: string; }) => Promise<{ success: boolean; }>
```

| Param         | Type                           |
| ------------- | ------------------------------ |
| **`options`** | <code>{ text: string; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### setBold(...)

```typescript
setBold(options: { bold: boolean; }) => Promise<{ success: boolean; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ bold: boolean; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### setAlignment(...)

```typescript
setAlignment(options: { align: 'left' | 'center' | 'right'; }) => Promise<{ success: boolean; }>
```

| Param         | Type                                                   |
| ------------- | ------------------------------------------------------ |
| **`options`** | <code>{ align: 'left' \| 'center' \| 'right'; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### setTextSize(...)

```typescript
setTextSize(options: { size: number; }) => Promise<{ success: boolean; }>
```

| Param         | Type                           |
| ------------- | ------------------------------ |
| **`options`** | <code>{ size: number; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### cutPaper()

```typescript
cutPaper() => Promise<{ success: boolean; }>
```

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### connectWifi(...)

```typescript
connectWifi(options: { ip: string; port: number; }) => Promise<{ connected: boolean; }>
```

| Param         | Type                                       |
| ------------- | ------------------------------------------ |
| **`options`** | <code>{ ip: string; port: number; }</code> |

**Returns:** <code>Promise&lt;{ connected: boolean; }&gt;</code>

--------------------


### disconnectWifi()

```typescript
disconnectWifi() => Promise<{ disconnected: boolean; }>
```

**Returns:** <code>Promise&lt;{ disconnected: boolean; }&gt;</code>

--------------------


### isWifiConnected()

```typescript
isWifiConnected() => Promise<{ connected: boolean; }>
```

**Returns:** <code>Promise&lt;{ connected: boolean; }&gt;</code>

--------------------


### checkPermissions()

```typescript
checkPermissions() => Promise<{ bluetooth: string; }>
```

**Returns:** <code>Promise&lt;{ bluetooth: string; }&gt;</code>

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<{ bluetooth: string; }>
```

**Returns:** <code>Promise&lt;{ bluetooth: string; }&gt;</code>

--------------------


### listUsbDevices()

```typescript
listUsbDevices() => Promise<{ devices: { name: string; vendorId: number; productId: number; }[]; }>
```

**Returns:** <code>Promise&lt;{ devices: { name: string; vendorId: number; productId: number; }[]; }&gt;</code>

--------------------


### connectUsb(...)

```typescript
connectUsb(options: { vendorId: number; productId: number; }) => Promise<{ connected: boolean; }>
```

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code>{ vendorId: number; productId: number; }</code> |

**Returns:** <code>Promise&lt;{ connected: boolean; }&gt;</code>

--------------------


### disconnectUsb()

```typescript
disconnectUsb() => Promise<{ disconnected: boolean; }>
```

**Returns:** <code>Promise&lt;{ disconnected: boolean; }&gt;</code>

--------------------


### isUsbConnected()

```typescript
isUsbConnected() => Promise<{ connected: boolean; }>
```

**Returns:** <code>Promise&lt;{ connected: boolean; }&gt;</code>

--------------------

</docgen-api>
