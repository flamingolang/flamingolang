## TODO

---

* Implement generator functions again

* ## `Obj`
  * Standard object methods
    * `getAttribute(name)`
    * `getAttributeOrNull(name)`
    * `setAttribute(name, value)`
    * `equalsAny(vararg values)`
    * `isAny(vararg values)`
    * `inAny(vararg collections)`
  * Other methods
    * ``

* ## `Array`
  * Standard iterable helper methods
    * `mapped(transformation) -> array`
    * `filtered(predicate) -> array`
    * `where(predicate, transform) -> array`
    * `joinString -> string`
    * `meta$contains(item)`
  * Other methods 
    * `toList`
  * Implement array specific iterator

* ## `List`
  * Standard iterable helper methods
    * `join(string)`
    * `merge(collection)`
    * `swap(list) -> self`
    * `meta$contains(item)`
  * Other helper methods
    * `toArray`
  * Implement list specific iterator 

* ## `String`
  * String helper methods
    * `replace(pattern, replacement)`
    * `replaceAll(varkwarg replacements)`
    * `matchesRegex(pattern)`
    * `isNumber`
    * `isIdentifier`
    * `isAlphabetic`
    * `isNumeric`
    * `isAlphanumeric`
    * `meta$contains(substring)`

* ## `Number`
  * Number helper methods
    * `floorDiv(operand)`

