package objects.libraries

import objects.base.stringOf
import runtime.NameTable


fun getStringLibrary(): FlModuleObj {
    val textConstants = FlModuleObj("stringlib.constants", null, NameTable("stringlib.constants"))

    textConstants.setValue("RESET", stringOf("\u001B[0m"))
    textConstants.setValue("BLACK", stringOf("\u001B[0;30m"))
    textConstants.setValue("RED", stringOf("\u001B[0;31m"))
    textConstants.setValue("GREEN", stringOf("\u001B[0;32m"))
    textConstants.setValue("BROWN", stringOf("\u001B[0;33m"))
    textConstants.setValue("BLUE", stringOf("\u001B[0;34m"))
    textConstants.setValue("PURPLE", stringOf("\u001B[0;35m"))
    textConstants.setValue("CYAN", stringOf("\u001B[0;36m"))
    textConstants.setValue("LIGHT_GRAY", stringOf("\u001B[0;37m"))
    textConstants.setValue("DARK_GRAY", stringOf("\u001B[1;30m"))
    textConstants.setValue("LIGHT_RED", stringOf("\u001B[1;31m"))
    textConstants.setValue("LIGHT_GREEN", stringOf("\u001B[1;32m"))
    textConstants.setValue("YELLOW", stringOf("\u001B[1;33m"))
    textConstants.setValue("LIGHT_BLUE", stringOf("\u001B[1;34m"))
    textConstants.setValue("LIGHT_PURPLE", stringOf("\u001B[1;35m"))
    textConstants.setValue("LIGHT_CYAN", stringOf("\u001B[1;36m"))
    textConstants.setValue("LIGHT_WHITE", stringOf("\u001B[1;37m"))
    textConstants.setValue("BOLD", stringOf("\u001B[1m"))
    textConstants.setValue("FAINT", stringOf("\u001B[2m"))
    textConstants.setValue("ITALIC", stringOf("\u001B[3m"))
    textConstants.setValue("UNDERLINE", stringOf("\u001B[4m"))
    textConstants.setValue("BLINK", stringOf("\u001B[5m"))
    textConstants.setValue("NEGATIVE", stringOf("\u001B[7m"))
    textConstants.setValue("CROSSED", stringOf("\u001B[9m"))
    textConstants.setValue("END", stringOf("\u001B[0m"))

    val stringLib = FlModuleObj("stringlib", null, NameTable("stringlib"))

    stringLib.setValue("constants", textConstants)

    return stringLib
}