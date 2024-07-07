package setup

import objects.libraries.*

fun getBuiltinLibrary(): FlModuleObj {
    val builtinsLib = FlModuleObj("setup.getBuiltins", null)
    builtins.entries.forEach { (key, value) -> builtinsLib.moduleAttributes[key] = value }
    return builtinsLib
}

fun initLibraries() {
    builtinModules["setup.getBuiltins"] = getBuiltinLibrary()
    builtinModules["flamingo"] = getFlamingoLibrary()
    builtinModules["importlib"] = getImportLibrary()
    builtinModules["stringlib"] = getStringLibrary()
    builtinModules["flamingodoc"] = getFlamingoDocLibrary()
}