import SwiftUI
import Shared

@main
struct LightsApp: App {
    var body: some Scene {
        WindowGroup {
            // Edge to edge: the Compose UI handles the safe area itself (see MainViewController.kt)
            ComposeView().ignoresSafeArea()
        }
    }
}

/** Hosts the shared Compose Multiplatform UI (the `Shared` Kotlin framework). */
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

