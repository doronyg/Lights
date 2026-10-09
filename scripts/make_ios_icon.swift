// Usage: swift make_ios_icon.swift <input.png> <output.png>
// Renders the input at 1024x1024 on an opaque background (iOS app icons must not have alpha).
// Background: the input's top-left pixel color if it's opaque, else the Android launcher green.
import AppKit

let args = CommandLine.arguments
guard args.count == 3, let src = NSImage(contentsOfFile: args[1]),
      let cg = src.cgImage(forProposedRect: nil, context: nil, hints: nil) else {
    fputs("usage: make_ios_icon <in.png> <out.png>\n", stderr); exit(1)
}

let rep = NSBitmapImageRep(cgImage: cg)
var bg = NSColor(srgbRed: 0x3D / 255.0, green: 0xDC / 255.0, blue: 0x84 / 255.0, alpha: 1)
if let corner = rep.colorAt(x: 0, y: 0), corner.alphaComponent > 0.99 { bg = corner }

let size = 1024
let ctx = CGContext(data: nil, width: size, height: size, bitsPerComponent: 8, bytesPerRow: 0,
                    space: CGColorSpace(name: CGColorSpace.sRGB)!,
                    bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue)!
ctx.interpolationQuality = .high
ctx.setFillColor(bg.cgColor)
ctx.fill(CGRect(x: 0, y: 0, width: size, height: size))
ctx.draw(cg, in: CGRect(x: 0, y: 0, width: size, height: size))

let out = NSBitmapImageRep(cgImage: ctx.makeImage()!)
try! out.representation(using: .png, properties: [:])!.write(to: URL(fileURLWithPath: args[2]))
print("wrote \(args[2]) (background \(bg))")

