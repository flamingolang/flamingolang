package setup

import objects.libraries.*
import objects.libraries.flamingodoc.getFlamingoDocLibrary
import runtime.NameTable

fun getBuiltinLibrary(): FlModuleObj {
    val builtinsLib = FlModuleObj("builtins", null, NameTable("builtins"))
    builtins.entries.forEach { (key, value) -> builtinsLib.setValue(key, value) }
    return builtinsLib
}

fun initLibraries() {
    builtinModules["builtins"] = getBuiltinLibrary()
    builtinModules["flamingo"] = getFlamingoLibrary()
    builtinModules["importlib"] = getImportLibrary()
    builtinModules["stringlib"] = getStringLibrary()
    builtinModules["flamingodoc"] = getFlamingoDocLibrary()
}