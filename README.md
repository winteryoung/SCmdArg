# SCmdArg

A Scala command line argument/option parser.

## License

This project is distributed under the Apache software license, version 2.0.
You can see the terms here: http://www.apache.org/licenses/LICENSE-2.0.txt

## Simplest Case
```scala
import com.iteye.yangdong.scmdarg._
import com.iteye.yangdong.scmdarg.CmdArgValueConverter._

def main(args: Array[String]) {
  val cmdArgs = new CmdArgParser("app", "Test application.") {
    val arg1 = arg[Int]("arg1", "Description to arg1")
  }
  cmdArgs.parse(args)
  
  println("arg1 value: " + (cmdArgs.arg1.get + 1))
}
```
Given the command line argument "--arg1 1", running this app will output:
```
arg1 value: 2
```

## Installation

SCmdArg uses Maven to build itself. Please see the POM to get the correct groupId and artifactId. Currently it
has not been uploaded to the Maven central repository. If you need to use it before the upload, please download
the source and `mvn install` on your machine. It doesn't rely on anything except the Scala runtime.

## Features

### Linux Command Line Style

#### Long argument names

Long argument names start with double dashes "--".
```scala
arg[String]()
```

#### Short argument names

Short argument names start with a single dash "-".
```scala
arg[String](shortName = Some('a'))
```

#### Boolean arguments

Turning a boolean argument value to `true` requires the presence of the argument name.
Explicitly setting `true` or `false` is not supported and the default value to a boolean argument must be `false`.

**Defining the argument**
```scala
val cmdArgs = new CmdArgParser("app") {
  val arg1 = arg[Boolean]()
}
cmdArgs.parse(args)
println(cmdArgs.arg1.get)
```

**Command line invocation**
```
> app --arg1
true
```

#### Combining short argument names

##### Example 1. Boolean arguments combination

**Defining the arguments**
```scala
val xx = arg[Boolean](shortName = Some('x'))
val yy = arg[Boolean](shortName = Some('y'))
val zz = arg[Boolean](shortName = Some('z'))
```

**Command line invocation**
```
> app -xyz
```

##### Example 2. `GREP` style arguments combination

The `grep` command in the Linux boxes allow one to retrieve the contextual information of the matching lines.
Let's say we wan to retrieve 1 line before the matching line and 3 lines after, we can write this:
```shell
someCommand | grep -A1 -B3 somePattern
```

Similarly,

**Defining the arguments**
```scala
val before = arg[Int](shortName = Some('B'))
val after = arg[Int](shortName = Some('A'))
```

**Command line invocation**
```shell
> app -A1 -B3
```

### Multi-value Arguments

A multi-value argument is one that takes a single value, but if it appears multiple times on the command line,
each occurrence adds its value to the list of already accumulated values for the argument.

**Example**
```scala
val cmdArgs = new CmdArgParser("app") {
  val arg1 = marg[Int]()
}
cmdArgs.parse(args)
println(cmdArgs.arg1)
```
```
> app --arg1 1 --arg1 2 --arg1 3
List(1, 2, 3)
```

### Help Support

You can display the help message explicitly by invoking the `displayHelp()` method. SCmdArg automatically
displays help when an argument parsing error occurred. The help message is composed of the application
description which you would specify by setting `appDesc` property of `CmdArgParser` and the argument
description list.

### Type Safety and Command Argument Converters

Like you have already seen, a type argument is required to define an command argument. The built-in
supported types includes (for the most up to date list see the source of `CmdArgValueConverter`):
* String
* Byte
* Short
* Int
* Long
* Float
* Double
* Boolean
* java.nio.file.Path
* java.nio.file.PathMatcher
* java.net.URL

To use the built-in converters, do this import:
```scala
import com.iteye.yangdong.CmdArgValueConverter._
```

To define a custom converter for your type:
```scala
class MyType
implicit object Converter4MyType extends CmdArgValueConverter[MyType] {
  ... // implement the members
}
val cmdArgs = new CmdArgParser("app") {
  val arg1 = arg[MyType]()
}
```

SCmdArg doesn't have a regex like validation support. If you want some very special validation,
define a type and write a type converter.

### The Default Argument

```scala
val cmdArgs = new CmdArgParser("app") {
  val arg1 = arg[Int](isDefault = true)
}
cmdArgs.parse(args)
println("arg1: " + cmdArgs.arg1)
```
```
> app 2
arg1: 2
```

### Default values

```scala
val cmdArgs = new CmdArgParser("app") {
  val arg1 = arg[Int](default = Some(1))
}
cmdArgs.parse(args)
println("arg1: " + cmdArgs.arg1)
```
```
> app
arg1: 1
```

### Validation

#### Required arguments

```scala
val cmdArgs = new CmdArgParser("app") {
  val arg1 = arg[Int](isRequired = true)
}
```
Invoking this command without giving any arguments will cause the app to exit and the error message
and the the help message will be displayed.

#### Valid Value Set

```scala
val cmdArgs = new CmdArgParser("app") {
  val arg1 = arg[Int](isRequired = true, validValueSet = Set("a", "b", "c"))
}
```
Invoking this command without giving `arg1` a value or value other than "a", "b", "c" will cause error.

#### Dependencies and Exclusiveness Between Arguments

Let's say you are writing an app that counts the line numbers of the given input or file.
An argument "--printFileName" would only make sense if a file was given. So:
```scala
val cmdArgs = new CmdArgParser("app") {
  val file = arg[Path]("file")
  val printFileName = arg[Boolean]()
  
  rel(printFileName ~> file)
  // or
  rel(printFileName dependsOn file)
}
```
"rel" stands for relationship.

Similarly, let's say arg1 would NOT make sense if arg2 is present:
```scala
val cmdArgs = new CmdArgParser("app") {
  val arg1 = arg[Path]()
  val arg2 = arg[Boolean]()
  
  rel(arg1 !~> arg2)
  // or
  rel(arg1 exclusiveFrom arg2)
}
```

Suppose you have an app that takes an "action" argument, and another argument "inputFile" only makes sense
if "action" equals to "countLineNumbers":
```scala
val cmdArgs = new CmdArgParser("app") {
  val action = arg[String]()
  val inputFile = arg[Path]()
  
  rel(inputFile ~> (action === "countLineNumbers"))
}
```

Suppose the "actions" argument is a multi-value argument and "inputFile" only makes sense if "actions"
contains "countLineNumbers":
```scala
val cmdArgs = new CmdArgParser("app") {
  val actions = marg[String]()
  val inputFile = arg[Path]()
  
  rel(inputFile ~> (actions contains "countLineNumbers"))
}
```

In fact, it could be more versatile. Let's say the presence of both x and y only makes sense if u or v is present:
```scala
rel((x and y) ~> (u or v))
```
a even more crazy example:
```scala
rel((z or (x and y)) ~> (u or (v == "2")))
```

## What's Not Supported?

SCmdArg doesn't support some features in other command line argument parsers (like Argot):

* Multiple default arguments / positional arguments

Multiple default arguments allows you to specify multiple arguments without giving their argument names.