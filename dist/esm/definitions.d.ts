export interface FaceCaptureResult {
    imageBase64?: string;
    reason?: string;
}
export interface FaceCapturingPlugin {
    startCamera(options?: {
        camera?: 'front' | 'back';
    }): Promise<void>;
    stopCamera(): Promise<void>;
    capture(): Promise<FaceCaptureResult>;
    addListener(eventName: 'faceDetected' | 'faceLost' | 'captureComplete', listenerFunc: (info: any) => void): Promise<{
        remove: () => void;
    }>;
}
