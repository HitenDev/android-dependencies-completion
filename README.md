# android-dependencies-completion

This is an Android Studio / IntelliJ IDEA plugin for search and complete dependencies from google() and mavenCentral() in Gradle projects.

This project base on [GradleDependenciesHelperPlugin](https://github.com/siosio/GradleDependenciesHelperPlugin).

# Features
- support google and mavenCentral，include android's `jetpack`/`androidx`/`support` packages.
- support generating variable and custom variable.
- dependencies variable offline covert.
- simple and clear UI.


![](https://user-gold-cdn.xitu.io/2019/5/29/16b0239b1d4b3618?w=730&h=378&f=png&s=96725)

# Installation
- Plugin website: https://plugins.jetbrains.com/plugin/12479

- Download file: https://github.com/HitenDev/android-dependencies-completion/releases

# Usage

 **Enabled `Code Completion->SmartType Completion` and `Shortcut Key`**

![](https://user-gold-cdn.xitu.io/2019/5/29/16b01bc59c8ffd3d?w=881&h=408&f=png&s=63983)

**Default Shortcut Key**
- macos `^(control) + ⇧(shift) + space`
- windows `ctrl + alt + space`
- linux `ctrl + shift + space`





*All the following operations are manual, please note.*

## Normal Operation

input string in xxx.gradle file

then press `Shortcut Key`

![](https://user-gold-cdn.xitu.io/2019/5/29/16b021d453eef7c0?w=836&h=368&f=gif&s=159919)

## Generating Variable
- append `'#'` to the end of the input string will generate a version variable

- append `'##'` to the end of the input string will generate a full variable

then press `Shortcut Key`

![](https://user-gold-cdn.xitu.io/2019/5/29/16b031d2139637d9?w=850&h=376&f=gif&s=220945)

## Custom Variable

- append `'#'+custom` to the end of the input string will generate a version variable

- append `'##'+custom` to the end of the input string will generate a full variable

then press `Shortcut Key`

![](https://user-gold-cdn.xitu.io/2019/5/29/16b031d3cef4a76c?w=794&h=324&f=gif&s=235109)


# Contact Me

- `nickname`: HitenDev
- `email`: zzdxit@gmail.com
- `juejin`: https://juejin.im/user/595a16125188250d944c6997
