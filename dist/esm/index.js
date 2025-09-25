import { registerPlugin } from '@capacitor/core';
const FaceCapturing = registerPlugin('FaceCapturing', {
    web: () => import('./web').then(m => new m.FaceCapturingWeb()),
});
export * from './definitions';
export { FaceCapturing };
//# sourceMappingURL=index.js.map