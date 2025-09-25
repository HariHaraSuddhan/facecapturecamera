import { registerPlugin } from '@capacitor/core';

import type { FaceCapturingPlugin } from './definitions';

const FaceCapturing = registerPlugin<FaceCapturingPlugin>('FaceCapturing', {
  web: () => import('./web').then(m => new m.FaceCapturingWeb()),
});

export * from './definitions';
export { FaceCapturing };
