# facecapturecamera

this plugin will helps to capture only the users face so that we can stop the duplicate images or any blank images tobe uploading.

## Install

```bash
npm install facecapturecamera
npx cap sync
```

## API

<docgen-index>

- [`startCamera(...)`](#startcamera)
- [`stopCamera()`](#stopcamera)
- [`capture()`](#capture)
- [`addListener('faceDetected' | 'faceLost' | 'captureComplete', ...)`](#addlistenerfacedetected--facelost--capturecomplete-)
- [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startCamera(...)

```typescript
startCamera(options?: { camera?: "front" | "back" | undefined; } | undefined) => Promise<void>
```

| Param         | Type                                         |
| ------------- | -------------------------------------------- |
| **`options`** | <code>{ camera?: 'front' \| 'back'; }</code> |

---

### stopCamera()

```typescript
stopCamera() => Promise<void>
```

---

### capture()

```typescript
capture() => Promise<FaceCaptureResult>
```

**Returns:** <code>Promise&lt;<a href="#facecaptureresult">FaceCaptureResult</a>&gt;</code>

---

### addListener('faceDetected' | 'faceLost' | 'captureComplete', ...)

```typescript
addListener(eventName: 'faceDetected' | 'faceLost' | 'captureComplete', listenerFunc: (info: any) => void) => Promise<{ remove: () => void; }>
```

| Param              | Type                                                           |
| ------------------ | -------------------------------------------------------------- |
| **`eventName`**    | <code>'faceDetected' \| 'faceLost' \| 'captureComplete'</code> |
| **`listenerFunc`** | <code>(info: any) =&gt; void</code>                            |

**Returns:** <code>Promise&lt;{ remove: () =&gt; void; }&gt;</code>

---

### Interfaces

#### FaceCaptureResult

| Prop              | Type                |
| ----------------- | ------------------- |
| **`imageBase64`** | <code>string</code> |
| **`reason`**      | <code>string</code> |

</docgen-api>

<!--

                                        if (!faces.isEmpty()) {
                                            if (!facePresent) {
                                                facePresent = true;
                                                if (FaceCameraPlugin.instance != null) {
                                                    FaceCameraPlugin.instance.notifyFaceDetected();
                                                }
                                            }
                                        } else {
                                            if (facePresent) {
                                                facePresent = false;
                                                if (FaceCameraPlugin.instance != null) {
                                                    FaceCameraPlugin.instance.notifyFaceLost();
                                                }
                                            }
                                        } -->
