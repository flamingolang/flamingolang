package objects.libraries

import objects.base.stringOf


fun getStringLibrary(): FlModuleObj {
    val textConstants = FlModuleObj("stringlib.constants", null)

    textConstants.moduleAttributes["RESET"] = stringOf("\u001B[0m")
    textConstants.moduleAttributes["BLACK"] = stringOf("\u001B[0;30m")
    textConstants.moduleAttributes["RED"] = stringOf("\u001B[0;31m")
    textConstants.moduleAttributes["GREEN"] = stringOf("\u001B[0;32m")
    textConstants.moduleAttributes["BROWN"] = stringOf("\u001B[0;33m")
    textConstants.moduleAttributes["BLUE"] = stringOf("\u001B[0;34m")
    textConstants.moduleAttributes["PURPLE"] = stringOf("\u001B[0;35m")
    textConstants.moduleAttributes["CYAN"] = stringOf("\u001B[0;36m")
    textConstants.moduleAttributes["LIGHT_GRAY"] = stringOf("\u001B[0;37m")
    textConstants.moduleAttributes["DARK_GRAY"] = stringOf("\u001B[1;30m")
    textConstants.moduleAttributes["LIGHT_RED"] = stringOf("\u001B[1;31m")
    textConstants.moduleAttributes["LIGHT_GREEN"] = stringOf("\u001B[1;32m")
    textConstants.moduleAttributes["YELLOW"] = stringOf("\u001B[1;33m")
    textConstants.moduleAttributes["LIGHT_BLUE"] = stringOf("\u001B[1;34m")
    textConstants.moduleAttributes["LIGHT_PURPLE"] = stringOf("\u001B[1;35m")
    textConstants.moduleAttributes["LIGHT_CYAN"] = stringOf("\u001B[1;36m")
    textConstants.moduleAttributes["LIGHT_WHITE"] = stringOf("\u001B[1;37m")
    textConstants.moduleAttributes["BOLD"] = stringOf("\u001B[1m")
    textConstants.moduleAttributes["FAINT"] = stringOf("\u001B[2m")
    textConstants.moduleAttributes["ITALIC"] = stringOf("\u001B[3m")
    textConstants.moduleAttributes["UNDERLINE"] = stringOf("\u001B[4m")
    textConstants.moduleAttributes["BLINK"] = stringOf("\u001B[5m")
    textConstants.moduleAttributes["NEGATIVE"] = stringOf("\u001B[7m")
    textConstants.moduleAttributes["CROSSED"] = stringOf("\u001B[9m")
    textConstants.moduleAttributes["END"] = stringOf("\u001B[0m")

    val stringLib = FlModuleObj("stringlib", null)

    stringLib.moduleAttributes["constants"] = textConstants

    return stringLib
}