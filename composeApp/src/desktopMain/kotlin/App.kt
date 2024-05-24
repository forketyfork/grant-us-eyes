import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.FrameWindowScope
import org.jetbrains.compose.ui.tooling.preview.Preview

import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Composable
@Preview
fun FrameWindowScope.App() {
    MaterialTheme {
        var content by remember { mutableStateOf("Drop a file here") }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(content)
        }

        val target = object : DropTarget() {
            @Synchronized
            override fun drop(evt: DropTargetDropEvent) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE)
                    val droppedFiles = evt
                        .transferable.getTransferData(
                            DataFlavor.javaFileListFlavor
                        ) as List<*>
                    for (file in droppedFiles) {
                        (file as File).inputStream().buffered().use {
                            val newContent = StringBuilder(file.path + "\n")
                            newContent.appendLine(it.nextInt().toMagicWordDescription())
                            newContent.appendLine(it.nextInt().toCpuTypeDescription())
                            newContent.appendLine(it.nextInt().toCpuSubtypeDescription())
                            newContent.appendLine(it.nextInt().toFileTypeDescription())
                            newContent.append("Number of load commands: ").appendLine(it.nextInt())
                            newContent.append("Size of load commands: ").append(it.nextInt()).appendLine(" bytes")
                            newContent.appendLine(it.nextInt().toFlagsDescription())
                            newContent.append("Reserved word: ").appendLine(it.nextInt())
                            content = newContent.toString()
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        this.window.contentPane.dropTarget = target
    }
}

fun InputStream.nextInt() = ByteBuffer.wrap(readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).int

fun Int.toMagicWordDescription() = when (this) {
    0xFEEDFACE.toInt() -> "32 bit executable"
    0xFEEDFACF.toInt() -> "64 bit executable"
    else -> "Not an executable"
}

fun Int.toCpuTypeDescription() = "CPU Type: ${cpuTypes[this] ?: "Unknown ($this)"}"

fun Int.toCpuSubtypeDescription(): String {
    return "CPU Subtype: TODO" // TODO
}

fun Int.toFileTypeDescription() = ("File type: " + fileTypes[this]) ?: "Unknown ($this)"

fun Int.toFlagsDescription(): String {
    val result = StringBuilder("Flags:\n")
    var bit = 1
    for (i in (0..31)) {
        if (this and bit > 0) {
            result.appendLine("- ${flags[i]}")
        }
        bit = bit shl 1
    }
    return result.toString()
}

val cpuTypes = mapOf(
    0x01 to "VAX",
    0x02 to "ROMP",
    0x04 to "NS32032",
    0x05 to "NS32332",
    0x06 to "MC680x0",
    0x07 to "x86",
    0x01000007 to "x86_64",
    0x08 to "MIPS",
    0x09 to "NS32532",
    0x0A to "MC98000",
    0x0B to "HP-PA",
    0x0C to "ARM",
    0x0100000C to "ARM64",
    0x0D to "MC88000",
    0x0E to "SPARC",
    0x0F to "i860 (big-endian)",
    0x10 to "i860 (little-endian)",
    0x11 to "RS/6000",
    0x12 to "PowerPC",
    0x01000012 to "PowerPC64",
    0xFF to "VEO"
)

val fileTypes = mapOf(
    0x00000001 to "Relocatable object file",
    0x00000002 to "Demand paged executable file",
    0x00000003 to "Fixed VM shared library file",
    0x00000004 to "Core file",
    0x00000005 to "Preloaded executable file",
    0x00000006 to "Dynamically bound shared library file",
    0x00000007 to "Dynamic link editor",
    0x00000008 to "Dynamically bound bundle file",
    0x00000009 to "Shared library stub for static linking only, no section contents",
    0x0000000A to "Companion file with only debug sections",
    0x0000000B to "x86_64 kexts",
    0x0000000C to "a file composed of other Mach-Os to be run in the same userspace sharing a single linkedit"
)

val flags = arrayOf(
    "The object file has no undefined references",
    "The object file is the output of an incremental link against a base file and can't be link edited again",
    "The object file is input for the dynamic linker and can't be statically link edited again",
    "The object file's undefined references are bound by the dynamic linker when loaded",
    "The file has its dynamic undefined references prebound",
    "The file has its read-only and read-write segments split",
    "The shared library init routine is to be run lazily via catching memory faults to its writeable segments (obsolete)",
    "The image is using two-level name space bindings",
    "The executable is forcing all images to use flat name space bindings",
    "This umbrella guarantees no multiple definitions of symbols in its sub-images so the two-level namespace hints can always be used",
    "Do not have dyld notify the prebinding agent about this executable",
    "The binary is not prebound but can have its prebinding redone. only used when MH_PREBOUND is not set",
    "Indicates that this binary binds to all two-level namespace modules of its dependent libraries",
    "Safe to divide up the sections into sub-sections via symbols for dead code stripping",
    "The binary has been canonicalized via the un-prebind operation",
    "The final linked image contains external weak symbols",
    "The final linked image uses weak symbols",
    "When this bit is set, all stacks in the task will be given stack execution privilege",
    "When this bit is set, the binary declares it is safe for use in processes with uid zero",
    "When this bit is set, the binary declares it is safe for use in processes when UGID is true",
    "When this bit is set on a dylib, the static linker does not need to examine dependent dylibs to see if any are re-exported",
    "When this bit is set, the OS will load the main executable at a random address",
    "Only for use on dylibs. When linking against a dylib that has this bit set, the static linker will automatically not create a load command to the dylib if no symbols are being referenced from the dylib",
    "Contains a section of type S_THREAD_LOCAL_VARIABLES",
    "When this bit is set, the OS will run the main executable with a non-executable heap even on platforms (e.g. i386) that don't require it",
    "The code was linked for use in an application",
    "The external symbols listed in the nlist symbol table do not include all the symbols listed in the dyld info",
    "Allow LC_MIN_VERSION_MACOS and LC_BUILD_VERSION load commands with the platforms macOS, macCatalyst, iOSSimulator, tvOSSimulator and watchOSSimulator",
    "",
    "",
    "",
    "Only for use on dylibs. When this bit is set, the dylib is part of the dyld shared cache, rather than loose in the filesystem"
)
