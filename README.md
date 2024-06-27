## Fl Lang

---

<div align="center">
	<img width="25%" src="resources/logo.svg" alt="logo">
</div>

### Why does this exist?
There are no shortage of solutions to the question "what programming language"
should I learn, but luckily this project is nowhere near adjacent to that question.
Fl is my pet project for learning about programming language design as well as
to be able to add built-in language features that easily with an API built with extendability
in mind.

### Why is it built on kotlin?
Fl is built in kotlin because kotlin runs on the Java virtual machine but 
provides (in my opinion) a better developer experience *when we ignore the compile time*.
As well as this, I have previous experience in creating projects in kotlin and I make use of
the Intellij suite of IDEs (for which kotlin has excellent first-class support); all these
points combined made Kotlin the easy choice.

### What is flamingo?
Fl is basically a scripting language which wraps around Kotlin. 
It uses an AST based approach to compiling where source code is compiled 
into an abstract syntax tree, then from there is compiled into a byte-code 
like format. It runs on the flamingo VM bundled with the compiler and tries to 
take heavy inspiration from Kotlin and other languages with quality of life features.
The ultimate goal is for it to be able to be used as a tool to show simple and
complex programming concepts to people who may not really know programming.

### What are/will be the key features?
* interpreted and dynamically typed
* first-class function and class support
* operator overloading
* rich anonymous lambda support
* focus on helper methods and minimizing lines written