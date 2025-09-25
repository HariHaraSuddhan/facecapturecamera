var capacitorFaceCapturing = (function (exports, core) {
    'use strict';

    const FaceCapturing = core.registerPlugin('FaceCapturing', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.FaceCapturingWeb()),
    });

    class FaceCapturingWeb extends core.WebPlugin {
        async startCamera() {
            console.warn('FaceCapture not supported on web');
        }
        async stopCamera() { }
        async capture() { return { reason: 'not_supported' }; }
    }
    const FaceCapture = new FaceCapturingWeb();

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        FaceCapturingWeb: FaceCapturingWeb,
        FaceCapture: FaceCapture
    });

    exports.FaceCapturing = FaceCapturing;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
