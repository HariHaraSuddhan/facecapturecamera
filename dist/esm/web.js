import { WebPlugin } from '@capacitor/core';
export class FaceCapturingWeb extends WebPlugin {
    async startCamera() {
        console.warn('FaceCapture not supported on web');
    }
    async stopCamera() { }
    async capture() { return { reason: 'not_supported' }; }
}
const FaceCapture = new FaceCapturingWeb();
export { FaceCapture };
//# sourceMappingURL=web.js.map