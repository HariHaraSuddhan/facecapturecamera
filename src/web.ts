import { WebPlugin } from '@capacitor/core';
import type { FaceCapturingPlugin } from './definitions';

export class FaceCapturingWeb extends WebPlugin implements FaceCapturingPlugin {
  async startCamera(): Promise<void> {
    console.warn('FaceCapture not supported on web');
  }
  async stopCamera(): Promise<void> { }
  async capture() { return { reason: 'not_supported' }; }
  // async addListener() { return { remove() {} }; }
}

const FaceCapture = new FaceCapturingWeb();
export { FaceCapture };
