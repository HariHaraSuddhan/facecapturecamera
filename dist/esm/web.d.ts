import { WebPlugin } from '@capacitor/core';
import type { FaceCapturingPlugin } from './definitions';
export declare class FaceCapturingWeb extends WebPlugin implements FaceCapturingPlugin {
    startCamera(): Promise<void>;
    stopCamera(): Promise<void>;
    capture(): Promise<{
        reason: string;
    }>;
}
declare const FaceCapture: FaceCapturingWeb;
export { FaceCapture };
