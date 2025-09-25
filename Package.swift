// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "Facecapturecamera",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "Facecapturecamera",
            targets: ["FaceCapturingPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "FaceCapturingPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/FaceCapturingPlugin"),
        .testTarget(
            name: "FaceCapturingPluginTests",
            dependencies: ["FaceCapturingPlugin"],
            path: "ios/Tests/FaceCapturingPluginTests")
    ]
)